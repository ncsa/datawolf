// Global Variables
var DEBUG = false;

var currentWorkflow = null;

// Collections
var personCollection = new PersonCollection();
var workflowCollection = new WorkflowCollection();
var workflowStepCollection = new WorkflowStepCollection();
var workflowToolCollection = new WorkflowToolCollection();
var stepLocationCollection = new GraphStepLocationCollection();

// Views
var workflowGraphView = null;
var workflowListView = null;

// TODO When the PersonView is created, select the first person as current user
var currentUser = null;//new Person({firstName: "John", lastName: "Doe", email: "john.doe@ncsa.uiuc.edu"});

// Endpoint Types
/*
var exampleDropOptions = {
                tolerance:"touch",
                hoverClass:"dropHover",
                activeClass:"dragActive"
            }; */
var exampleDropOptions = {
                hoverClass:"hover",
                activeClass:"active"
};

// Green
//var color2 = "#316b31";
// tree moss green
//var color2 = "#659d32";
var color2 = "#3B5E2B";
// Yellow
var color3 = "#616161";

// Helps determine whether a connection is being made or a tool is dropped
var toolDrop = false;

var deleteEndpoint = {
    endpoint: ["Rectangle", {width: 15, height: 15}],
    paintStyle:{ fillStyle: "transparent", strokeStyle: color3, lineWidth: 3 },
    scope:"green dot",
    connectorStyle:{ strokeStyle:color2, lineWidth:3 },
    connector: ["Bezier", { curviness:63 } ],
    isTarget:false,
    maxConnections:1,
    dropOptions : exampleDropOptions,
    overlays:[ [ "Label", { location:[0.5, 0.5], label:"X", cssClass:"endpointTargetLabel" } ] ]
}

var targetEndpoint = {
    endpoint: ["Rectangle", {width: 15, height: 10}],
    paintStyle:{ fillStyle: "transparent", strokeStyle: color3, lineWidth: 3 },
    scope:"green dot",
    connectorStyle:{ strokeStyle:color2, lineWidth:3 },
    connector: ["Bezier", { curviness:63 } ],
    isTarget:true,
    maxConnections:1,
    dropOptions : exampleDropOptions,
    overlays:[ [ "Label", { location:[0.5, -0.5], label:"Drop", cssClass:"endpointTargetLabel" } ] ]
};

var sourceEndpoint = {
    endpoint: ["Rectangle", {width: 15, height: 10}],
    paintStyle:{ fillStyle: "transparent", strokeStyle: color3, lineWidth: 3  },
    isSource:true,
    scope:"green dot",
    connectorStyle:{ strokeStyle:color2, lineWidth:3 },
    connector: ["Bezier", { curviness:63 } ],
    isSource:true,
    maxConnections:-1,
    dragOptions : {},
    overlays:[ [ "Label", { location:[0.5, -0.5], label:"Drop", cssClass:"endpointTargetLabel" } ] ]
};

// Found on JSFiddle, temporary to give a uuid
function generateUUID() {
    var d = new Date().getTime();
    var uuid = 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
        var r = (d + Math.random()*16)%16 | 0;
        d = Math.floor(d/16);
        return (c=='x' ? r : (r&0x7|0x8)).toString(16);
    });
    return uuid;
};

/*
    This code will add a shape to the canvas, left here as an example

var AddShapeButtonView = Backbone.View.extend({
    events: {
        'click button#addshape-button' : 'addShape'
    },

    template: _.template($('#addshape-button-template').html()),

    render: function(e) {
        //console.log("render add shape button");
        $(this.el).html(this.template());

        return this;
    },

    addShape: function() {
        var myapp = $("#editor-app");
        //console.log("find .wgraph");
        //console.log(myapp.find('.wgraph'));

        var id = "my-id" + incr;
        var innerText = "Analysis"+incr;
        incr++;
        var shapeClass = "shape";
        var dataShapeClass = "Rectangle";
        var divTag = document.createElement("div");
        divTag.id = id;
        divTag.setAttribute("class", shapeClass);
        divTag.setAttribute("data-shape", dataShapeClass);
        divTag.innerText = innerText;
        
        $('#wgraph').append(divTag);

        var shapes = $(".shape");
        console.log(shapes);
        var anchors = [[1, 0.2, 1, 0], [0.8, 1, 0, 1], [0, 0.8, -1, 0], [0.2, 0, 0, -1] ]
        // make everything draggable
        jsPlumb.draggable(shapes);
        jsPlumb.addEndpoint(id, {anchor: "RightMiddle"}, inputEndpoint);        
        jsPlumb.addEndpoint(id, { anchor:"LeftMiddle" }, inputEndpoint); 

        //jsPlumb.addEndpoint(id, { anchor:"LeftMiddle" }, exampleEndpoint2); 
        //jsPlumb.connect({ source: "rec1", target: id, anchors: ["RightMiddle", "LeftMiddle"]});


    }
});  */

