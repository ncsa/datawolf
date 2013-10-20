var Dataset = Backbone.Model.extend({
	
	defaults: {
        title: null, 			/** title of the dataset **/
        description: null, 		/** description of the dataset **/
    },

	getCreator: function() {
		return new Person(this.get('creator'));
	},

});

var DatasetCollection = Backbone.Collection.extend({
	model: Dataset,
	url: "/datasets" //"http://localhost:8080/datasets"
});