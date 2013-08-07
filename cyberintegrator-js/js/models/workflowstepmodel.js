var WorkflowStep = Backbone.Model.extend({

	setInput: function(inputDataId, outputId) {
		var inputs = this.get('inputs');
		if(outputId === null) {
			delete inputs[inputDataId];
		} else {
			inputs[inputDataId] = outputId;
		}
	},

	getInputs: function() {
		return this.get('inputs');
	},

	getOutputs: function() {
		return this.get('outputs');
	},
	
	getTool: function() {
		return new WorkflowTool(this.get('tool'));
	}
});

var WorkflowStepCollection = Backbone.Collection.extend({
	model: WorkflowStep
});