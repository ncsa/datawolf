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

            if(datawolfOptions.showRegistration) {
                $('#register-form').html(new RegistrationView().render().el);
            }

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

var checkLogin = function(email, password) {
    var token = email + ':' + password;
    var hash = btoa(token);
    var url = '/login?email='+email;
    $.ajax({
        type: "GET",
        url: url,
        dataType: "text",
        beforeSend: function (xhr) {
            xhr.setRequestHeader ("Authorization", "Basic "+hash);
        },

        success: function(msg) {
            if(msg) { 
                var json = JSON.parse(msg);
                localStorage.currentUser = json.id;
                location.replace("index.html");
            } else {
                showingLoginError = true;
                $("#login-error").show();
            }
        },

        error: function(msg) {
            console.log(msg);
        }
    })
}

var createAccount = function(email, password) {
    var url = '/login?email='+email+'&password='+password;
    $.ajax({
        type: "POST",
        url: url,
        dataType: "text",
        success: function(msg) {
            location.replace("index.html");
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    });
}

var createPerson = function(firstName, lastName, email, password) {
    var url = '/persons?'+'firstname='+firstName+'&lastname='+lastName+'&email='+email;
    $.ajax({
        type: "POST",
        url: url,
        dataType: "text",

        success: function(msg) {
            console.log("created person");
            localStorage.currentUser = msg;
            createAccount(email, password);
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    }); 
}

var app = new AppRouter();

Backbone.history.start();
