var WorkflowToolData = Backbone.Model.extend({
	initialize: function() {
	},

	setId: function(id) {
		this.set('id', id);
	},

	setTitle: function(title) {
		this.set('title', title);
	},

	setDescription: function(description) {
		this.set('description', description);
	},

	setMimeType: function(mimeType) {
		this.set('mimeType', mimeType);
	},

	setDataId: function(dataId) {
		this.set('dataId', dataId);
	},

	getDataId: function() {
		return this.get('dataId');
	}

});

var WorkflowToolDataCollection = Backbone.Collection.extend({
	model: WorkflowToolData
});