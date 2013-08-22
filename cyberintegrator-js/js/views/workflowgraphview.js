var inputAnchors = [[-0.07, 0.5, -1, 0], [-0.07, 0.25, -1, 0], [-0.07, 0.75, -1, 0]];
var outputAnchors = [[1.04, 0.5, 1, 0], [1.04, 0.25, 1, 0], [1.04, 0.75, 1, 0] ];

// TODO CMN: implement auto-layout algorithm
var WorkflowGraphView = Backbone.View.extend({
    events: {
        'dragenter .wgraph': 'handleDragEnter',
        'drop .wgraph': 'handleDrop'
    },

    //template: _.template($('#drop-template').html()),

    initialize: function() {
        this.$el.bind('dragenter', _.bind(this.handleDragEnter, this));
        this.$el.bind('dragover', _.bind(this.handleDragOver, this));
        this.$el.bind('drop', _.bind(this.handleDrop, this));
    },

    render: function(e) {
        // build the graph view the first time
        if(currentWorkflow != null) {
            var workflow = null;
            workflowCollection.each(function(model) {
                if(model.get('id') === currentWorkflow) {
                    workflow = model;
                }
            });

            //var x = 5;
            //var y = 50;
            var stepCollection = workflow.getWorkflowSteps();
            var _this = this;
            stepCollection.each(function(workflowStep) {
                //console.log("step title = "+ workflowStep.get('title') );
                var workflowTool = workflowStep.getTool();
                var toolId = workflowTool.get('id');
                //console.log("tool to add is "+toolId);
                _this.addToolToGraph(toolId, workflowStep.get('id'), x, y);
                //x = x + 200;
            });
        } 
        
        return this;
    },

    setWorkflow: function(workflowId) {
        $(this.el).empty();
        var workflow = getWorkflow(workflowId);

        if(workflow != null) {

            if(DEBUG) {
                console.log(JSON.stringify(workflow, undefined, 2));
            }
            //var x = 20;
            //var y = 50;
            var stepCollection = workflow.getSteps();
            var _this = this;
            stepCollection.each(function(workflowStep) {
                var workflowTool = workflowStep.getTool();
                var toolId = workflowTool.get('id');
                var stepId = workflowStep.get('id');
                var graphLocation = null;
                stepLocationCollection.find(function(location) {
                    if(location.get('id') === stepId) {
                        graphLocation = location;
                        return false;
                    }
                })
                _this.addToolToGraph(toolId, workflowStep.get('id'), graphLocation.getX(), graphLocation.getY());
            });

            var stepCollectionSource = workflow.getSteps();

            // Restore connections
            stepCollection.each(function(workflowStep) {
                var inputs = workflowStep.getInputs();
                for(var key in inputs) {
                    var sourceStep = null;
                    var sourceLabel = null;
                    var targetStep = workflowStep;
                    var targetLabel = null;

                    var workflowToolInputCollection = workflowStep.getTool().getInputs();
                    var workflowToolInput = null;
                    workflowToolInputCollection.each(function(workflowToolData) {
                        if(workflowToolData.get('dataId') === key) {
                            workflowToolInput = workflowToolData;
                            targetLabel = workflowToolInput.get('title');
                            return false;
                        }
                    });

                    var workflowToolOutput = null;
                    stepCollectionSource.each(function(sourceWorkflowStep) {
                        var outputMap = sourceWorkflowStep.getOutputs();
                        var outputDataId = null;
                        for(var outputKey in outputMap) {
                            if(outputMap[outputKey] === inputs[key]) {
                                outputDataId = outputKey;
                            }
                        }

                        var sourceOutputs = sourceWorkflowStep.getTool().getOutputs();
                        sourceOutputs.each(function(workflowToolOutputData) {
                            if(workflowToolOutputData.get('dataId') === outputDataId) {
                                workflowToolOutput = workflowToolOutputData;
                                sourceStep = sourceWorkflowStep;
                                sourceLabel = workflowToolOutput.get('title');
                                return false;
                            }
                        });
                    });

                    console.log(sourceLabel + " is connected to "+targetLabel);

                    var sourceEndpoint = null;
                    var targetEndpoint = null;
                    var shapes = $(".shape");
                    for(var i = 0; i < shapes.length; i++) {
                        if(shapes[i].id === sourceStep.get('id')) {//workflowStep.get('id')) {
                            var endpoints = jsPlumb.getEndpoints(shapes[i]);
                            for(var j = 0; j < endpoints.length; j++) {
                                if(endpoints[j].overlays[0].getLabel() === sourceLabel) {
                                    sourceEndpoint = endpoints[j];
                                }
                            }
                        } else if(shapes[i].id === targetStep.get('id')) {
                            var endpoints = jsPlumb.getEndpoints(shapes[i]);
                            for(var j = 0; j < endpoints.length; j++) {
                                if(endpoints[j].overlays[0].getLabel() === targetLabel) {
                                    targetEndpoint = endpoints[j];
                                }
                            }
                        }
                    }
                    jsPlumb.connect({source: sourceEndpoint, target: targetEndpoint});
                }
            });

        }
    },

    createWorkflowStep: function(toolId) {
        var workflowTool = getWorkflowTool(toolId); 
        
        var stepId = generateUUID();
        var title = workflowTool.get('title');
        var date = new Date();
        var creator = null; 

        var workflow = null;
        console.log("current workflow is: "+currentWorkflow);
        workflowCollection.each(function(model) {
            if(model.get('id') === currentWorkflow) {
                workflow = model;
            }
        });

        // POJO's can only appear once in JSON so other instances should just include a reference
        if(workflow.getCreator().get('id') === currentUser.get('id')) {
            creator = currentUser.get('id');
        } else {
            creator = currentUser;
        }
        currentUser;

        // Create new input/output table
        var inputs = new Object();
        var outputs = new Object();
        var parameters = new Object();

        workflowTool.getOutputs().each(function(workflowToolData) {
            outputs[workflowToolData.get('dataId')] = generateUUID();
        });

        workflowTool.getParameters().each(function(workflowToolParameter) {
            parameters[workflowToolParameter.get('parameterId')] = generateUUID();
        });

        var workflowStep = new WorkflowStep({id: stepId, title: title, createDate: date, creator: creator, tool: workflowTool, inputs: inputs, outputs: outputs, parameters: parameters});

        var stepCollection = workflow.getSteps();
        stepCollection.add(workflowStep);
        workflow.set({steps: stepCollection});

        if(DEBUG) {
            console.log("workflow is: "+JSON.stringify(workflow, undefined, 2));
        }

        workflow.save();

        return stepId;
    },

    addToolToGraph: function(toolId, stepId, x, y) {
        //console.log("x = " +x + ", y = "+y);
        var workflowTool = null;
        workflowToolCollection.each(function(model) {
            if(model.get('id') === toolId) {
                workflowTool = model;
                return false;
            }
        });

         // TODO: CMN fix graph objects to get width/height dynamically instead of using static values from CSS
        //var myapp = $("#editor-app");

        var id = stepId;
        var innerText = workflowTool.get('title');
        var shapeClass = "shape";
        var dataShapeClass = "Rectangle";
        var divTag = document.createElement("div");

        divTag.id = id;
        divTag.setAttribute("class", shapeClass);
        divTag.setAttribute("data-shape", dataShapeClass);
        divTag.innerText = innerText;
        divTag.style.position = "absolute";
        divTag.style.left = x;
        divTag.style.top = y;

        $('#wgraph').append(divTag);

        //var shapes = $(".shape");

        // make everything draggable
        jsPlumb.draggable($('#'+stepId));
        //jsPlumb.draggable(shapes);

        // update the step location when drag stops
        $('#'+stepId).bind('dragstop', handleDragStop);

        // Add input endpoints
        var inputs = workflowTool.getInputs();

        var index = 0;

        //targetEndpoint.overlays[0][1].label
        inputs.each(function(workflowToolData) {
            //console.log(JSON.stringify(workflowToolData, undefined, 2));
            var endpoint = jQuery.extend(true, {}, targetEndpoint);

            var title = workflowToolData.get('title');
            var xLocation = -0.75 * (title.length / 4.0);

            endpoint.overlays[0][1].location[0] = xLocation;
            endpoint.overlays[0][1].label = title; 

            jsPlumb.addEndpoint(id, { anchor: inputAnchors[index], beforeDrop: handleConnect }, endpoint);  
            index++;
        });

        // Add output endpoints
        var outputs = workflowTool.getOutputs();
        index = 0;
        outputs.each(function(workflowToolData) {
            var endpoint = jQuery.extend(true, {}, sourceEndpoint);
            var title = workflowToolData.get('title');
            var xLocation = 0.85 * (title.length / 4.0);
            endpoint.overlays[0][1].location[0] = xLocation;
            endpoint.overlays[0][1].label = title;
            jsPlumb.addEndpoint(id, { anchor: outputAnchors[index], beforeDetach: handleDisconnect }, endpoint); 
            index++;
        });
        var del = jQuery.extend(true, {}, deleteEndpoint);
        jsPlumb.addEndpoint(id, { anchor: "TopRight" }, del);

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

        if(toolDrop) {
            var toolId = e.originalEvent.dataTransfer.getData('Text');
            var workflowTool = null;
            workflowToolCollection.each(function(model) {
                if(model.get('id') === toolId) {
                    workflowTool = model;
                }
            });
            var x = e.originalEvent.offsetX - 62 + 'px';
            var y = e.originalEvent.offsetY - 32 + 'px';
            var stepId = this.createWorkflowStep(workflowTool.get('id'));

            var graphLocation = new GraphStepLocation({id: stepId, x: x, y: y});
            stepLocationCollection.create(graphLocation);
            this.addToolToGraph(toolId, stepId, x, y);

            toolDrop = false;
        }
    },

});

