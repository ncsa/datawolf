var personCollection = new PersonCollection();
var showingLoginError = false;

var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    list:function() {
    	
    	personCollection.fetch({success: function() {
    		$('#login-form').html(new LoginView().render().el);
    		$('#username').keypress(function() {
    			if(showingLoginError) {
    				$("#login-error").hide();
    			}
    		});

			$('#password').keypress(function() {
				if(showingLoginError) {
    				$("#login-error").hide();
    			}
    		});
    	}});
    }
});

var app = new AppRouter();

Backbone.history.start();