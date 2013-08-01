var Workflow = Backbone.Model.extend({
	initialize: function() {

	},

	getWorkflowSteps: function() {
		return new WorkflowStepCollection(this.get('steps'));
	}

});

var WorkflowCollection = Backbone.Collection.extend({
	model: Workflow,
	localStorage: new Backbone.LocalStorage('workflows')
});