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

	userLogin: function(e) {
		e.preventDefault();
		var email = $('input[name=username]').val();
		var user = null;
		personCollection.each(function(person) {
			if(person.get('email') === email) {
				user = person;
				return false;
			}
		});

		if(user != null) {
			localStorage.currentUser = user.get('id');
			location.replace("index.html");

		} else {
			showingLoginError = true;
			$("#login-error").show();
		}
	}

});