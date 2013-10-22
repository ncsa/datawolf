var previousExecution=null;

var ExecutionListView = Backbone.View.extend({
	tagName: "ul",
	id: "executionSelector",

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


		var selectedExecution = $('#executionSelector').find(".highlight");
        // console.log(selectedExecution);
        if(selectedExecution!=null && selectedExecution.length !=0){
        	var execid = selectedExecution.attr("value");
        	var model = getExecution(execid);
        	var execWorkflowId = model.get("workflowId");
			var execWorkflow = getWorkflow(execWorkflowId);

			var json = model.toJSON();
			if(json.creator==null){
                json.creator="";
            }
			json["wktitle"] = execWorkflow.get("title");
			json["numsteps"]= execWorkflow.get("steps").length;

			var popoverTitle = _.template($('#execution-popover').html())
			var popoverContent = _.template($('#execution-popover-content').html());
			selectedExecution.popover({html: true, title: popoverTitle(model.toJSON()), content: popoverContent(json), trigger: 'manual'});

            if(previousExecution==null){
                selectedExecution.popover('toggle');
                previousExecution=selectedExecution;                
            }
            else if(selectedExecution.attr("value")==previousExecution.attr("value")){
                selectedExecution.popover('toggle');
                previousExecution=null;    
            }
            else{
                previousExecution.popover('toggle');
                selectedExecution.popover('toggle');
                previousExecution=selectedExecution;                                
            }
        }
        else{
            if(previousExecution !=null){
                previousExecution.popover('toggle');
                previousExecution=null;
            }
        }
	}
});

