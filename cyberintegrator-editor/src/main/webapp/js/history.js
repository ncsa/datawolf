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


// TODO
var currentUser = null;//new Person({firstName: "John", lastName: "Doe", email: "john.doe@ncsa.uiuc.edu", id:"55"});


var isInputConnected = function(inputKey){
    // console.log(inputKey +" ----- " + inputOutputMap[inputKey]);
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

var getLogFiles = function(execid) {
    var myurl = '/executions/'+execid+"/logfiles";
    console.log(myurl);
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
                console.log("log files:");
                console.log(obj);
                // for(var index = 0; index < obj.length; index++) {
                // }
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
        personCollection.fetch({success: function() {
            personCollection.each(function(person) {
                if(person.get('id') === id) {
                    currentUser = person;
                    return false;
                }
            });

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

        }});
    }

});

function registerTabEvent() {
    //console.log("register tab event");
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

    divTag.id = tagid;//workflowId;

    divTag.setAttribute("class", "tab-pane active dropzone canvas-selected");

    $("#tab-content").append(divTag);
    
    var cwId =  ce.get("workflowId");
    var cw = getWorkflow(cwId);    
    inputOutputMap = buildInputOutputMap(cw);

    var executionView = new WorkflowExecutionView({
        model: ce, 
        el: '#'+tagid//workflowId
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
    // console.log("EVENTBUS updating executions");
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

function updateExecutionStatus(){
    if($(".active").find(".cbi-execution-title")[0] !== undefined){
        var els = $(".active").find(".step-status-info");
        var elstatus = els.find(".step-status");
        var elsruntime = els.find(".step-runtime");
        var execid = $(".active").find(".cbi-execution-title")[0].id;

        executionCollection.fetch({success: function(){
            var exec = getExecution(execid);
            var stepstats = exec.get("stepStates");
            var starttimes = exec.get("stepsStart"); 
            var endtimes = exec.get("stepsEnd"); 
            
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
        }});
    }
    setTimeout(updateExecutionStatus, 1000);
}

function updateExecutionOutputs(){
    // console.log("update execution outputs");
    if($(".active").find(".cbi-execution-title")[0] !== undefined){
        var execid = $(".active").find(".cbi-execution-title")[0].id;
        

        executionCollection.fetch({success: function() {
            var exec = getExecution(execid);
            var wkid = exec.get("workflowId");
            var wk = getWorkflow(wkid);

            var steps = $(".active").find(".cbi-execstep");

            steps.each(function(index) {

                var stepel=steps[index];
                var outlist = $(stepel).find(".cbi-execoutputlist")[0];
                var stepid=outlist.id;
                var step=getStep(wk, stepid);
                var stepoutputs=step.outputs;
                var outitems = $(outlist).find(".cbi-execoutput");

                var index=0;
                _.each(_.keys(stepoutputs), function(outputkey) {
                    var outputElementId = stepoutputs[outputkey];
                    var outputInfo = getBy('dataId', outputkey, step.tool.outputs);           
                    if(outputInfo !== null){

                        var dsid = currentExecution.get("datasets")[outputElementId];
                        var ds = getDataset(dsid);
                        var actualvalue = dsid;
                        var item=outitems[index];

                        if (actualvalue === 'ERROR') {
                            $(item).html(outputInfo.title+' <font color="red">(error)</font>');
                        }
                        else if ( actualvalue === null) {
                            $(item).html(outputInfo.title+' (unfinished)');
                        }  
                        else {
                            $(item).html('<a href="/datasets/'+actualvalue+'/zip">'+outputInfo.title+'</a>');
                        }

                        index = index+1;

                    }

                }, this);

            });
            
        }});
    }
    setTimeout(updateExecutionOutputs, 2000);
}

updateExecutionStatus();
updateExecutionOutputs();

var app = new AppRouter();

Backbone.history.start();
