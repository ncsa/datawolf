var WorkflowListView = Backbone.View.extend({
	tagName: "select",
	id: "workflowSelector",
	className: 'cbi-select',

	events: {
		"change" : "onChange"
	},

	initialize: function() {
		this.$el.attr('size', '10');
		this.model.bind("reset", this.render, this);
		var self = this;
		this.model.bind("add", function(workflow) {
			$(self.el).append(new WorkflowListItemView({model: workflow}).render().el);
		});
		
		//this.model.fetch();
	},

	render: function(e) {
		$(this.el).empty();
		_.each(this.model.models, function(workflow) {
			$(this.el).append(new WorkflowListItemView({model: workflow}).render().el);
		}, this);

		return this;
	},

	onChange: function(e) {
		var selection = $('#workflowSelector').val();
		console.log("selection is "+selection);
		//console.log(JSON.stringify(getWorkflow(selection), undefined, 2));
	}

});

var WorkflowListItemView = Backbone.View.extend({
	tagName: "option",

	template: _.template($('#workflow-list-item').html()),

	initialize: function() {
		this.model.bind("change", this.render, this);
		this.model.bind("destroy", this.close, this);
	},

	attributes: function() {
		return {
			value: this.model.get('id')
		}
	},

	render: function(e) {
		$(this.el).html(this.template(this.model.toJSON()));

		return this;
	},
	close: function() {
		$(this.el).unbind();
		$(this.el).remove();
	}
});

var WorkflowButtonView = Backbone.View.extend({
	events: {
		"click button#new-workflow" : "newWorkflow",
		"click button#delete-workflow" : "deleteWorkflow",
		"click button#open-workflow" : "openWorkflow",
		"click button#save-workflow" : "saveWorkflow"
	},

	template: _.template($("#new-workflow-buttons").html()),

	render: function(e) {
		$(this.el).html(this.template());

		return this;
	},

	newWorkflow: function(e) {
		e.preventDefault();
		eventBus.trigger("clicked:newworkflow", null);	
	},

	deleteWorkflow: function(e) {
		// TODO implement delete workflow completely (both REST service and client)
		e.preventDefault();

		var selectedWorkflow = $('#workflowSelector').val();
		console.log("selected workflow = "+selectedWorkflow);
		if(selectedWorkflow != null) {
			var tmp = null;
			workflowCollection.each(function(workflow) {
				if(workflow.get('id') === selectedWorkflow) {
					tmp = workflow;
					return false;
				}
			});

			tmp.getSteps().each(function(workflowStep) {
				var location = null;
				stepLocationCollection.each(function(graphLocation) {
					if(graphLocation.get('id') === workflowStep.get('id')) {
						location = graphLocation;
						return false;
					}
				});
				
				if(location != null) {
					location.destroy();
				}
			});
			tmp.destroy();
			if(currentWorkflow === selectedWorkflow) {
				eventBus.trigger('clearWorkflow', null);
			}
			eventBus.trigger('clicked:deleteworkflow', null);
		}
	},

	openWorkflow: function(e) {
		var selection = $('#workflowSelector').val();
		eventBus.trigger("clicked:openworkflow", selection);
	},

	saveWorkflow: function(e) {
		var selection = $('#workflowSelector').val();
		var workflow = getWorkflow(selection);
		//console.log("saving workflow");
		//console.log(JSON.stringify(workflow, undefined, 2));
		//workflow.set({title: "test1" });
		//workflow.save();
		//postWorkflow(workflow);
	}

});

var AddWorkflowView = Backbone.View.extend({
	events: {
		"click button#create-workflow" : "createWorkflow",
		"click button#cancel" : "cancelWorkflow"
	},

	template: _.template($("#new-workflow").html()),

	render: function(e) {
		$(this.el).html(this.template());
		return this;
	},

	createWorkflow: function(e) {
		e.preventDefault();

		// TODO add validation that the input is correct
		var id = generateUUID();
		var title = this.$('input[name=workflow-title]').val();
		var date = new Date();
		var creator = currentUser.toJSON();
		var _this = this.model;
		this.model.save({title: title, created: date, creator: creator}, {
			wait: true,

			success: function(model, response) {
				console.log("workflow created - success.");
				workflowCollection.add(_this);
				eventBus.trigger("clicked:createworkflow", model.get('id'));//id);
			},
			error: function(model, error) {
				//console.log(JSON.stringify(model, undefined, 2));
				console.log("workflow not saved: "+error.responseText);
			}
		});

		$('#modalWorkflowView').modal('hide');
	},

	cancelWorkflow: function(e) {
		e.preventDefault();
		$('#modalWorkflowView').modal('hide');
	}
});

