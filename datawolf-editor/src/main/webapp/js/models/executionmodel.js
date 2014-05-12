var Execution = Backbone.Model.extend({
    urlRoot: "/executions/",
});

var ExecutionCollection = Backbone.Collection.extend({
    model: Execution,
    //localStorage: new Backbone.LocalStorage('executions'),
    url: "/executions", //"http://localhost:8080/executions",

    remote: true,
	
	getReadUrl: function() {
		return '/executions/'+ '?email=' + currentUser.get('email');
	},

	sync: function(method, model, options) {
		// Filter by user
    	if(method === 'read') {
			options = options || {};
    		options.url = model.getReadUrl();
    	}
    	
    	return Backbone.sync.apply(this, arguments);
 	}
});
