var WorkflowListView = Backbone.View.extend({
	tagName: "select",
	id: "workflowSelector",
	className: 'cbi-select',

	events: {
		"change" : "onChange"
	},

	initialize: function() {
		this.$el.attr('size', '10');
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

		//var workflow = getWorkflow(selection);
		//console.log(JSON.stringify(workflow, undefined, 2));
		$('#infoview').html(new WorkflowInfoView({model: workflow}).render().el);
		//selection.log(JSON.stringify(getWorkflow(selection), undefined, 2));
	}

});

var WorkflowListItemView = Backbone.View.extend({
	tagName: "option",

	template: _.template($('#workflow-list-item').html()),

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

		return this;
	},
	close: function() {
		$(this.el).unbind();
		$(this.el).remove();
	}
});

var WorkflowButtonView = Backbone.View.extend({
	events: {
		"click button#new-workflow" : "newWorkflow",
		"click button#delete-workflow" : "deleteWorkflow",
		"click button#open-workflow" : "openWorkflow",
		"click button#save-workflow" : "saveWorkflow",
		"click button#copy-workflow" : "copyWorkflow"
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

		var selectedWorkflow = $('#workflowSelector').val();
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
		var creator = currentUser.toJSON();
		var _this = this.model;
		this.model.save({title: title, created: date, creator: creator}, {
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
		});

		$('#modalWorkflowView').modal('hide');
	},

	cancelWorkflow: function(e) {
		e.preventDefault();
		$('#modalWorkflowView').modal('hide');
	}
});

