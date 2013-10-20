var ExecutionListView = Backbone.View.extend({
	tagName: "ul",

	className: 'executionView',
	events: {
		"change" : "onChange",
		"click" : "onClick" 
	},

	initialize: function() {
		this.model.bind("reset", this.render, this);
		var self = this;
		this.model.bind("add", function(execution) {
			$(self.el).append(new ExecutionListItemView({model: execution}).render().el);
		});
		
	},

	render: function(e) {
		$(this.el).empty();
		_.each(this.model.models, function(execution) {
			$(this.el).append(new ExecutionListItemView({model: execution}).render().el);
		}, this);

		return this;
	}

});

var ExecutionListItemView = Backbone.View.extend({
	tagName: "li",
	template: _.template($('#execution-list-item').html()),

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
		var popoverTitle = _.template($('#execution-popover').html())
		var popoverContent = _.template($('#execution-popover-content').html());
		return this;
	},
	close: function() {
		$(this.el).unbind();
		$(this.el).remove();
	},

	onClick: function(e) {
		var id = this.model.get('id');
		currentExecution=getExecution(id);
		currentExecutionEl=$(this.el);
		$('.highlight').removeClass('highlight');
		$(this.el).addClass('highlight');
	},

	onDoubleClick: function(e) {
		e.preventDefault();
		var id = this.model.get('id');
		$('.highlight').removeClass('highlight');
		$(this.el).addClass('highlight');
		eventBus.trigger("clicked:newopenexecution", id);
	},

});

var ExecutionButtonView = Backbone.View.extend({
	events: {
		"click button#execution-info-btn" : "executionInfo",
	},

	template: _.template($("#execution-buttons").html()),

	render: function(e) {
		$(this.el).html(this.template());

		return this;
	},

	executionInfo : function() {
		if(currentExecution != null) {
			if(currentExecution.get('creator') === null){
				var creator = {
					"firstName":"",
					"lastName":""
				};
				currentExecution.set('creator', creator);
			}
			var execWorkflowId = currentExecution.get("workflowId");
			var execWorkflow = getWorkflow(execWorkflowId);

			addinfo={"wktitle": execWorkflow.get("title"),
					 "numsteps": execWorkflow.get("steps").length
					};

			var json = currentExecution.toJSON();
			
			$.extend(json, addinfo);

			var popoverTitle = _.template($('#execution-popover').html())
			var popoverContent = _.template($('#execution-popover-content').html());
			currentExecutionEl.popover({html: true, title: popoverTitle(json), content: popoverContent(json), trigger: 'manual'});
			currentExecutionEl.popover('toggle');
		}
	}
});

