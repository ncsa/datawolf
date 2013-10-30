var inputAnchors = [[-0.07, 0.5, -1, 0], [-0.07, 0.25, -1, 0], [-0.07, 0.75, -1, 0]];
var outputAnchors = [[1.04, 0.5, 1, 0], [1.04, 0.25, 1, 0], [1.04, 0.75, 1, 0] ];
var endpointHoverStyle = {fillStyle:"#5C96BC"}
// Green
//var color2 = "#316b31";
// tree moss green
//var color2 = "#659d32";
var color2 = "#3B5E2B";
// Yellow
var color3 = "#616161";

var exampleDropOptions = {
                hoverClass:"hover",
                activeClass:"active"
};

var deleteEndpoint = {
    endpoint: "Dot",
    paintStyle:{ fillStyle: "#FFFFFF", radius: 7},
    scope:"green dot",
    connectorStyle:{ strokeStyle: "#D0D2D3", lineWidth: 5 },
    connector: ["Bezier", { curviness:63 } ],
    isTarget:false,
    maxConnections:1,
    dropOptions : exampleDropOptions,
    overlays:[ [ "Label", { location:[0.54, 0.55], label:"X", cssClass:"endpointTargetLabel" } ] ]
}

var targetEndpoint = {
    endpoint: "Rectangle",
    paintStyle:{ width: 16, height: 7, fillStyle: "#929497" },
    scope:"green dot",
    connectorStyle:{ strokeStyle: "#D0D2D3", lineWidth: 5 },
    connector: ["Bezier", { curviness:63 } ],
    isTarget:true,
    maxConnections:1,
    dropOptions : exampleDropOptions,
    //EndpointHoverStyle : {fillStyle:"#5C96BC" },
    hoverPaintStyle: {fillStyle: "#00ADEE"},

    overlays:[ [ "Label", { location:[0.0, 0.0], label:"Drop", cssClass:"in-output-text-default" } ] ]
};

var sourceEndpoint = {
    endpoint: "Rectangle",
    paintStyle:{ width: 16, height: 7, fillStyle: "#929497" },
    isSource:true,
    scope:"green dot",
    connectorStyle: { strokeStyle: "#D0D2D3", lineWidth: 5 },
    connector: ["Bezier", { curviness:63 } ],
    isSource:true,
    maxConnections:-1,
    hoverPaintStyle: {fillStyle: "#00ADEE"},
    dragOptions : {},
    overlays:[ [ "Label", { location:[2.0, 0.3], label:"Drop", cssClass:"in-output-text-default" } ] ]
};

//var classWorkflowId = null;

