var WorkflowInfoView = Backbone.View.extend({
	
	template: _.template($('#workflow-info-view').html()),

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}
});

var WorkflowStepInfoView = Backbone.View.extend({
	template: _.template($('#workflow-step-info-view').html()),

	initialize: function() {

	},

	render: function() { 
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}
});

var ToolDataInfoView = Backbone.View.extend({
	template: _.template($('#tool-data-info-view').html()),

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}
});
