// Global Variables
var DEBUG = false;

// Show information
var showWorkflowInfo = false;
var showDatasetInfo = false;

var currentWorkflow = null;
var currentWorkflowEl = null;
var currentDataset = null;
var currentDatasetEl = null;


var datasetDrop=false;

var inputOutputMap = null;

var numTabs = 0;


// Collections
var personCollection = new PersonCollection();
var workflowCollection = new WorkflowCollection();
var workflowStepCollection = new WorkflowStepCollection();
var workflowToolCollection = new WorkflowToolCollection();
var datasetCollection = new DatasetCollection();


// Views
var workflowListView = null;
var datasetListView=null;



// TODO
var currentUser = new Person({firstName: "John", lastName: "Doe", email: "john.doe@ncsa.uiuc.edu", id:"55"});

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

var getBy = function(field, val, all_elements) {
    var found_element = null;
    // console.log(JSON.stringify(all_elements, undefined, 2));
    _.each(all_elements,function(test_element) {
        // console.log(JSON.stringify(test_element, undefined, 2));
        if(test_element[field] === val) {
            found_element = test_element;
            return false;
        }
    });
    return found_element;
};



// Router
var AppRouter = Backbone.Router.extend({
    routes:{
        "":"list"
    },
    
    list:function() {
        datasetCollection.fetch({success: function() {
            datasetListView = new DatasetListView({model: datasetCollection});
            $('#datasets').html(datasetListView.render().el);
            $('#datasetbuttons').html(new DatasetButtonView().render().el);
         }});

        workflowCollection.fetch({success: function() {
            workflowListView = new WorkflowListView({model: workflowCollection});
            $('#workflows').html(workflowListView.render().el);
            $('#workflowbuttons').html(new WorkflowButtonView().render().el);
        }});

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

var postSubmission = function(workflowid, creatorid, title, description, parameters, datasets) {

    var submission = prepareSubmission(workflowid, creatorid, title, description, parameters, datasets);

    console.log('post submission:');
    console.log(JSON.stringify(submission,undefined, 2));
    
    $.ajax({
            type: "POST",
            beforeSend: function(request) {
                request.setRequestHeader("Content-type", "application/json");
                request.setRequestHeader("Accept", "application/json");
            },
            url: "/executions", //"http://localhost:8080/executions",
            dataType: "text",
            data: JSON.stringify(submission),

            success: function(msg) {
                console.log("remote submission id="+msg);
                alert('success: '+msg);
            },
            error: function(msg) {
                alert('error: '+JSON.stringify(msg));
            }

        }); 

}

var prepareSubmission = function(workflowid, creatorid, title, description, parameters, datasets){
    var submission = new Submission();

    submission.set('parameters', {});
    submission.set('datasets', {});
    submission.set('workflowId', workflowid);
    submission.set('creatorId', creatorid);
    submission.set('title', title);
    submission.set('description', description);
    
    //parameters        
    _.each(parameters,function(input){
        var x=submission.get('parameters');
        x[input.name]=input.value;
        submission.set('parameters',x);
    }, this);


    //datasets 
    _.each(datasets,function(input){
        var y=submission.get('datasets');        
        y[input.id]=input.value;
        submission.set('datasets',y);
    }, this);
    
    return submission;            
}


var isInputConnected = function(inputKey){
    return ((inputOutputMap[inputKey] !== undefined));
}

var buildInputOutputMap = function(wf){
    var outputMap = new Object();
    var inputMap = new Object();

    _.each(wf.attributes.steps, function(step) {
        _.each(_.keys(step.outputs), function(outputkey) {
            outputMap[step.outputs[outputkey]]=outputkey;
        });
        _.each(_.keys(step.inputs), function(inputkey) {
            inputMap[step.inputs[inputkey]]=inputkey;
        });
    });

    var map = new Object();
    _.each(_.keys(inputMap),function(key){
        if(outputMap[key] !== undefined){
            map[inputMap[key]]=outputMap[key];
        }
    });

    return map;
}



var eventBus = _.extend({}, Backbone.Events);

eventBus.on("clicked:updateDatasets", function(){
    // console.log("EVENTBUS updating datasets");
    datasetCollection.fetch({success: function() {
        datasetListView = new DatasetListView({model: datasetCollection});
        $('#datasets').html(datasetListView.render().el);
     }});
 
});


eventBus.on('clicked:newopenworkflow', function(workflowId) {
    numTabs=numTabs+1;

    $('.active').removeClass('active');

    // Clear out selected tab CSS before adding new active tab
    eventBus.trigger('clicked:tab', null);

    var cw = getWorkflow(workflowId);
    var tagid = workflowId+"-"+numTabs;

    var txt = '<li class="active"><a href="#'+tagid +'" data-toggle="tab" class="mytab"><button class="close closeTab" type="button" >×</button><label class="canvastab-text-selected" id="lbl'+tagid+'">'+cw.get('title')+'</label></a></li>';
    
    $("#tabs").append(txt);
    var divTag = document.createElement("div");

    divTag.id = tagid;

    divTag.setAttribute("class", "tab-pane active dropzone canvas-selected");

    $("#tab-content").append(divTag);
        
    inputOutputMap = buildInputOutputMap(cw);
    var workflowExecutionView = new WorkflowExecutionView({
        model: cw, 
        el: '#'+tagid//workflowId
    });
    workflowExecutionView.render();

    registerCloseEvent();
    $('.active').on('click', tabSelectionEvent);
    
});

eventBus.on('clicked:newdataset', function() {
    $('#new-dataset-content').html(new NewDatasetView().render().el);
    $('#modalDatasetView').modal('show');
});


eventBus.on('clicked:tab', function(selected) {
    $("#tabs").children('li').each(function() {
        if(this === selected) {
            var child = $(this).find('label');
            child.removeClass('canvastab-text-unselected');
            child.addClass('canvastab-text-selected');
        } else if(this.id === 'add-workflow') {
            // Do nothing
        } else {
            //console.log("unselected label is "+$(this).find('label').text());
            var child = $(this).find('label');
            child.removeClass('canvastab-text-selected');
            child.addClass('canvastab-text-unselected');
        }
    });
});

var app = new AppRouter();

Backbone.history.start();
