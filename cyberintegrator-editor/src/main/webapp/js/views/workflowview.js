var previousWorkflow = null;

var WorkflowListView = Backbone.View.extend({
	tagName: "ul",
	id: "workflowSelector",
	//className: 'cbi-select',
	//className: "list-group cbi-list-group",
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
		console.log("selection is "+console);
		eventBus.trigger("clicked:openworkflow", selection);
		//var workflow = getWorkflow(selection);
		//console.log(JSON.stringify(workflow, undefined, 2));
		//$('#infoview').html(new WorkflowInfoView({model: workflow}).render().el);
		//selection.log(JSON.stringify(getWorkflow(selection), undefined, 2));

	}

});

var WorkflowListItemView = Backbone.View.extend({
	tagName: "li",
	//className: "list-group-item cbi-list-group-item",
	template: _.template($('#workflow-list-item').html()),
	//className: "active",

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
		//$(this.el).popover({html: true, title: popoverTitle(this.model.toJSON()), content: popoverContent(this.model.toJSON()), trigger: 'manual'});
		getExecutions(this.model.get('id'));
		return this;
	},
	close: function() {
		$(this.el).unbind();
		$(this.el).remove();
	},

	onClick: function(e) {
		var id = this.model.get('id');
		//console.log(JSON.stringify(getWorkflow(id), null, 2));
		$('.highlight').removeClass('highlight');
		$(this.el).addClass('highlight');
		if(showWorkflowInfo) {
			/*
			var json = this.model.toJSON();
			json.numExecutions = numExecutions[this.model.get('id')];
			json.avgTime = "0.0 s";
			var popoverTitle = _.template($('#workflow-popover').html())
			var popoverContent = _.template($('#workflow-popover-content').html());
			$(this.el).popover({html: true, title: popoverTitle(this.model.toJSON()), content: popoverContent(json), trigger: 'manual'});
			$(this.el).popover('toggle'); */
		}
		//$(this.el).popover('show');
		//$(this.el).popover({html: true, title: "Hello", content: '<div class="my-popover-content">world</div>'});
	},

	onDoubleClick: function(e) {
		//var selection = $('#workflowSelector').val();
		//e.preventDefault();
		//var id = this.model.get('id');
		//console.log("selection is "+id);
		//console.log("add highlight");
		//$('.highlight').removeClass('highlight');
		//$(this.el).addClass('highlight');
		//console.log($(this.el));
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
		"click button#new-workflow" : "newWorkflow",
		"click button#delete-workflow" : "deleteWorkflow",
		"click button#workflow-open-btn" : "openWorkflow",
		"click button#save-workflow" : "saveWorkflow",
		"click button#copy-workflow" : "copyWorkflow",
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
		var selectedWorkflow = $('.highlight').attr('value')
		//var selectedWorkflow = $('#workflowSelector').val();
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
			tmp.destroy({
				wait: true,
			
				success: function(model, response) {
					console.log("deleted workflow - success");
				},

				error: function(model, response) {
					console.log("deleted workflow - failed"+response);
				}

			});
			if(currentWorkflow === selectedWorkflow) {
				eventBus.trigger('clearWorkflow', null);
			}
			eventBus.trigger('clicked:deleteworkflow', null);
		}
	},

	openWorkflow: function(e) {
		e.preventDefault();
		var selectedWorkflow = $('#workflowSelector').find(".highlight");
		if(selectedWorkflow != null && selectedWorkflow.length != 0) {
			var wkid = selectedWorkflow.attr("value");
			eventBus.trigger("clicked:newopenworkflow", wkid);
		}
	},

	saveWorkflow: function(e) {
		var selection = $('#workflowSelector').val();
		var workflow = getWorkflow(selection);
		//console.log("saving workflow");
		//console.log(JSON.stringify(workflow, undefined, 2));
		//workflow.set({title: "test1" });
		//workflow.save();
		//postWorkflow(workflow);
	},

	copyWorkflow: function(e) {
		var selection = $('#workflowSelector').val();
		var	oldWorkflow = getWorkflow(selection);
		var workflowClone = oldWorkflow.clone();

		// TODO CMN: should we let user specify a title?
		//var title = workflowClone.get('title') + '(1)';
		//workflowClone.set('title', title);

		var originalCreator = oldWorkflow.getCreator();
		var newCreator = currentUser;

	    var date = new Date(); 
	    workflowClone.set('created', date);
		workflowClone.unset('id');

		var contributors = workflowClone.get('contributors');
		if(contributors == null) {
			contributors = new Object();
		}

		var sameCreator = false;
		if(originalCreator.get('email') != newCreator.get('email')) {
			contributors.push(newCreator);
			workflowClone.set('contributors', contributors);
		} else {
			sameCreator = true;
		}
		
		var steps = workflowClone.get('steps');
		var clonedSteps = new WorkflowStepCollection();

		// Update cloned steps with new ids, etc
		for(var index = 0; index < steps.length; index++) {
			var oldStep = new WorkflowStep(steps[index]);
			console.log(JSON.stringify(oldStep, undefined, 2));

			var workflowTool = oldStep.getTool();

			var inputs = new Object();
	        var outputs = new Object();
	        var parameters = new Object();

	        workflowTool.getOutputs().each(function(workflowToolData) {
	            outputs[workflowToolData.get('dataId')] = generateUUID();
	        });

	        workflowTool.getParameters().each(function(workflowToolParameter) {
	            parameters[workflowToolParameter.get('parameterId')] = generateUUID();
	        });
			
	        var stepId = generateUUID();
	        var title = oldStep.get('title');
	        var workflowStep = null;
	        if(index === 0 && !sameCreator) {
	        	workflowStep = new WorkflowStep({id: stepId, title: title, createDate: date, creator: newCreator, tool: workflowTool, inputs: inputs, outputs: outputs, parameters: parameters});
	    	} else {
	        	workflowStep = new WorkflowStep({id: stepId, title: title, createDate: date, creator: newCreator.get('id'), tool: workflowTool, inputs: inputs, outputs: outputs, parameters: parameters});
	    	}
	        clonedSteps.add(workflowStep);

	        //Update cloned step with same location information as original step
	        var graphLocation = stepLocationCollection.get(oldStep.get('id'));
	        if(graphLocation != null) {
	        	var newGraphLocation = graphLocation.clone();
	        	newGraphLocation.set('id', stepId);
	        	console.log("found graph location: "+JSON.stringify(graphLocation, undefined, 2));
	        	// TODO: uncomment this when finished with clone
	        	stepLocationCollection.add(newGraphLocation);
	        }
	    }

	    // Reconnect input/outputs
		for(var index = 0; index < steps.length; index++) {
			var oldStep = new WorkflowStep(steps[index]);
			var newIndex;
			for(var key in oldStep.getInputs()) {
				var value = oldStep.getInputs()[key];
				var toolDataInputCollection = oldStep.getTool().getInputs();

				var connectedInput = null;
				var connectedOutput = null;
				toolDataInputCollection.each(function(toolDataInput) {
					if(toolDataInput.get('dataId') === key) {
						connectedInput = toolDataInput;
						return false;
					}
				});

				for(var i = 0; i < steps.length; i++) {
					var currentStep = new WorkflowStep(steps[i]);
					var tmpTool = currentStep.getTool();
					var tmpOutputs = currentStep.getOutputs();
					for(var j in tmpOutputs) {
						if(tmpOutputs[j] === value) {	
							var dataOutputCollection = tmpTool.getOutputs();
							dataOutputCollection.each(function(workflowToolData) {
								if(workflowToolData.get('dataId') === j) {
									newIndex = i;
									connectedOutput = workflowToolData;
									return false;
								}
							});
						}
					}
				}

				var sourceStep = clonedSteps.at(newIndex);
				var outputTool = sourceStep.get('tool');
				var workflowToolOutputs = outputTool.getOutputs();
				var workflowToolData = null;

				workflowToolOutputs.each(function(workflowToolOutput) {
				    if(workflowToolOutput.get('title') === connectedOutput.get('title')) {
				        workflowToolData = workflowToolOutput;
				        return false;
				    }
			    });     

				var stepOutputMap = sourceStep.getOutputs();
				var outputDataId = workflowToolData.get('dataId');
				var outputUUID = null;
				for(var key in stepOutputMap) {
				    if(key === outputDataId) {
				        outputUUID = stepOutputMap[key];
				    }
				}

				var targetStep = clonedSteps.at(index);
				var inputTool = targetStep.get('tool');
				var inputDataCollection = inputTool.getInputs();
				var workflowToolDataInput = null;
				inputDataCollection.each(function(workflowToolInput) {
					if(workflowToolInput.get('title') === connectedInput.get('title')) {
						workflowToolDataInput = workflowToolInput;
						return false;
					}
				});
				//console.log(workflowToolData.get('title') + " is the input to "+workflowToolDataInput.get('title'));
				targetStep.setInput(workflowToolDataInput.get('dataId'), outputUUID);
			} 
		}

		workflowClone.set('steps', clonedSteps);
		//console.log(JSON.stringify(workflowClone, undefined, 2));

		// This works, just need to finish above todo's
		workflowClone.save({}, {
			wait: true,
			
			success: function(model, response) {
				console.log("copied workflow - success");
				workflowCollection.add(workflowClone);
			},

			error: function(model, response) {
				console.log("copied workflow - failed");
			}
		});
	},

	workflowInfo : function() {

        var selectedWorkflow = $('#workflowSelector').find(".highlight");
        // console.log(selectedWorkflow);
        if(selectedWorkflow!=null && selectedWorkflow.length !=0){
        	var wkid = selectedWorkflow.attr("value");
        	var model = getWorkflow(wkid);
			var json = model.toJSON();
			json.numExecutions = numExecutions[model.get('id')];
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
	},
});

var SaveWorkflowView = Backbone.View.extend({
	template: _.template($("#new-workflow").html()),

	events: {
		"click button#save-workflow" : "saveWorkflow",
		"click button#cancel" : "cancelWorkflow"
	},

	render: function(e) {
		console.log("title = "+this.model.get('title'));
		$(this.el).html(this.template());
		return this;
	},

	saveWorkflow: function(e) {
		e.preventDefault();

		// TODO add validation that the input is correct
		var title = this.$('input[name=workflow-title]').val();
		eventBus.trigger("clicked:saveworkflow", this.model.get('id'), title);
		/*
		var _this = this.model;
		this.model.save({title: title}, {
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
		}); */

		$('#modalWorkflowView').modal('hide');
	},

	cancelWorkflow: function(e) {
		e.preventDefault();
		$('#modalWorkflowView').modal('hide');
	}

	
});
