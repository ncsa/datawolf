
// key = tab id, value = inputMap
var executionInputMap = {};
// key = tab id, value = parameterList
var executionParameterMap = {};

var WorkflowExecutionView = Backbone.View.extend({
	tagName: "ol",
	className: 'cbi-execlist',

	template: _.template($('#submission-header').html()),

	initialize: function() {
		this.$el.attr('size', '10');
		this.listenTo(eventBus, 'close', this.close);
	},

	render: function(e) {
		$(this.el).empty();
		$(this.el).html(this.template(this.model.attributes));
		
		// Create parameter and input map
		executionInputMap[this.el.id] = {};
		executionParameterMap[this.el.id] = [];

		var num=0;
		_.each(this.model.attributes.steps, function(step) {
			num = num+1;
			step["num"]=num;
			var newstep=new WorkflowExecutionStepView({model: step, storageId: this.el.id});
			$(this.el).append(newstep.render().el);
		}, this);
		$(this.el).append(new WorkflowSubmitButtonView({storageId: this.el.id}).render().el);

		return this;
	},

	close: function(id) {
		if(id === this.el.id) {
			delete executionInputMap[this.el.id];
			delete executionParameterMap[this.el.id];
		}
	}
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
		$(this.el).append(new WorkflowExecutionParamListView({model: this.model, storageId: this.options.storageId}).render().el);
		$(this.el).append(new WorkflowExecutionDatasetListView({model: this.model, storageId: this.options.storageId}).render().el);
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
				
				// Add parameter to the list of checked parameters if null is not allowed
				if(!paraminfo.allowNull) {
					executionParameterMap[this.options.storageId].push(parameterInputBoxId);
				} 
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
					var wedsv = new WorkflowExecutionDatasetView({model: datasetInfo, storageId: this.options.storageId});
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
    },    

    handleDrop: function(e) {
        e.preventDefault();

        if(datasetDrop) {
            var datasetId = e.originalEvent.dataTransfer.getData('Text');
            datasetCollection.each(function(model) {
                if(model.get('id') === datasetId) {
                    currentDataset = model;
                }
            });

            var title=currentDataset.get('title');
			this.$el.children().find( "p" ).html(title);

			this.$el.find("#"+this.datasetElementId)[0].value=""+datasetId;

			executionInputMap[this.options.storageId][this.datasetElementId] = datasetId;
            datasetDrop = false;
        }
    },

	render: function(e) {
		executionInputMap[this.options.storageId][this.datasetElementId] = "";
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
		$(this.el).html(this.template());
		return this;
	},

	submitWorkflow: function(e) {
		var parent=this.el.parentElement;
		var title=$(parent).find('.cbi-submission-title')[0].value;
		if(title.trim() === "") {
			window.alert("Please specify a submission title.");
			return;
		}

		var canExecute = true;
		var inputMap = executionInputMap[this.options.storageId];
		for(var key in inputMap) {
			if(inputMap[key] === "") {
				canExecute = false;
			}
		}	

		var parameters = $(parent).find('.cbi-param-input');
		var parameterList = executionParameterMap[this.options.storageId];
		if(parameters != undefined) {
			for(var index = 0; index < parameters.length; index++) {
				var id = parameters[index].name;
				if(_.contains(parameterList, id)) {
					if(parameters[index].value === "") {
						canExecute = false;
					}
				} 
			}
		}

		if(!canExecute) {
			window.alert("Please specify all required datasets and parameters");
			return;
		}

		var description=$(parent).find('.cbi-submission-description')[0].value;

		//getWorkflowIdFromTab(parent.id);
		return postSubmission(getWorkflowIdFromTab(parent.id), currentUser.get('id'), title, description, $(parent).find('.cbi-param-input'), $(parent).find('.cbi-dataset-selection'));
	}

});

var getWorkflowIdFromTab = function(tabId) {
	var index=tabId.lastIndexOf("-");
	var id = tabId.substring(0,index);
    return id;
}
