var ResetPasswordView = Backbone.View.extend({

	template: _.template($('#reset-view-template').html()),
	events: {
		'click button#reset-password-btn' : 'resetPassword'
	},

	initialize: function() {
	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	resetPassword: function(e) {
		e.preventDefault();
		var token = $('input[name=tokenInput]').val();
		var newPassword = $('input[name=passwordInput]').val();
		var confirmNewPassword = $('input[name=confirmPasswordInput]').val();
		updatePassword(token, newPassword, confirmNewPassword);
	}

});


