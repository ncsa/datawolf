var Submission = Backbone.Model.extend({

	defaults: {
        workflowId: null,    	/** Workflow that is executed */
        creatorId: null,    	/** creator of the execution */
        title: null, 			/** title of the execution **/
        description: null, 		/** description of the execution **/
        parameters: null,    	/** maping a parameter to a specific parameter in the workflow */
		datasets: null   		/** maping a dataset to a specific dataset in the workflow */
    },

	initialize: function() {

	},

});
