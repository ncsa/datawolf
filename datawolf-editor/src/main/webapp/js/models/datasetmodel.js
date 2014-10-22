var Dataset = Backbone.Model.extend({
	urlRoot: datawolfOptions.rest + "/datasets",

	defaults: {
        title: null, 			/** title of the dataset **/
        description: null, 		/** description of the dataset **/
    },

    getDeleteUrl: function() {
		return datawolfOptions.rest + '/datasets/'+ this.id;
	},

	getCreator: function() {
		return new Person(this.get('creator'));
	},

	getFileDescriptors: function() {
		return new FileDescriptorCollection(this.get('fileDescriptors'));
	},

	sync: function(method, model, options) {
    	if(method === 'delete') {
    		console.log("set delete url");
			options = options || {};
    		options.url = model.getDeleteUrl();
    	} 

    	return Backbone.sync.apply(this, arguments);
 	}

});

var DatasetCollection = Backbone.Collection.extend({
	model: Dataset,
	url: datawolfOptions.rest + "/datasets", 
	remote: true,
	
	getReadUrl: function() {
		return datawolfOptions.rest + '/datasets/'+ '?email=' + currentUser.get('email');
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


