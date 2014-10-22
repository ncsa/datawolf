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

var GraphStepLocation = Backbone.Model.extend({
	getX: function() {
		return this.get('x');
	},
	getY: function() {
		return this.get('y');
	}
});

var GraphStepLocationCollection = Backbone.Collection.extend({
	// dual storage required a url to create a local storage location
	url: datawolfOptions.rest + '/graphlocation/',
	model: GraphStepLocation,
	local: true
	//localStorage: new Backbone.LocalStorage('graphlocations')
});
