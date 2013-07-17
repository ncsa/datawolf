// Global Variables

var workflowGraphView = null;


// Test purposes
var incr = 0;

// Endpoint Types
var exampleDropOptions = {
                tolerance:"touch",
                hoverClass:"dropHover",
                activeClass:"dragActive"
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

var inputEndpoint = {
    endpoint: ["Rectangle", {width: 15, height: 10}],
    paintStyle:{ fillStyle:color3 },
    isSource:true,
    scope:"green dot",
    connectorStyle:{ strokeStyle:color2, lineWidth:3 },
    connector: ["Bezier", { curviness:63 } ],
    maxConnections:3,
    isTarget:true,
    dropOptions : exampleDropOptions
};
var outputEndpoint = {
    endpoint:["Dot", {radius:7} ],
    anchor:"BottomLeft",
    paintStyle:{ fillStyle: color3, opacity:0.5 },
    isSource:true,
    scope:'blue dot',
    connectorStyle:{ strokeStyle: color3, lineWidth:4 },
    connector : ["Bezier", {curviness:63}],
    isTarget:true,
    dropOptions : exampleDropOptions
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
    var workflowGraphView = new WorkflowGraphView({
        el: '#wgraph'
    });

    workflowGraphView.render();
    //$('#render').html(workflowGraphView.render().el);

    //workflowGraphView.addShape('my-id', 'shape', 'Circle');
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
        var listVal = {title: "eAIRS Parameters"};
        var listVal1 = {title: "eAIRS File Transfer"};
        var model = [listVal, listVal1];
        
        //console.log("length is "+model.length);
        $('#workflow-tools').html(new WorkflowToolListView({model: model}).render().el);
    }

});

var eventBus = _.extend({}, Backbone.Events);

var app = new AppRouter();
Backbone.history.start();
