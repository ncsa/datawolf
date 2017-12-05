// Global Variables
var DEBUG = false;

// Show information
var currentExecution = null;
var currentExecutionEl = null;

var numTabs = 0;

var activeTabExecutionId = null;

// Collections
var executionCollection = new ExecutionCollection();
var workflowCollection = new WorkflowCollection();
var datasetCollection = new DatasetCollection();
var personCollection = new PersonCollection();

// Views
var executionListView = null;
var executionButtonView = null;

var currentUser = null;

var isInputConnected = function(inputKey){
    return ((inputOutputMap[inputKey] !== undefined));
}

var buildInputOutputMap = function(wf){
    var outputMap = new Object();
    var inputMap = new Object();
    if(wf!==null){
        _.each(wf.attributes.steps, function(step) {
            _.each(_.keys(step.outputs), function(outputkey) {
                outputMap[step.outputs[outputkey]]=outputkey;
            });
            _.each(_.keys(step.inputs), function(inputkey) {
                inputMap[step.inputs[inputkey]]=inputkey;
            });
        });
    }

    var map = new Object();
    _.each(_.keys(inputMap),function(key){
        if(outputMap[key] !== undefined){
            map[inputMap[key]]=outputMap[key];
        }
    });

    return map;
}

// Utility methods
var getExecution = function(executionId) {
    var execution = null;
        executionCollection.each(function(model) {
        if(model.get('id') === executionId) {
            execution = model;
            return false;
        }
    });

    return execution;
}

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

var getDataset = function(datasetId) {
    var dataset = null;
    datasetCollection.each(function(model) {
        if(model.get('id') === datasetId) {
            dataset = model;
            return false;
        }
    });
    return dataset;
}

var getStep = function(workflow, stepid){
    var steps=workflow.attributes.steps;
    var step=null;
    _.each(steps,function(steptest) {
        if(steptest["id"] === stepid) {
            step = steptest;
            return false;
        }
    });
    return step;
}

var getLogFiles = function(execid, stepid, logdiv) {
    var myurl = datawolfOptions.rest + '/executions/'+execid+"/logfiles";
    $.ajax({
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Content-type", "application/json");
                request.setRequestHeader("Accept", "application/json");
            },
            url: myurl,
            dataType: "text",

            success: function(msg) {
                var obj = JSON.parse(msg);

                if(obj.length > 0) {
                    // Find the log entry for the step
                    var logEntry = _.findWhere(obj, {'stepId' : stepid});

                    // Get the file descriptor section of the log entry
                    var log = logEntry.log;
                    var logId = log['id'];

                    $(logdiv).html('<a href=' + '"' + datawolfOptions.rest + '/files/' + logId + '/file' + '" target="dwlog" >' + "Log file" + '</a>');
                } else {
                    $(logdiv).html('Log file - None');
                    console.log("No log file found. Check the datawolf.properties file if you expected a log file.");
                }
                return obj;
            },
            error: function(msg) {
                alert('error: '+JSON.stringify(msg));
            }

        });
}

// Router
var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    list:function() {

        var id = localStorage.currentUser;
        var personEndpoint = datawolfOptions.rest + '/persons/'+id;
        $.ajax({
            type: "GET",
            beforeSend: function(request) {
                request.setRequestHeader("Accept", "application/json");
            },
            url: personEndpoint,
            dataType: "text",

            success: function(msg) {

                currentUser = new Person(JSON.parse(msg));
                if(currentUser == null) {
                    location.replace('login.html');
                }

                $('#current-user').text('Hello '+currentUser.get('firstName'));

                workflowCollection.fetch({success: function() {
                    // console.log("workflows fetched");
                }});
                datasetCollection.fetch({success: function() {
                    // console.log("datasets fetched");
                }});
                executionCollection.fetch({success: function() {
                    executionListView = new ExecutionListView({model: executionCollection});
                    $('#executions').html(executionListView.render().el);
                    $('#executionbuttons').html(new ExecutionButtonView().render().el);
                 }});
            }
        });
    }

});

function registerTabEvent() {
    $("#tabs").children('li').each(function() {
        $(this).on('click', tabSelectionEvent);
    });
}

