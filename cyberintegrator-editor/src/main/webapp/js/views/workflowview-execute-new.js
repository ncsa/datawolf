var previousWorkflow = null;

var WorkflowListView = Backbone.View.extend({
	tagName: "ul",
	id: "workflowSelector",

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
		$('.highlight').removeClass('highlight');
		$(this.el).addClass('highlight');
	},

	onDoubleClick: function(e) {
		//e.preventDefault();
		//var id = this.model.get('id');

		//$('.highlight').removeClass('highlight');
		//$(this.el).addClass('highlight');
		//eventBus.trigger("clicked:newopenworkflow", id);
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
		e.preventDefault();
		var selectedWorkflow = $('#workflowSelector').find(".highlight");
		$('.highlight').removeClass('highlight');
		if(selectedWorkflow != null && selectedWorkflow.length != 0) {
			var wkid = selectedWorkflow.attr("value");
			eventBus.trigger("clicked:newopenworkflow", wkid);
		}
	},


	workflowInfo : function() {

        var selectedWorkflow = $('#workflowSelector').find(".highlight");
        // console.log(selectedWorkflow);
        if(selectedWorkflow!=null && selectedWorkflow.length !=0){
        	var wkid = selectedWorkflow.attr("value");
        	var model = getWorkflow(wkid);
			var json = model.toJSON();
			json.numExecutions = "0";//TODO numExecutions[model.get('id')];
			json.avgTime = "0.0 s"; //TODO
			var popoverTitle = _.template($('#workflow-popover').html())
			var popoverContent = _.template($('#workflow-popover-content').html());
			selectedWorkflow.popover({html: true, title: popoverTitle(model.toJSON()), content: popoverContent(json), trigger: 'manual'});

            if(previousWorkflow==null){
                selectedWorkflow.popover('toggle');
                previousWorkflow=selectedWorkflow;                
            }
            else if(selectedWorkflow.attr("value")==previousWorkflow.attr("value")){
                selectedWorkflow.popover('toggle');
                previousWorkflow=null;    
            }
            else{
                previousWorkflow.popover('toggle');
                selectedWorkflow.popover('toggle');
                previousWorkflow=selectedWorkflow;                                
            }
        }
        else{
            if(previousWorkflow !=null){
                previousWorkflow.popover('toggle');
                previousWorkflow=null;
            }
        }
	}
});


