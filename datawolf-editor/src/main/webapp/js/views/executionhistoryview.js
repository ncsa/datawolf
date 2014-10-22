var currentExecution=null;
// var logFiles=null;

var WorkflowExecutionView = Backbone.View.extend({
	tagName: "ol",
	className: 'cbi-execlist',

	template: _.template($('#execution-header').html()),

	initialize: function() {
		this.$el.attr('size', '10');
	},

	render: function(e) {
		$(this.el).empty();
		currentExecution=this.model;
		// logFiles=getLogFiles(currentExecution.get("id"));
		// console.log("fetched log files");
		// console.log(logFiles);

		var execWorkflowId = this.model.get("workflowId");
		var execWorkflow = getWorkflow(execWorkflowId);

		this.model.set("wktitle", execWorkflow.get("title"));
		$(this.el).html(this.template(this.model.attributes));

		var num=0;
		_.each(execWorkflow.attributes.steps, function(step) {
			num = num+1;
			step["num"]=num;
			var newstep=new WorkflowExecutionStepView({model: step});
			$(this.el).append(newstep.render().el);
		}, this);

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
		// console.log(this.model);
		$(this.el).append(new WorkflowExecutionParamListView({model: this.model}).render().el);
		$(this.el).append(new WorkflowExecutionDatasetListView({model: this.model}).render().el);
		$(this.el).append(new WorkflowExecutionOutputListView({model: this.model}).render().el);
		$(this.el).append(new WorkflowExecutionLogFileView({model: {"stepid":this.model.id, "execid":currentExecution.get("id")}}).render().el);//, "logmap":logFiles
		// $(this.el).append('<a href="/executions/'+currentExecution.get("id")+'/logfiles/'+'stepid'+'" class="cbi-execlogfile"> Log file </a>');
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
				var actualvalue = currentExecution.get("parameters")[parameterInputBoxId];
				wepv.execvalue=actualvalue;//parameterInputBoxId;
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

	template: _.template($('#param-hist-list-item').html()),

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
					var dsid = currentExecution.get("datasets")[datasetElementId];
					var ds = getDataset(dsid);

					var actualvalue = undefined;
					if(ds != null) {
						actualvalue = ds.get("title");
					}
					wedsv.execvalue=actualvalue;
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

	template: _.template($('#dataset-hist-list-item').html()),

	render: function(e) {
		$(this.el).html(this.template(this));
		return this;
	},
});




var WorkflowExecutionOutputListView = Backbone.View.extend({
	tagName: "ol",

	className: 'cbi-execoutputlist',

	initialize: function() {
		this.$el.attr('size', '10');
	},

    attributes: function() {
		return {
			id: this.model.id,
		}
	},

	render: function(e) {
		$(this.el).empty();

		$(this.el).append("Generated outputs:");
		_.each(_.keys(this.model.outputs), function(outputkey) {
			var outputElementId = this.model.outputs[outputkey];
			var outputInfo = getBy('dataId', outputkey, this.model.tool.outputs);			
			if(outputInfo !== null){
				var wedsv = new WorkflowExecutionOutputView({model: outputInfo});
				var dsid = currentExecution.get("datasets")[outputElementId];
				var ds = getDataset(dsid);
				if(ds != null) {
					if (ds.get("fileDescriptors").length == 1) {
						wedsv.fileid=ds.get("fileDescriptors")[0].id;
					} else {
						// TODO CBI-490 Fix case where multiple file descriptors assigned to 1 output
						wedsv.fileid=null;
					}
				} else {
					wedsv.fileid = null;
				}
				wedsv.execvalue=dsid;
				wedsv.datasetElementId=outputElementId;
				$(this.el).append(wedsv.render().el);
			}

		}, this);

		if($(this.el).children().length === 0){
			$(this.el).append(" no outputs generated");
		}

		return this;
	},

});


var WorkflowExecutionOutputView = Backbone.View.extend({
	tagName: "li",
	className: 'cbi-execoutput',

	template: _.template($('#output-hist-list-item').html()),

	render: function(e) {
		$(this.el).html(this.template(this));
		return this;
	},
});

var WorkflowExecutionLogFileView = Backbone.View.extend({
	tagName: "li",
	className: 'cbi-execlogfile',

	template: _.template($('#steplog-hist-list-item').html()),

	render: function(e) {
		// console.log(this.model);
		$(this.el).html(this.template(this));
		return this;
	},
});

var WorkflowSubmitButtonView = Backbone.View.extend({
	events: {
		"click button#execute-workflow" : "submitWorkflow"
	},

	template: _.template($("#resubmit-execution-button").html()),

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


