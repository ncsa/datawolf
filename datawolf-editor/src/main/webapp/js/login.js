var showingLoginError = false;
var showingRegistrationError = false;

var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    // Show login form
    list:function() {
    	
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
            showingLoginError = true;
            $("#login-error").show();
        }
    })
}

var createAccount = function(email, password, userId) {
    var url = datawolfOptions.rest + '/login?email='+email+'&password='+password;
    $.ajax({
        type: "POST",
        url: url,
        dataType: "text",
        success: function(msg) {
            if(msg === 'Not Active') {
                showingRegistrationError = true;
                document.getElementById("registration-error-text").innerHTML = "Registration successful, but the account is not active. Please contact an administrator.";
                $("#registration-error").show();
            } else {
                localStorage.currentUser = userId;
                location.replace("index.html");
            }
        },
        error: function(msg) {
            console.log(JSON.stringify(msg));
            // TODO add more user friendly error message
            showingRegistrationError = true;
            document.getElementById("registration-error-text").innerHTML = msg.responseText;
            $("#registration-error").show();
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
            createAccount(email, password, msg);
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    }); 
}

var app = new AppRouter();

Backbone.history.start();
