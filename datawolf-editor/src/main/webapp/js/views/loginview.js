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

var RegistrationButtonView = Backbone.View.extend({
	template: _.template($('#register-button-view-template').html()),

	events: {
		'click button#registerMedici' : "registerMedici",
		'click button#registerDefault' : "registerDefault"
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	registerMedici: function(e) {
		$('#register-form').html(new MediciRegistrationView().render().el);
	},

	registerDefault: function(e) {
		$('#register-form').html(new RegistrationView().render().el);
	}
});

var RegistrationView = Backbone.View.extend({
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

var MediciRegistrationView = Backbone.View.extend({
	template: _.template($('#register-medici-view-template').html()),

	events: {
		'click button#register-btn' : 'registerMediciUser'
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	registerMediciUser: function(e) {
		e.preventDefault();
		console.log("Create DataWolf account using Clowder account");
		var email = $('input[name=email]').val();
		var password = $('input[name=newpassword]').val();

		// Check if user/pass is valid before allowing it to be used with DataWolf
		$.ajax({
			url: datawolfOptions.clowder + '/api/me',
			method: 'GET',
			beforeSend: function(xhr) {
				xhr.setRequestHeader('Authorization', 'Basic '+ btoa(email + ':' + password));
			},
			success: function(message) {
				// DataWolf needs to store basic information about the user
				// It might be possible to eliminate this so we can always call Clowder API to validate user as needed
				// TODO handle case if users change their clowder password
				var response = JSON.parse(JSON.stringify(message));
				createAccount(email, password, response.id);
			},

			error: function(message) {
				document.getElementById("registration-error-text").innerHTML = "Error validating Clowder account.";
				$("#registration-error").show()
			}

		});

	}
});
