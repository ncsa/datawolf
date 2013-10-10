var Workflow = Backbone.Model.extend({
	urlRoot: "/workflows/",

	getDeleteUrl: function() {
		return '/workflows/'+ this.id + '/delete';
	},

	getCreator: function() {
		return new Person(this.get('creator'));
	},

	getSteps: function() {
		return new WorkflowStepCollection(this.get('steps'));
	},

	removeStep: function(stepId) {
		var stepCollection = this.getSteps();
		var step = null;
		stepCollection.each(function(workflowStep) {
			if(stepId === workflowStep.get('id')) {
				step = workflowStep;
				return false;
			}
		});
		stepCollection.remove(step);

		// Without this, the steps collection in the model does not get updated
		// We might be able to fix this if we store the steps in their own local storage
		// Then we can call step.destroy()
		this.set({steps: stepCollection });
	},

	sync: function(method, model, options) {
    	
    	if(method === 'delete') {
			options = options || {};
    		options.url = model.getDeleteUrl();
    	}
    	
    	return Backbone.sync.apply(this, arguments);
 	}

});

var WorkflowCollection = Backbone.Collection.extend({
	model: Workflow,
	//localStorage: new Backbone.LocalStorage('workflows'),
	url: "/workflows/",
	methodUrl: {
		'delete' : '/workflows/'+this.id + '/delete/'
	},
});