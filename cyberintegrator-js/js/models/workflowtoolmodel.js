var WorkflowTool = Backbone.Model.extend({
	urlRoot: "/workflowtools/",

	getInputs: function() {
		return new WorkflowToolDataCollection(this.get("inputs"));
	},

	getOutputs: function() {
		return new WorkflowToolDataCollection(this.get("outputs"));
	},		

	getParameters: function() {
		return new WorkflowToolParameterCollection(this.get('parameters'));
	}
});

var WorkflowToolCollection = Backbone.Collection.extend({
	model: WorkflowTool,
	url: "/workflowtools"
});