function tabSelectionEvent() {
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

var getBy = function(field, val, all_elements) {
    var found_element = null;
    _.each(all_elements,function(test_element) {
        if(test_element[field] === val) {
            found_element = test_element;
            return false;
        }
    });
    return found_element;
};

var eventBus = _.extend({}, Backbone.Events);

eventBus.on('clicked:newopenexecution', function(executionId) {
    numTabs=numTabs+1;

    $('.active').removeClass('active');

    // Clear out selected tab CSS before adding new active tab
    eventBus.trigger('clicked:tab', null);

    var ce = getExecution(executionId);

    var tagid = executionId+"-"+numTabs;

    var txt = '<li class="active"><a href="#'+tagid +'" data-toggle="tab" class="mytab"><button class="close closeTab" type="button" >×</button><label class="canvastab-text-selected" id="lbl'+tagid+'">'+ce.get('title')+'</label></a></li>';
    
    $("#tabs").append(txt);
    var divTag = document.createElement("div");

    divTag.id = tagid;

    divTag.setAttribute("class", "tab-pane active dropzone canvas-selected");

    $("#tab-content").append(divTag);
    
    var cwId =  ce.get("workflowId");
    var cw = getWorkflow(cwId);    
    inputOutputMap = buildInputOutputMap(cw);

    var executionView = new WorkflowExecutionView({
        model: ce, 
        el: '#'+tagid
    });
    executionView.render();

    registerCloseEvent();
    $('.active').on('click', tabSelectionEvent);
    
});

eventBus.on('clicked:tab', function(selected) {
    $("#tabs").children('li').each(function() {
        if(this === selected) {
            var child = $(this).find('label');
            child.removeClass('canvastab-text-unselected');
            child.addClass('canvastab-text-selected');

            // Set selected tab as current workflow
            var lblId = child.attr('id');

        } else if(this.id === 'add-workflow') {
            // Do nothing
        } else {
            var child = $(this).find('label');
            child.removeClass('canvastab-text-selected');
            child.addClass('canvastab-text-unselected');
        }
    });
});

eventBus.on("clicked:updateExecutions", function(){
    executionCollection.fetch({success: function() {
        executionListView = new ExecutionListView({model: executionCollection});
        $('#executions').html(executionListView.render().el);
     }});
 
});

var getExecutionIdFromTabLabel = function(tabId) {
    var index=tabId.lastIndexOf("-");
    var id = tabId.substring(3,index);
    return id;
}

var isFinished = function(executionId) {
    var execution = null;
    executionCollection.each(function(model) {
        if(model.get('id') === executionId) {
            execution = model;
            return false;
        }
    });

    var stepStates = execution.get('stepState');
    var update = true;
    for(var key in stepStates) {
        if(stepStates[key] === 'WAITING' || stepStates[key] === 'QUEUED' || stepStates[key] === 'RUNNING') {
            update = false;
        }
    }

    return update;
}

// Check if execution is still running and should be updated
var isFinishedAndUpdated = function(executionId, status) {
    // Check execution step states
    var update = isFinished(executionId);

    // Check to see if the page has updated at least once
    status.each(function(index) {
        var stepid=status[index].id;
        // Not yet updated, update at least once
        if($(status[index]).text() === 'Unknown') {
            update = false;
        }
    });

    return update;
}

function updateActiveExecution() {
    if($(".active").find(".cbi-execution-title")[0] !== undefined){
        var execid = $(".active").find(".cbi-execution-title")[0].id;
        var els = $(".active").find(".step-status-info");
        var elstatus = els.find(".step-status");
        var elsruntime = els.find(".step-runtime");

        if(!isFinishedAndUpdated(execid, elstatus)) {
            // TODO - why not fetch just the single execution?
            executionCollection.fetch({success: function() {
                datasetCollection.fetch({success: function() {
                    // Update cancel button
                    document.getElementById("cancel-execution-btn").disabled = isFinished(execid);

                    var exec = getExecution(execid);
                    var wkid = exec.get("workflowId");
                    var wk = getWorkflow(wkid);

                    var steps = $(".active").find(".cbi-execstep");

                    var stepstats = exec.get("stepState");
                    var starttimes = exec.get("stepsStart"); 
                    var endtimes = exec.get("stepsEnd"); 

                    // Update the Runtimes
                    elstatus.each(function(index) {
                        var stepid=elstatus[index].id;
                        var stat = stepstats[stepid];
                        var st = starttimes[stepid];
                        var et = endtimes[stepid];
                        var runtime = "N/A";
                        if(st !== undefined && et !== undefined){
                            runtime = (et-st) + "";
                        }
                        $(elstatus[index]).html('<b>'+stat+'</b>');
                        if(stat === "ABORTED"){
                            $(elstatus[index]).css( "color", "red" );
                        }
                        else if(stat === "FINISHED"){
                            $(elstatus[index]).css( "color", "green" );
                        }
                        else{
                            $(elstatus[index]).css( "color", "#395763" );
                        }
                        $(elsruntime[index]).html(runtime);
                    });

                    // Update the output links
                    steps.each(function(index) {

                        var stepel = steps[index];
                        var outlist = $(stepel).find(".cbi-execoutputlist")[0];
                        var stepid = outlist.id;
                        var step = getStep(wk, stepid);
                        var stepoutputs = step.outputs;
                        var outitems = $(outlist).find(".cbi-execoutput");
                        var outputlog = $(stepel).find(".cbi-execlogfile");

                        // Update log file links
                        var steplog = outputlog[0];

                        // Obtain log file from execution 
                        getLogFiles(exec.get('id'), stepid, steplog);

                        var index = 0;
                        _.each(_.keys(stepoutputs), function(outputkey) {
                            var outputElementId = stepoutputs[outputkey];
                            var outputInfo = getBy('dataId', outputkey, step.tool.outputs);
                            if(outputInfo !== null){
                                var dsid = exec.get("datasets")[outputElementId];
                                var ds = getDataset(dsid);
                                var actualvalue = dsid;
                                var item=outitems[index];

                                if (actualvalue === 'ERROR') {
                                    $(item).html(outputInfo.title+' <font color="red">(error)</font>');
                                }
                                else if ( actualvalue === undefined) {
                                    $(item).html(outputInfo.title+' (unfinished)');
                                } else if(ds != null) {  
                                    if ( ds.get("fileDescriptors").length === 1 ) {
                                        $(item).html('<a href=' + '"' + datawolfOptions.rest + '/datasets/'+actualvalue+'/'+ds.get("fileDescriptors")[0].id+'/file">'+outputInfo.title+'</a>');
                                    }
                                    else {
                                        $(item).html('<a href=' + '"' + datawolfOptions.rest + '/datasets/'+actualvalue+'/zip">'+outputInfo.title+'</a>');
                                    }
                                }
                                index = index+1;
                            }
                        }, this);
                    });
                }});
            }});
        } 
    }
    setTimeout(updateActiveExecution, 5000);
}

updateActiveExecution();

var app = new AppRouter();

Backbone.history.start();
