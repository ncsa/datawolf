var WorkflowListView = Backbone.View.extend({
	tagName: "select",
	id: "workflowSelector",

	events: {
		"change" : "onChange"
	},

	initialize: function() {
		this.$el.attr('size', '10');
		this.model.bind("reset", this.render, this);
		this.model.fetch();
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
		//console.log("selection is "+selection);
	}

});

var WorkflowListItemView = Backbone.View.extend({
	tagName: "option",

	template: _.template($('#workflow-list-item').html()),

	attributes: function() {
		return {
			value: this.model.get('id')
		}
	},

	render: function(e) {
		$(this.el).html(this.template(this.model.toJSON()));

		return this;
	}
});

var WorkflowButtonView = Backbone.View.extend({
	events: {
		"click button#new-workflow" : "newWorkflow",
		"click button#delete-workflow" : "deleteWorkflow",
		"click button#open-workflow" : "openWorkflow"
	},

	template: _.template($("#new-workflow-buttons").html()),

	render: function(e) {
		$(this.el).html(this.template());
		return this;
	},

	newWorkflow: function(e) {
		e.preventDefault();
		console.log("trigger new");
		eventBus.trigger("clicked:newworkflow", null);	
	},

	deleteWorkflow: function(e) {
		// TODO implement delete workflow
		e.preventDefault();

		var selectedWorkflow = $('#workflowSelector').val();
		if(selectedWorkflow != null) {
			var tmp = null;
			workflowCollection.each(function(workflow) {
				if(workflow.get('id') === selectedWorkflow) {
					tmp = workflow;
					return false;
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

		// TODO add creator to the workflow, this should come from the user that logs into the system when that is in place
		var workflow = new Workflow({id: id, title: title, date: date});
		workflowCollection.create(workflow);
		$('#modalWorkflowView').modal('hide');
		eventBus.trigger("clicked:createworkflow", id);
	},

	cancelWorkflow: function(e) {
		e.preventDefault();
		$('#modalWorkflowView').modal('hide');
	}
});

