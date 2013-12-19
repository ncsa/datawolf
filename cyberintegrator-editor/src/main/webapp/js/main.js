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
var javaToolCollection = null;

// Views
var workflowGraphView = null;
var workflowListView = null;
var commandLineBasicView = null;
var commandLineOptionView = null;
var commandLineFileView = null;
var commandLineEnvView = null;
var hpcOptionView = null;

// TODO When the PersonView is created, select the first person as current user
var currentUser = null;//new Person({firstName: "John", lastName: "Doe", email: "john.doe@ncsa.uiuc.edu"});

var numExecutions = {};
// TODO CMN - implement this to save/restore opened workflows
var openWorkflows = [];

// List of available executors
var executors = [];

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

var getPerson = function(personId) {
    var user = null;
    personCollection.each(function(person) {
        if(person.get('id') === personId) {
            user = person;
            return false;
        }
    });

    return user;
}

// Router
var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list",
    },

    list:function() {
        var id = localStorage.currentUser;
        personCollection.fetch({cache: false, success: function() {
            personCollection.each(function(person) {
                if(person.get('id') === id) {
                    currentUser = person;
                    return false;
                }
            });
            //if(personCollection.size() > 0) {
            //    currentUser = personCollection.first();
            //}
            if(currentUser == null) {
                location.replace('login.html');
            } 

            $('#current-user').text('Hello '+currentUser.get('firstName'));
            
            if(DEBUG) {
                console.log("current user: "+JSON.stringify(currentUser, undefined, 2));
            }
           
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

            // Sync dirty/destroyed models with server, then fetch           
            workflowCollection.syncDirtyAndDestroyed(); 
            workflowCollection.fetch({success: function() {
                    workflowListView = new WorkflowListView({model: workflowCollection});
                    $('#workflows').html(workflowListView.render().el);
                    $('#workflowbuttons').html(new WorkflowButtonView().render().el);
            }});

            jsPlumb.bind("endpointClick", handleEndpointClick);

            //openWorkflows = JSON.parse(localStorage["openWorkflows"]);
            //localStorage["openWorkflows"] = JSON.stringify(openWorkflows);

            stepLocationCollection.fetch();
            registerCloseEvent();
            registerOpenEvent();
            registerTabEvent();
            getExecutors();
            //$('#tool-modal-content').html(new CommandLineView().render().el);
        }});
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

var getExecutors = function() {
    var myurl = '/executors';
    $.ajax({
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Content-type", "application/json");
            request.setRequestHeader("Accept", "application/json");
        },
        url: myurl,//'http://localhost:8001/workflows/',
        dataType: "text",
        //data: ,//JSON.stringify(workflowId)+'/executions',

        success: function(msg) {
            //var test = new WorkflowCollection(msg);
            var obj = JSON.parse(msg);
            //console.log(obj[0]);
            for(var index = 0; index < obj.length; index++) {
                executors[index] = obj[index];
            }

            localStorage["executors"] = JSON.stringify(executors); 
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    });
}

var getExecutions = function(workflowId) {
    var myurl = '/workflows/'+workflowId + '/executions';
    $.ajax({
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Content-type", "application/json");
            request.setRequestHeader("Accept", "application/json");
        },
        url: myurl,//'http://localhost:8001/workflows/',
        dataType: "text",
        //data: ,//JSON.stringify(workflowId)+'/executions',

        success: function(msg) {
            var obj = JSON.parse(msg);
            numExecutions[workflowId] = obj.length;
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    }); 
};

var addWorkflowTool = function(toolId) {
    $.ajax({
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Content-type", "application/json");
            request.setRequestHeader("Accept", "application/json");
        },
        url: '/workflowtools/'+toolId,
        dataType: "text",

        success: function(msg) {
            var workflowTool = new WorkflowTool(JSON.parse(msg));
            workflowToolCollection.add(workflowTool);
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
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

var postTool = function(zip) {
    console.log('post tool');
    var blob = zip.generate({type:"blob"});
    var data = new FormData();
    data.append('tool', blob);
    var oReq = new XMLHttpRequest();
    oReq.open("POST", "/workflowtools");
    oReq.onreadystatechange = function() {
        if (oReq.readyState == 4 && oReq.status == 200 ) {
            var tool = JSON.parse(this.responseText);
            addWorkflowTool(tool[0]);
        } 
    }
    oReq.send(data);
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

eventBus.on('clicked:saveworkflow', function(workflowId, title) {
    console.log("title = "+title);
    getWorkflow(workflowId).save({title: title}, {
        wait: true,

        success: function(model, response) {
            console.log("workflow updated - success.");
            $('label[id=lbl'+workflowId+ ']').text(title);
        },
        error: function(model, error) {
                //console.log(JSON.stringify(model, undefined, 2));
                console.log("workflow not saved: "+error.responseText);
        }
    });
    
});

eventBus.on('clicked:newopenworkflow', function(workflowId) {
    // Check if workflow is already displayed in a tab
    if($('#'+workflowId).length === 0) {
        $('.active').removeClass('active');
        var divLink = '<li class="active"><a href="#'+workflowId +'" data-toggle="tab" class="mytab"><button class="close closeTab" type="button" >×</button><label class="canvastab-text-selected" id="lbl'+workflowId+'"></label></a></li>';
        
        $("#tabs").append(divLink);
        var divTag = document.createElement("div");

        divTag.id = workflowId;
        divTag.setAttribute("class", "tab-pane active dropzone canvas-selected");

        $("#tab-content").append(divTag);
        var graphView = new WorkflowGraphView({
            el: '#'+workflowId
        });
        graphView.render();
        graphView.setWorkflow(workflowId);
        registerCloseEvent();
        $('.active').on('click', tabSelectionEvent);
        eventBus.trigger('clicked:tab', $('.active')[0]);
        currentWorkflow = workflowId; 
    } else {
        // Workflow already open, show the tab
        $('#tabs a[href="#'+workflowId+'"]').tab('show');
        eventBus.trigger('clicked:tab', $('#tabs a[href="#'+workflowId+'"]').parent()[0]);
    }
});

eventBus.on('clicked:newworkflow', function() {
    var workflow = new Workflow();
    // let the server set the id
    //var id = generateUUID();
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
    if(selected.nodeName === 'LI') {
        $("#tabs").children('li').each(function() {
            if(this.id === 'add-workflow') {
                // Do nothing
            } else if(this === selected) {
                //console.log("selected label is "+$(this).find('label').text());
                var child = $(this).find('label');
                child.removeClass('canvastab-text-unselected');
                child.addClass('canvastab-text-selected');

                // Set selected tab as current workflow
                var lblId = child.attr('id');
                currentWorkflow = lblId.substring(3, lblId.length);

            } else {
                //console.log("unselected label is "+$(this).find('label').text());
                var child = $(this).find('label');
                child.removeClass('canvastab-text-selected');
                child.addClass('canvastab-text-unselected');
            }
        });
    }
});

function selectTab(tabName) {
    console.log("select tab at index: "+$(tabName).index());
    $("#tab-content").tabs("option", "active", $(tabName).index());
}

var app = new AppRouter();

Backbone.history.start();
