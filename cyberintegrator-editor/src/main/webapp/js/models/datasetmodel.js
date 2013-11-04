var Dataset = Backbone.Model.extend({
	
	defaults: {
        title: null, 			/** title of the dataset **/
        description: null, 		/** description of the dataset **/
    },

	getCreator: function() {
		return new Person(this.get('creator'));
	},

	getFileDescriptors: function() {
		return new FileDescriptorCollection(this.get('fileDescriptors'));
	},

});

var DatasetCollection = Backbone.Collection.extend({
	model: Dataset,
	url: "/datasets", //"http://localhost:8080/datasets"
	remote: true,
	
	getReadUrl: function() {
		return '/datasets/'+ '?email=' + currentUser.get('email');
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


