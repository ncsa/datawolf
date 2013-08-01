// Global Variables
var currentWorkflow = null;

// Collections
var workflowCollection = new WorkflowCollection();
var workflowStepCollection = new WorkflowStepCollection();
var workflowToolCollection = new WorkflowToolCollection();

// Views
var workflowGraphView = null;
var workflowListView = null;

// TODO When the PersonView is created, select the first person as current user
var currentUser = new Person({firstName: "John", lastName: "Doe", email: "john.doe@ncsa.uiuc.edu"});


// Test purposes
var incr = 0;

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

var targetEndpoint = {
    endpoint: ["Rectangle", {width: 15, height: 10}],
    paintStyle:{ fillStyle: "transparent", strokeStyle: color3, lineWidth: 3 },
    scope:"green dot",
    connectorStyle:{ strokeStyle:color2, lineWidth:3 },
    connector: ["Bezier", { curviness:63 } ],
    isTarget:true,
    maxConnections:-1,
    dropOptions : exampleDropOptions
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
    dragOptions : {}
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

        //$('#add-shape-div').html(new AddShapeButtonView().render().el);

        // This tool list should come from a rest endpoint
        var cfdTool = new WorkflowTool({ id: '1', title: "eAIRS-CFD", inputs: '1', outputs: '3'});
        var parameterTool = new WorkflowTool({ id: '2', title: "eAIRS CFD Parameters", inputs: '0', outputs: '1'});
        var fileTool = new WorkflowTool({ id: '3', title: "eAIRS File-Transfer", inputs: '2', outputs: '1'});
        var resultTool = new WorkflowTool({ id: '4', title: "eAIRS Results", inputs: '1', outputs: '1'});

        workflowToolCollection.add(cfdTool);
        workflowToolCollection.add(parameterTool);
        workflowToolCollection.add(fileTool);
        workflowToolCollection.add(resultTool);

        workflowCollection.add(new Workflow({id: generateUUID(), title: "title"}));
        
        $('#workflow-tools').html(new WorkflowToolListView({model: workflowToolCollection}).render().el);

        workflowListView = new WorkflowListView({model: workflowCollection});
        $('#workflows').html(workflowListView.render().el);
        $('#workflowbuttons').html(new WorkflowButtonView().render().el);
    }

});

var eventBus = _.extend({}, Backbone.Events);

eventBus.on('clicked:newworkflow', function() {
    console.log('eventbus triggered');
    $('#new-workflow-content').html(new AddWorkflowView().render().el);
    $('#modalWorkflowView').modal('show');
});

eventBus.on('clicked:createworkflow', function(workflowId) {
    console.log("create workflow: "+workflowId);
    currentWorkflow = workflowId;
    // TODO this should be something we can trigger from an update of the collection
    workflowListView = new WorkflowListView({model: workflowCollection});
    $('#workflows').html(workflowListView.render().el);
    workflowGraphView.setWorkflow(workflowId);

});

eventBus.on('clicked:openworkflow', function(workflowId) {
    currentWorkflow = workflowId;
    workflowGraphView.setWorkflow(workflowId);
});

eventBus.on('clicked:deleteworkflow', function() {
    // TODO this should be automatically done if we bind add/remove events to the view
    workflowListView = new WorkflowListView({model: workflowCollection});
    $('#workflows').html(workflowListView.render().el);
});

eventBus.on('clearWorkflow', function() {
    currentWorkflow = null;
    workflowGraphView.setWorkflow(null);
})

var app = new AppRouter();
Backbone.history.start();