var resetRenderMode = function(desiredMode) {
        var newMode = jsPlumb.setRenderMode(desiredMode);
        $(".rmode").removeClass("selected");
        $(".rmode[mode='" + newMode + "']").addClass("selected");       

        $(".rmode[mode='canvas']").attr("disabled", !jsPlumb.isCanvasAvailable());
        $(".rmode[mode='svg']").attr("disabled", !jsPlumb.isSVGAvailable());
        $(".rmode[mode='vml']").attr("disabled", !jsPlumb.isVMLAvailable());

        init();
    };

var init = function() {
    workflowGraphView = new WorkflowGraphView({
        el: '#wgraph'
    });

    workflowGraphView.render();
};    

// Utility methods
var getWorkflow = function(workflowId) {
    var workflow = null;
        workflowCollection.each(function(model) {
        if(model.get('id') === workflowId) {
            workflow = model;
            return false;
        }
    });

    return workflow;
}

var getWorkflowTool = function(toolId) {
    var workflowTool;
    workflowToolCollection.each(function(model) {
            if(model.get('id') === toolId) {
                workflowTool = model;
                return false;
            }
    });

    return workflowTool;
}

// Router
var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    list:function() {

        jsPlumb.bind("ready", function() {
            // chrome fix.
            document.onselectstart = function () { return false; };             

            $(".rmode").bind("click", function() {
                var desiredMode = $(this).attr("mode");
                if (jsPlumbDemo.reset) jsPlumbDemo.reset();
                jsPlumb.reset();
                resetRenderMode(desiredMode);                   
            }); 

            resetRenderMode(jsPlumb.SVG);
        });

        //var tempToolCollection = new WorkflowToolCollection();
        workflowToolCollection.fetch({success: function() {
            //console.log("tool collection size = "+workflowToolCollection.size());
            $('#workflow-tools').html(new WorkflowToolListView({model: workflowToolCollection}).render().el);
        }});

        personCollection.fetch({success: function() {
            if(personCollection.size() > 0) {
                currentUser = personCollection.first();
            }

            if(DEBUG) {
                console.log("current user: "+JSON.stringify(currentUser, undefined, 2));
            }
        }});

        //var tmpCollection = new WorkflowCollection();
        // Fetch the collection and check for dirty/destroyed
        workflowCollection.fetch({success: function() {
            workflowCollection.syncDirtyAndDestroyed();

            // Re-fetch after sync because fetch won't pull everything if there are dirty/destroyed records
            // TODO CMN: is there a way to determine if everything was up to date? 
            workflowCollection.fetch({success: function() {
                workflowCollection.each(function(workflow) {
                    if(_.isString(workflow.get('creator'))) {
                        // Fixes a bug where not all the json for the model is returned
                        workflow.fetch();
                    }
                });
                workflowListView = new WorkflowListView({model: workflowCollection});
                $('#workflows').html(workflowListView.render().el);
                $('#workflowbuttons').html(new WorkflowButtonView().render().el);
            }});
        }});

        jsPlumb.bind("endpointClick", handleEndpointClick);

        //var workflow = null;//new Workflow({id: generateUUID()});
        //workflowCollection.each(function(w) {
        //    workflow = w;
        //});

        // fetch graph locations
        /* temp removed since no url set, it gives error trying to sync  
        */
        //stepLocationCollection.syncDirtyAndDestroyed();
        stepLocationCollection.fetch();

        //$('#persons').html(new PersonListView({model: personCollection}).render().el);
    }

});



