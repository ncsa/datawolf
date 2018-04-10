var showingPasswordError = false;

var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    // Show Password reset form
    list:function() {
    	
      $('#reset-password-form').html(new ResetPasswordView().render().el);  
      $('#tokenInput').keypress(function() {
        if(showingPasswordError) {
          $("#password-error").hide();
        }
      });

      $('#passwordInput').keypress(function() {
        if(showingPasswordError) {
          $("#password-error").hide();
        }
      });

      $('#confirmPasswordInput').keypress(function() {
        if(showingPasswordError) {
          $("#password-error").hide();
      }
    });
    }
});

var updatePassword = function(token, newPassword, confirmNewPassword) {
    var url = datawolfOptions.rest + '/login/updatePassword?password='+newPassword;
    if(newPassword != confirmNewPassword) {
      showingPasswordError = true;
      $("#password-error").show();
    } else {
        $.ajax({
            type: "POST",
            url: url,
            dataType: "text",
            beforeSend: function (xhr) {
                xhr.setRequestHeader ("Authorization", token);
            },

            success: function(msg) {
                url = datawolfOptions.rest + '/login/token';
                $.ajax({
                    type: "DELETE",
                    url: url,
                    dataType: "text",
                    beforeSend: function (xhr) {
                        xhr.setRequestHeader ("Authorization", token);
                    },

                    success: function(msg) {
                        $("#password-success").show();
                    },

                    error: function(msg) {
                        showingPasswordError = true;
                        $("#password-error").show();
                    }
                });
            },

            error: function(msg) {
                showingPasswordError = true;
                $("#password-error").show();
            }
        });
    }
}

var app = new AppRouter();

Backbone.history.start();
