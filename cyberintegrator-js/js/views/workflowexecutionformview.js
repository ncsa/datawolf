var WorkflowExecutionView = Backbone.View.extend({
	tagName: "ol",
	className: 'cbi-execlist',

	template: _.template($('#submission-header').html()),

	initialize: function() {
		this.$el.attr('size', '10');
	},

	render: function(e) {
		$(this.el).empty();
		$(this.el).html(this.template(this.model.attributes));
		var num=0;
		_.each(this.model.attributes.steps, function(step) {
			num = num+1;
			step["num"]=num;
			var newstep=new WorkflowExecutionStepView({model: step});
			$(this.el).append(newstep.render().el);
		}, this);
		$(this.el).append(new WorkflowSubmitButtonView().render().el);

		return this;
	},

});

var WorkflowExecutionStepView = Backbone.View.extend({
	tagName: "li",
	className: 'cbi-execstep',

	template: _.template($('#step-list-item').html()),

	attributes: function() {
		return {
			value: this.model.title
		}
	},

	render: function(e) {
		$(this.el).html(this.template(this.model));
		$(this.el).append(new WorkflowExecutionParamListView({model: this.model}).render().el);
		$(this.el).append(new WorkflowExecutionDatasetListView({model: this.model}).render().el);
		return this;
	}
});


var WorkflowExecutionParamListView = Backbone.View.extend({
	tagName: "ol",

	className: 'cbi-execparamlist',

	initialize: function() {
		this.$el.attr('size', '10');		
	},

	render: function(e) {
		$(this.el).empty();

		$(this.el).append("Parameters:");
		_.each(_.keys(this.model.parameters), function(paramkey) {
			var parameterInputBoxId = this.model.parameters[paramkey];
			var paraminfo = getBy('parameterId', paramkey, this.model.tool.parameters);			

			if(paraminfo !== null && paraminfo.hidden === false){
				var wepv = new WorkflowExecutionParamView({model: paraminfo});
				wepv.inputboxId=parameterInputBoxId;
				$(this.el).append(wepv.render().el);
			}
		}, this);

		if($(this.el).children().length === 0){
			$(this.el).append(" no parameters to set");
		}

		return this;
	},

});


var WorkflowExecutionParamView = Backbone.View.extend({
	tagName: "li",
	className: 'cbi-execparam',

	template: _.template($('#param-list-item').html()),

    attributes: function() {
		return {
			value: this.model.title
		}
	},

	render: function(e) {
		$(this.el).html(this.template(this));
		return this;
	}
});



var WorkflowExecutionDatasetListView = Backbone.View.extend({
	tagName: "ol",

	className: 'cbi-execdatasetlist',

	initialize: function() {
		this.$el.attr('size', '10');
	},

	render: function(e) {
		$(this.el).empty();

		$(this.el).append("Inputs:");
		_.each(_.keys(this.model.inputs), function(inputkey) {
			var datasetElementId = this.model.inputs[inputkey];

			if(!isInputConnected(inputkey)){
				var datasetInfo = getBy('dataId', inputkey, this.model.tool.inputs);			

				if(datasetInfo !== null){
					var wedsv = new WorkflowExecutionDatasetView({model: datasetInfo});
					wedsv.datasetElementId=datasetElementId;
					$(this.el).append(wedsv.render().el);

				}
			}
		}, this);

		if($(this.el).children().length === 0){
			$(this.el).append(" no inputs to set");
		}

		return this;
	},

});



var WorkflowExecutionDatasetView = Backbone.View.extend({
	tagName: "li",
	className: 'cbi-execdataset',

	events: {
        'dragenter .cbi-execdataset': 'handleDragEnter',
        'drop .cbi-execdataset': 'handleDrop'
    },


	template: _.template($('#dataset-select-item').html()),

	initialize: function() {
        this.$el.bind('dragenter', _.bind(this.handleDragEnter, this));
        this.$el.bind('dragover', _.bind(this.handleDragOver, this));
        this.$el.bind('drop', _.bind(this.handleDrop, this));
    },
	
	handleDragOver: function(e) {
        e.preventDefault(); // Drop event will not fire unles you cancel default behavior.
        e.stopPropagation();
        return false;
    },

    handleDragEnter: function(e){
        e.preventDefault();
        //console.log('drag enter');
    },    

    handleDrop: function(e) {
        e.preventDefault();
    	// console.log("Dropped!!!");

        if(datasetDrop) {
            var datasetId = e.originalEvent.dataTransfer.getData('Text');
            // console.log(datasetId);
            datasetCollection.each(function(model) {
                if(model.get('id') === datasetId) {
                    currentDataset = model;
                }
            });

            var title=currentDataset.get('title');
			this.$el.children().find( "p" ).html(title);

			this.$el.find("#"+this.datasetElementId)[0].value=""+datasetId;
            datasetDrop = false;
        }
    },

	render: function(e) {
		$(this.el).html(this.template(this));
		return this;
	},
});



var WorkflowSubmitButtonView = Backbone.View.extend({
	events: {
		"click button#execute-workflow" : "submitWorkflow"
	},

	template: _.template($("#execute-workflow-button").html()),

	render: function(e) {
		// console.log('rendering submit button view');
		$(this.el).html(this.template());

		return this;
	},

	submitWorkflow: function(e) {
		// e.preventDefault();
		var parent=this.el.parentElement;
		var title=$(parent).find('.cbi-submission-title')[0].value;
		var description=$(parent).find('.cbi-submission-description')[0].value;

		getWorkflowIdFromTab(parent.id);
		return postSubmission(getWorkflowIdFromTab(parent.id), currentUser.get('id'), title, description, $(parent).find('.cbi-param-input'), $(parent).find('.cbi-dataset-selection'));

	}

});


var getWorkflowIdFromTab = function(tabId) {
	var index=tabId.lastIndexOf("-");
	var id = tabId.substring(0,index);
    return id;
}