var postWorkflow = function(workflow) {
    if(DEBUG) {
        console.log(JSON.stringify(workflow,undefined, 2));
    }
    
    $.ajax({
            type: "POST",
            beforeSend: function(request) {
                request.setRequestHeader("Content-type", "application/json");
                request.setRequestHeader("Accept", "application/json");
            },
            url: "http://localhost:8001/workflows",
            dataType: "text",
            data: JSON.stringify(workflow),

            success: function(msg) {
                
                console.log("remote workflow id="+msg);
                alert('success: '+msg);
            },
            error: function(msg) {
                alert('error: '+JSON.stringify(msg));
            }

        }); 

}

var handleEndpointClick = function(endpoint, originalEvent) {
    if(endpoint.overlays[0].getLabel() === 'X') {
        //alert("click on endpoint on element " + endpoint.elementId);
        var workflow = getWorkflow(currentWorkflow);
        var step = null;
        var workflowStepCollection = workflow.getSteps();
        workflowStepCollection.each(function(workflowStep) {
            if(workflowStep.get('id') === endpoint.elementId) {
                step = workflowStep;
                return false;
            }
        });
        var confirmDelete = confirm("delete "+step.get('title') + "?");

        if(confirmDelete) {
            // find all connections and delete them
            var outputIds = new Array();
            var workflowToolInputCollection = step.getTool().getOutputs();
            var stepOutputs = step.getOutputs();
            workflowToolInputCollection.each(function(workflowToolInput) {
                outputIds.push(stepOutputs[workflowToolInput.get('dataId')]);
                //outputIds.push(workflowToolInput.get('dataId'));
            });

            for(var i = 0; i < outputIds.length; i++) {
                //console.log("input id = "+outputIds[i]);
                // For each output, find steps that are connected to it and delete connection
                var outputId = outputIds[i];
                workflowStepCollection.each(function(workflowStep) {
                    var stepInputs = workflowStep.getInputs();
                    for(var key in stepInputs) {
                        if(stepInputs[key] === outputId) {
                            workflowStep.setInput(key, null);
                        }
                    }
                });
            }

            // remove tool div and connections/endpoints
            var tmp = document.getElementById(step.get('id'));
            if(tmp != null) {
                jsPlumb.remove(tmp);
            }
            var steps = workflow.get('steps');    
            var index = 0;
            for(var tmpIndex = 0; tmpIndex < steps.length; tmpIndex++) {
                console.log(steps[index].id);
                if(steps[tmpIndex].id === step.get('id')) {
                    index = tmpIndex;
                    break;
                }
            } 
            if(index < steps.length) {
                steps.splice(index, 1);
            }

            workflow.save({steps: steps}, {
                wait: true,

                success: function(model, response) {
                    console.log("updated workflow - success");
                    //console.log("workflow is: "+JSON.stringify(model, undefined, 2));
                },

                error: function(model, error) {
                    console.log("failed to update workflow");
                }
            });
        }
    } else {
        $('#infoview').empty();
        $('#infoview').append(endpoint.overlays[0].getLabel());
    }
}

var eventBus = _.extend({}, Backbone.Events);

eventBus.on('clicked:newworkflow', function() {
    $('#new-workflow-content').html(new AddWorkflowView({model: new Workflow()}).render().el);
    $('#modalWorkflowView').modal('show');
});

eventBus.on('clicked:createworkflow', function(workflowId) {
    console.log("create workflow: "+workflowId);
    currentWorkflow = workflowId;
    // TODO this should be something we can trigger from an update of the collection
    //workflowListView = new WorkflowListView({model: workflowCollection});
    //$('#workflows').html(workflowListView.render().el);
    workflowGraphView.setWorkflow(workflowId);

});

eventBus.on('clicked:openworkflow', function(workflowId) {
    currentWorkflow = workflowId;
    workflowGraphView.setWorkflow(workflowId);
});

eventBus.on('clicked:deleteworkflow', function() {
    // TODO this should be automatically done if we bind add/remove events to the view
    //workflowListView = new WorkflowListView({model: workflowCollection});
    //$('#workflows').html(workflowListView.render().el);
});

eventBus.on('clearWorkflow', function() {
    currentWorkflow = null;
    workflowGraphView.setWorkflow(null);
})

var app = new AppRouter();

Backbone.history.start();
