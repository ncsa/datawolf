var WorkflowListView = Backbone.View.extend({
	tagName: "ul",

	className: 'workflowToolView',
	events: {
		"change" : "onChange",
		"click" : "onClick" 
	},

	initialize: function() {
		//this.$el.attr('size', '10');
		this.model.bind("reset", this.render, this);
		var self = this;
		this.model.bind("add", function(workflow) {
			$(self.el).append(new WorkflowListItemView({model: workflow}).render().el);
		});
	},

	render: function(e) {
		$(this.el).empty();
		_.each(this.model.models, function(workflow) {
			$(this.el).append(new WorkflowListItemView({model: workflow}).render().el);
		}, this);

		return this;
	},

});

var WorkflowListItemView = Backbone.View.extend({
	tagName: "li",
	template: _.template($('#workflow-list-item').html()),

	events: {
		"click" : "onClick",
		"dblclick" : "onDoubleClick",
		"mouseenter" : "showDetails",
		"mouseleave" : "hideDetails",
	},

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

		var popoverTitle = _.template($('#workflow-popover').html())
		var popoverContent = _.template($('#workflow-popover-content').html());
		return this;
	},
	close: function() {
		$(this.el).unbind();
		$(this.el).remove();
	},

	onClick: function(e) {
		var id = this.model.get('id');
		currentDataset=null;
		currentDatasetEl=null;
		currentWorkflow=getWorkflow(id);
		currentWorkflowEl=$(this.el);
		$('.highlight').removeClass('highlight');
		$(this.el).addClass('highlight');
	},

	onDoubleClick: function(e) {
		e.preventDefault();
		var id = this.model.get('id');

		$('.highlight').removeClass('highlight');
		$(this.el).addClass('highlight');
		eventBus.trigger("clicked:newopenworkflow", id);
	},

	showDetails: function() {
		// if we want to show on over, put popover code here	
	},

	hideDetails: function() {
		// TODO - this calls a lot of unnecessary destroys
		//$(this.el).popover('destroy');
	}

});

var WorkflowButtonView = Backbone.View.extend({
	events: {
		"click button#workflow-info-btn" : "workflowInfo",
		// "click button#new-workflow" : "newWorkflow",
		// "click button#delete-workflow" : "deleteWorkflow",
		"click button#workflow-open-btn" : "openWorkflow",
		// "click button#save-workflow" : "saveWorkflow",
		// "click button#copy-workflow" : "copyWorkflow"
	},

	template: _.template($("#new-workflow-buttons").html()),

	render: function(e) {
		$(this.el).html(this.template());

		return this;
	},

	openWorkflow: function(e) {
		// var selection = $('#workflowSelector').val();
		eventBus.trigger("clicked:openworkflow", selection);
	},


	workflowInfo : function() {
		if(currentWorkflow != null) {
			var json = currentWorkflow.toJSON();
			json.numExecutions = "1.0";//numExecutions[model.get('id')]; //TODO
			json.avgTime = "0.0 s";//TODO
			var popoverTitle = _.template($('#workflow-popover').html())
			var popoverContent = _.template($('#workflow-popover-content').html());
			currentWorkflowEl.popover({html: true, title: popoverTitle(json), content: popoverContent(json), trigger: 'manual'});
			currentWorkflowEl.popover('toggle');
		}
	}
});


