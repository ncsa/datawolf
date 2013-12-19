var personCollection = new PersonCollection();
var showingLoginError = false;
var showingRegistrationError = false;

var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    // Show login form
    list:function() {
    	
    	personCollection.fetch({success: function() {
    		$('#login-form').html(new LoginView().render().el);
            $('#register-form').html(new RegistrationView().render().el);

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

            $('#firstname').keypress(registrationError);
            $('#lastname').keypress(registrationError);
            $('#email').keypress(registrationError);
            $('#newpassword').keypress(registrationError);

    	}});
    }
});

var registrationError = function() {
    if(showingRegistrationError) {
        $("#registration-error").hide();
    }
};

var createPerson = function(firstName, lastName, email) {
    var url = '/persons?'+'firstname='+firstName+'&lastname='+lastName+'&email='+email;
    $.ajax({
        type: "POST",
        url: url,
        dataType: "text",

        success: function(msg) {
            console.log("created person with id = "+msg);
            localStorage.currentUser = msg;
            location.replace("index.html");
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    }); 
}

var app = new AppRouter();

Backbone.history.start();