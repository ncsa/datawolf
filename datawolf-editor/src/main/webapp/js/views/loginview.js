var LoginView = Backbone.View.extend({

	template: _.template($('#login-view-template').html()),
	events: {
		'click button#login-btn' : 'userLogin',
		'click button#forgot-password' : 'forgotPassword'
	},

	initialize: function() {
	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	// TODO - Add better authentication using OpenID or other authentication method
	userLogin: function(e) {
		e.preventDefault();
		var email = $('input[name=username]').val();
		var password = $('input[name=password]').val();

		checkLogin(email, password);
	},

	forgotPassword: function(e) {
        $('#login-form').html(new ResetPasswordView().render().el);
    }

});

var DataWolfRegistrationButtonView = Backbone.View.extend({
    template: _.template($('#register-button-view-template').html()),

    events: {
        'click button#registerDefault' : "registerDefault"
    },

    initialize: function() {

    },

    render: function() {
        $(this.el).html(this.template());
        return this;
    },

    registerDefault: function(e) {
        $('#register-form').html(new DataWolfRegistrationView().render().el);
    }
});

var DataWolfRegistrationView = Backbone.View.extend({
	template: _.template($('#register-view-template').html()),

	events: {
		'click button#register-btn' : 'registerUser'
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	registerUser: function(e) {
		e.preventDefault();

		var email = $('input[name=email]').val();
		var firstName = $('input[name=firstname]').val();
		var lastName = $('input[name=lastname]').val();
        var password = $('input[name=newpassword]').val();
        var confirmPassword = $('input[name=confirmPassword]').val();

		if(password.length < 6) {
            showingRegistrationError = true;
            document.getElementById("registration-error-text").innerHTML = "Password is too short. Password must be 6 or more characters.";
            $("#registration-error").show();
		} else if (password != confirmPassword) {
            showingRegistrationError = true;
            document.getElementById("registration-error-text").innerHTML = "Passwords do not match.";
            $("#registration-error").show();
        } else {
			createPerson(firstName, lastName, email, password);
		}
	}


});


