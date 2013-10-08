// Global Variables
var DEBUG = false;

// Show information
var showWorkflowInfo = false;
var showToolInfo = false;

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


// Helps determine whether a connection is being made or a tool is dropped
var toolDrop = false;

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
        //el: '#wgraph'
        el: '#pane1'
    });

    workflowGraphView.render();
    jsPlumb.importDefaults({
        //HoverPaintStyle : {strokeStyle:"#ec9f2e" },
        //EndpointHoverStyle : {fillStyle:"#5C96BC" }

    });
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
            $('#workflowToolButtons').html(new WorkflowToolButtonBar().render().el);
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
                        //console.log("fetching again for id = "+workflow.get('id'));
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
        registerCloseEvent();
        registerOpenEvent();
        registerTabEvent();
        //$('#persons').html(new PersonListView({model: personCollection}).render().el);
    }

});

function registerTabEvent() {
    //console.log("register tab event");
    $("#tabs").children('li').each(function() {
        $(this).on('click', tabSelectionEvent);
    });
}

function tabSelectionEvent() {
    //var selected = this;
    //updateTabSelection(this);
    eventBus.trigger("clicked:tab", this);
}

function registerOpenEvent() {
    $(".openTab").click(function() {
        eventBus.trigger("clicked:newworkflow", null);
    });
}

function registerCloseEvent() {

    $(".closeTab").click(function () {

        //there are multiple elements which has .closeTab icon so close the tab whose close icon is clicked
        var tabContentId = $(this).parent().attr("href");
        $(this).parent().parent().remove(); //remove li of tab
        $('#tabs a:last').tab('show'); // Select first tab
        $(tabContentId).remove(); //remove respective tab content

    });
}

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
    var workflow = getWorkflow(currentWorkflow);
    var step = null;
    var workflowStepCollection = workflow.getSteps();
    workflowStepCollection.each(function(workflowStep) {
        if(workflowStep.get('id') === endpoint.elementId) {
            step = workflowStep;
            return false;
        }
    });    
    if(endpoint.overlays[0].getLabel() === 'X') {
        //alert("click on endpoint on element " + endpoint.elementId);
        
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
        console.log("workflow step is "+step.get('title'));
        var endpointLbl = endpoint.overlays[0].getLabel();
        var toolData = null;
        step.getTool().getInputs().each(function(toolInput) {
            if(toolInput.get('title') === endpointLbl) {
                toolData = toolInput;
                return false;
            }
        });

        if(toolData == null) {
            step.getTool().getOutputs().each(function(toolOutput) {
                if(toolOutput.get('title') === endpointLbl) {
                    toolData = toolOutput;
                    return false;
                }
            });
        }

        if(toolData != null) {
            console.log("found tool data");
            $('#infoview').empty();
            $('#infoview').append(new ToolDataInfoView({model: toolData}).render().el);
        } 
        //$('#infoview').append(endpoint.overlays[0].getLabel());
    }
}

var eventBus = _.extend({}, Backbone.Events);

/*
eventBus.on('clicked:newworkflow', function() {
    $('#new-workflow-content').html(new AddWorkflowView({model: new Workflow()}).render().el);
    $('#modalWorkflowView').modal('show');
});*/

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

eventBus.on('clicked:newopenworkflow', function(workflowId) {
    //currentWorkflow = workflowId;
    //workflowGraphView.setWorkflow(workflowId);
    $('.active').removeClass('active');

    // Clear out selected tab CSS before adding new active tab
    eventBus.trigger('clicked:tab', null);
    //$(this.el).addClass('highlight');

    var txt = '<li class="active"><a href="#'+workflowId +'" data-toggle="tab" class="mytab"><button class="close closeTab" type="button" >×</button><label class="canvastab-text-selected" id="lbl'+workflowId+'"></label></a></li>';
    console.log(txt);
    
    $("#tabs").append(txt);
    var divTag = document.createElement("div");

    divTag.id = workflowId;
    divTag.setAttribute("class", "tab-pane active dropzone canvas-selected");

    $("#tab-content").append(divTag);
    //$("#"+workflowId).append("I'm the new pane");    
    var graphView = new WorkflowGraphView({
        el: '#'+workflowId
    });
    graphView.render();
    graphView.setWorkflow(workflowId);
    registerCloseEvent();
    $('.active').on('click', tabSelectionEvent);
    
});

eventBus.on('clicked:newworkflow', function() {
    var workflow = new Workflow();
    var id = generateUUID();
    var title = "untitled";
    var date = new Date();
    var creator = currentUser.toJSON();
    workflow.save({title: title, created: date, creator: creator}, {
        wait: true,

        success: function(model, response) {
            console.log("workflow created - success.");
            workflowCollection.add(workflow);
            eventBus.trigger("clicked:newopenworkflow", workflow.get('id'));
            //eventBus.trigger("clicked:createworkflow", model.get('id'));//id);
        },
        error: function(model, error) {
            //console.log(JSON.stringify(model, undefined, 2));
            console.log("workflow not saved: "+error.responseText);
        }
    });
    workflow.set("title", "untitled");
});

eventBus.on('clearWorkflow', function() {
    currentWorkflow = null;
    workflowGraphView.setWorkflow(null);
});

eventBus.on('clicked:tab', function(selected) {
    $("#tabs").children('li').each(function() {
        if(this === selected) {
            console.log("selected label is "+$(this).find('label').text());
            var child = $(this).find('label');
            child.removeClass('canvastab-text-unselected');
            child.addClass('canvastab-text-selected');

        } else if(this.id === 'add-workflow') {
            // Do nothing
        } else {
            console.log("unselected label is "+$(this).find('label').text());
            var child = $(this).find('label');
            child.removeClass('canvastab-text-selected');
            child.addClass('canvastab-text-unselected');
        }
    });
});

var app = new AppRouter();

Backbone.history.start();
