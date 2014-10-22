var WorkflowTool = Backbone.Model.extend({
	urlRoot: datawolfOptions.rest + "/workflowtools",
	
	getDeleteUrl: function() {
		return datawolfOptions.rest + '/workflowtools/' + this.id;
	},

	getInputs: function() {
		return new WorkflowToolDataCollection(this.get("inputs"));
	},

	getOutputs: function() {
		return new WorkflowToolDataCollection(this.get("outputs"));
	},		

	getParameters: function() {
		return new WorkflowToolParameterCollection(this.get('parameters'));
	},

	getBlobs: function() {
		return new FileDescriptorCollection(this.get('blobs'));
	},

	getExecutor: function() {
		return this.get('executor');
	}
});

var WorkflowToolCollection = Backbone.Collection.extend({
	model: WorkflowTool,
	url: datawolfOptions.rest + "/workflowtools"
});