var handleDragStop = function(e) {
    var div = e.currentTarget;
    var stepId = div.id;
    var x = div.style.left
    var y = div.style.top

    stepLocationCollection.each(function(location) {
        if(location.get('id') === stepId) {
            location.save({x: x, y: y});
            return false;
        }
    });
}

var mouseClick = function() {
    console.log("mouse click");
    return true;
}

// Handles connecting steps 
var handleConnect = function(connection) {
    var sourceStepId = connection.sourceId;
    var targetStepId = connection.targetId;

    var workflow = getWorkflow(currentWorkflow);
    var workflowSteps = workflow.getSteps();
    var sourceStep = null;
    var targetStep = null;
    workflowSteps.each(function(workflowStep) {
        if(workflowStep.get('id') === sourceStepId) {
            sourceStep = workflowStep;
        } else if(workflowStep.get('id') === targetStepId) {
            targetStep = workflowStep;
        }
    })

    var sourceEndpoint = connection.connection.endpoints[0];
    var targetEndpoint = connection.dropEndpoint;

    console.log("connect "+sourceEndpoint.overlays[0].getLabel() + " to " + targetEndpoint.overlays[0].getLabel());

    var workflowTool = sourceStep.getTool();
    var workflowToolOutputs = workflowTool.getOutputs();
    var workflowToolData = null;

    workflowToolOutputs.each(function(workflowToolOutput) {
        if(workflowToolOutput.get('title') === sourceEndpoint.overlays[0].getLabel()) {
            workflowToolData = workflowToolOutput;
            return false;
        }
    });                

    var outputDataId = workflowToolData.get('dataId');
    var outputUUID = null;
    var stepOutputMap = sourceStep.getOutputs();
    for(var key in stepOutputMap) {
        if(key === outputDataId) {
            outputUUID = stepOutputMap[key];
        }
    }

    //console.log("UUID of output is "+outputUUID);

    // Find the Input being connected
    var workflowToolDataInput = null;
    var workflowToolInputCollection = targetStep.getTool().getInputs();
    workflowToolInputCollection.each(function(workflowToolInput) {
        if(workflowToolInput.get('title') === targetEndpoint.overlays[0].getLabel()) {
            workflowToolDataInput = workflowToolInput;
            return false;
        }
    });

    console.log("tool input being connected is "+workflowToolDataInput.get('title'));
    targetStep.setInput(workflowToolDataInput.get('dataId'), outputUUID);

    if(DEBUG) {
        var checkInputMap = targetStep.getInputs();
        for(var key in checkInputMap) {
            console.log("key is "+key + " value is "+checkInputMap[key]);
        }
    }   

    workflow.save();

    return true;
}

// Handles disconnecting steps
var handleDisconnect = function(connection) {

    var workflow = getWorkflow(currentWorkflow);
    var workflowSteps = workflow.getSteps();

    var targetStepId = connection.targetId;
    var targetStep = null;
    workflowSteps.each(function(workflowStep) {
        if(workflowStep.get('id') === targetStepId) {
            targetStep = workflowStep;
            return false;
        }
    })

    var sourceEndpoint = connection.endpoints[0];
    var targetEndpoint = connection.endpoints[1];
    console.log("disconnect "+sourceEndpoint.overlays[0].getLabel() + " from " + targetEndpoint.overlays[0].getLabel()); 

    // Find the Input being disconnected
    var workflowToolDataInput = null;
    var workflowToolInputCollection = targetStep.getTool().getInputs();
    workflowToolInputCollection.each(function(workflowToolInput) {
        if(workflowToolInput.get('title') === targetEndpoint.overlays[0].getLabel()) {
            workflowToolDataInput = workflowToolInput;
            return false;
        }
    });

    targetStep.setInput(workflowToolDataInput.get('dataId'), null);
    workflow.save(); 

    return true;
}