// TODO CMN: implement auto-layout algorithm
var WorkflowGraphView = Backbone.View.extend({
    events: {
        'dragenter .pane1': 'handleDragEnter',
        'drop .pane1': 'handleDrop'
    },

    //template: _.template($('#drop-template').html()),

    initialize: function() {
        this.$el.bind('dragenter', _.bind(this.handleDragEnter, this));
        this.$el.bind('dragover', _.bind(this.handleDragOver, this));
        this.$el.bind('drop', _.bind(this.handleDrop, this));
    },

    render: function(e) {
        // Add Button Bar
        $(this.el).append(new WorkflowGraphButtonBar().render().el);
        // build the graph view the first time
        /*
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
        } */
        
        return this;
    },

    setWorkflow: function(workflowId) {
        $(this.el).empty();
        var workflow = getWorkflow(workflowId);

        if(workflow != null) {
            // Sometimes creator/tools are just ids instead of entire object, this can cause issues saving the bean
            if(_.isString(workflow.get('creator'))) {
                workflow.set('creator', getPerson(workflow.get('creator')));
            }

            var index = 0;
            workflow.getSteps().each(function(workflowStep) {
                if(_.isString(workflowStep.get('tool'))) {
                    console.log("fixing tools");
                    workflow.get('steps')[index].tool = getWorkflowTool(workflowStep.get('tool')).toJSON()
                }
                index++;
            });


            this.model = workflow;
            $(this.el).append(new WorkflowGraphButtonBar({model: this.model}).render().el);
            //classWorkflowId = this.model.get('id'); 
            if(this.model.get('title') != null) {
                $('label[id=lbl'+workflowId+ ']').text(this.model.get('title'));
            } else {
                console.log("workflow has no title");
            }
            
            if(DEBUG) {
                console.log(JSON.stringify(workflow, undefined, 2));
            }
            var x = 5;
            var y = 50;
            var stepCollection = workflow.getSteps();
            var _this = this;
            stepCollection.each(function(workflowStep) {
                var workflowTool = workflowStep.getTool();
                var toolId = workflowTool.get('id');
                if(toolId == null) {
                    toolId = workflowStep.get('tool');
                }
                var stepId = workflowStep.get('id');
                var graphLocation = null;
                stepLocationCollection.find(function(location) {
                    if(location.get('id') === stepId) {
                        graphLocation = location;
                        return false;
                    }
                })

                if(graphLocation != null) {
                    _this.addToolToGraph(toolId, workflowStep.get('id'), graphLocation.getX(), graphLocation.getY());
                } else {
                    _this.addToolToGraph(toolId, workflowStep.get('id'), x+'px', y+'px');//graphLocation.getX(), graphLocation.getY());
                }
                x = x + 200;
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


                    var workflowToolInputCollection = null; // 
                    if(workflowStep.getTool().get('id') != null) {
                        workflowToolInputCollection = workflowStep.getTool().getInputs();
                    } else {
                        workflowToolInputCollection = getWorkflowTool(workflowStep.get('tool')).getInputs();
                    }
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

                        var sourceOutputs = null;
                        if(sourceWorkflowStep.getTool().get('id') != null) {
                            sourceOutputs = sourceWorkflowStep.getTool().getOutputs();
                        } else {
                            sourceOutputs = getWorkflowTool(sourceWorkflowStep.get('tool')).getOutputs();
                        }
                        sourceOutputs.each(function(workflowToolOutputData) {
                            if(workflowToolOutputData.get('dataId') === outputDataId) {
                                workflowToolOutput = workflowToolOutputData;
                                sourceStep = sourceWorkflowStep;
                                sourceLabel = workflowToolOutput.get('title');
                                return false;
                            }
                        });
                    });

                    if(DEBUG) {
                        console.log(sourceLabel + " is connected to "+targetLabel);
                    }
                       
                    var sourceEndpoint = null;
                    var targetEndpoint = null;
                    var shapes = $(".shape");
                    if(sourceStep != null) {
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
                }
            });

        } else {
            console.log("workflow null: "+workflowId);
        }
    },

    createWorkflowStep: function(toolId) {
        var workflowTool = getWorkflowTool(toolId); 
        
        var stepId = generateUUID();
        var title = workflowTool.get('title');
        var date = new Date();
        var creator = null; 

        var workflow = null;
        var workflowId = this.model.get('id');
        console.log("current workflow is: "+this.model.get('id'));
        workflowCollection.each(function(model) {
            if(model.get('id') === workflowId) {
                workflow = model;
            }
        });

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

        var workflowStep = new WorkflowStep({id: stepId, title: title, createDate: date, creator: currentUser, tool: workflowTool, inputs: inputs, outputs: outputs, parameters: parameters});

        var steps = workflow.get('steps');
        var newstep = workflowStep.toJSON();
        steps.push(newstep);

        //workflow.set({steps: steps});

        if(DEBUG) {
            console.log("workflow is: "+JSON.stringify(this.model, undefined, 2));
        }

        // Check if a tool is simply a reference ID, replace with full JSON if it is
        for(var i = 0; i < steps.length; i++) {
            if(_.isString(steps[i].tool)) {
                steps[i].tool = getWorkflowTool(steps[i].tool).toJSON();
            }
        }

        this.model.save({steps: steps}, {
            wait: true,

            success: function(model, response) {
                console.log("updated workflow - success");
                //console.log("workflow is: "+JSON.stringify(model, undefined, 2));
            },

            error: function(model, error) {
                console.log("failed to update workflow");
            }
        });

        return stepId;
    },

    addToolToGraph: function(toolId, stepId, x, y) {
        // console.log("x = " +x + ", y = "+y);
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
        var dataShapeClass = getShape(innerText); //"Rectangle";
        var divTag = document.createElement("div");

        divTag.id = id;
        divTag.setAttribute("class", shapeClass);
        divTag.setAttribute("data-shape", dataShapeClass);
        divTag.innerText = innerText;
        divTag.style.position = "absolute";
        divTag.style.left = x;
        divTag.style.top = y;
        divTag.onmousedown = mouseClick;

        //$('#wgraph').append(divTag);
        $(this.el).append(divTag);

        var shapes = $(".shape");

        // make everything draggable
        //jsPlumb.draggable($('#'+stepId));
        jsPlumb.draggable(shapes);

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
            var length = title.length;
            //console.log("length = "+title.length);
            var xLocation = -0.0023 * length * length * length + 0.063 * length * length - 0.6474 * length + 0.7278;

            endpoint.overlays[0][1].location[0] = xLocation;
            endpoint.overlays[0][1].label = title;

            jsPlumb.addEndpoint(id, { anchor: inputAnchors[index], beforeDrop: handleConnect }, endpoint);  
            index++;
            //console.log(length);
        });

        // Add output endpoints
        var outputs = workflowTool.getOutputs();
        index = 0;
        outputs.each(function(workflowToolData) {
            var endpoint = jQuery.extend(true, {}, sourceEndpoint);
            var title = workflowToolData.get('title');

            var length = title.length;
            //console.log("length = "+title.length);
            var xLocation = -0.0306 * length * length + 0.9652 * length - 4.0247;
            endpoint.overlays[0][1].location[0] = xLocation;
            endpoint.overlays[0][1].label = title;
            jsPlumb.addEndpoint(id, { anchor: outputAnchors[index], beforeDetach: handleDisconnect }, endpoint); 
            index++;
        });
        var del = jQuery.extend(true, {}, deleteEndpoint);
        jsPlumb.addEndpoint(id, { anchor: [0.90,0.25,0,0.0] }, del);
        //jsPlumb.addEndpoint(id, { anchor: "TopRight" }, del);

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

            // temp removed for url issue with rest
            var graphLocation = new GraphStepLocation({id: stepId, x: x, y: y});
            stepLocationCollection.create(graphLocation);
            this.addToolToGraph(toolId, stepId, x, y);

            //console.log("workflow has");
            //console.log(JSON.stringify(getWorkflow(currentWorkflow), undefined, 2));

            toolDrop = false;
        }
    },
});

