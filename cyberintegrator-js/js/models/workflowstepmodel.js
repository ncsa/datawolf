var WorkflowStep = Backbone.Model.extend({
	
	getTool: function() {
		return new WorkflowTool(this.get('tool'));
	}
});

var WorkflowStepCollection = Backbone.Collection.extend({
	model: WorkflowStep
});