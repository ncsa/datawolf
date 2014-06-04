var LoginView = Backbone.View.extend({

	template: _.template($('#login-view-template').html()),
	events: {
		'click button#login-btn' : 'userLogin'
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

		var user = null;

		// Find if person exists
		personCollection.each(function(person) {
			if(person.get('email') === email) {
				user = person;
				return false;
			}
		});

		if(user != null) {
			checkLogin(email, password);
		} else {
			showingLoginError = true;
			$("#login-error").show();
		}
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

		var user = null;
		// Find if person exists
		personCollection.each(function(person) {
			if(person.get('email') === email) {
				user = person;
				return false;
			}
		});

		if(user != null) {
			showingRegistrationError = true;
			$("#registration-error").show();
		} else {
			var firstName = $('input[name=firstname]').val();
			var lastName = $('input[name=lastname]').val();
			var password = $('input[name=newpassword]').val();

			createPerson(firstName, lastName, email, password);
		}
	}


})
