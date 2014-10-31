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

            // Registration buttons to display forms
            $('#register-buttons').html(new RegistrationButtonView().render().el);

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
    var url = datawolfOptions.rest + '/login?email='+email;
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
    var url = datawolfOptions.rest + '/login?email='+email+'&password='+password;
    $.ajax({
        type: "POST",
        url: url,
        dataType: "text",
        success: function(msg) {
            var user = null;
            // Find if person exists, might need to refetch
            personCollection.fetch({
                success: function() {
                    // TODO make this a function so we can check collection to avoid unnecessary fetch
                    personCollection.each(function(person) {
                        if(person.get('email') === email) {
                            user = person;
                            return false;
                        }
                    });
                    console.log(user.get('id'));
                    localStorage.currentUser = user.get('id');
                    location.replace("index.html");
                },
                error: function() {
                    console.log("Error fetching user list");
                }
            });
        },
        error: function(msg) {
            console.log(JSON.stringify(msg));
            // TODO add more user friendly error message
            alert('error: '+JSON.stringify(msg));
        }
    });
}

var createPerson = function(firstName, lastName, email, password) {
    var url = datawolfOptions.rest + '/persons?'+'firstname='+firstName+'&lastname='+lastName+'&email='+email;
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