var WorkflowGraphButtonBar = Backbone.View.extend({

    template: _.template($("#workflow-graph-button-bar").html()),

    events: {
        "click button#workflow-save-as" : "saveAs",
    },

    initialize: function() {

    },

    render: function() {
        $(this.el).html(this.template());
        return this;
    },

    saveAs : function() {
        console.log("save as");
        $('#new-workflow-content').html(new SaveWorkflowView({model: this.model}).render().el);
        $('#modalWorkflowView').modal('show');
    }

});

var getShape = function(title) {
    
    if(title === 'eAIRS File-Transfer') {
        return "Rectangle-1";
    } else if(title === 'eAIRS CFD Parameters') {
        return "Rectangle-2";
    } else if(title === 'eAIRS Results') {
        return "Rectangle-3";
    } else if(title === 'eAIRS-CFD-Tachyon-MPI') {
        return "Rectangle-4";
    } else {
        return "Rectangle-1";
    }
}

var handleDragStop = function(e) {
    var div = e.currentTarget;
    var stepId = div.id;
    var x = div.style.left
    var y = div.style.top

    var updated = false;
    stepLocationCollection.each(function(location) {
        if(location.get('id') === stepId) {
            location.save({x: x, y: y});
            updated = true;
            return false;
        }
    });

    if(!updated) {
        var graphLocation = new GraphStepLocation({id: stepId, x: x, y: y});
        stepLocationCollection.create(graphLocation);
    }
}

var mouseClick = function(e) {
    console.log("mouse click: "+e.target.id);
    var stepId = e.target.id;
    //console.log("workflow id = "+classWorkflowId);
    var workflow = getWorkflow(currentWorkflow);
    var workflowStep = null;
    workflow.getSteps().each(function(step) {
        if(step.get('id') === stepId) {
            workflowStep = step;
            return false;
        }
    });

    console.log("selected "+workflowStep.get('title'));

    
    return true;
}

// Handles connecting steps 
var handleConnect = function(connection) {
    console.log("handle connect");
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
    });

    if(_.isString(sourceStep.get('tool'))) {
        sourceStep.set('tool', getWorkflowTool(sourceStep.get('tool')));
    }

    if(_.isString(targetStep.get('tool'))) {
        targetStep.set('tool', getWorkflowTool(targetStep.get('tool')));
    }

    var sourceEndpoint = connection.connection.endpoints[0];
    var targetEndpoint = connection.dropEndpoint;

    if(DEBUG) {
        console.log("connect "+sourceEndpoint.overlays[0].getLabel() + " to " + targetEndpoint.overlays[0].getLabel());
    }

    var workflowTool = getWorkflowTool(sourceStep.get('tool').id);
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
    var tool = getWorkflowTool(targetStep.get('tool').id);
    var workflowToolDataInput = null;
    var workflowToolInputCollection = tool.getInputs();//getWorkflowTool(targetStep.getTool().get('id'));//targetStep.getTool().getInputs();
    //console.log("here5: "+JSON.stringify(tool, undefined, 2));
    //console.log(JSON.stringify(tool.get("inputs"));
    //console.log(workflowToolInputCollection.size());

    //console.log(JSON.stringify(targetStep.getTool(), undefined, 2));
    workflowToolInputCollection.each(function(workflowToolInput) {
        if(workflowToolInput.get('title') === targetEndpoint.overlays[0].getLabel()) {
            workflowToolDataInput = workflowToolInput;
            return false;
        }
    });

    if(DEBUG) {
        console.log("tool input being connected is "+workflowToolDataInput.get('title'));
    }

    targetStep.setInput(workflowToolDataInput.get('dataId'), outputUUID);

    if(DEBUG) {
        var checkInputMap = targetStep.getInputs();
        for(var key in checkInputMap) {
            console.log("key is "+key + " value is "+checkInputMap[key]);
        }
    }   

    // TODO CMN : does this just save what changed or is this a full save?
    workflow.save();

    return true;
}

// Handles disconnecting steps
var handleDisconnect = function(connection) {
    //console.log("handle disconnnect: "+classWorkflowId);
    console.log("currentworkflow: "+currentWorkflow);
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