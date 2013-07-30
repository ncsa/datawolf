var inputAnchors = [[-0.07, 0.5, -1, 0], [-0.07, 0.25, -1, 0], [-0.07, 0.75, -1, 0]];
var outputAnchors = [[1.04, 0.5, 1, 0], [1.04, 0.25, 1, 0], [1.04, 0.75, 1, 0] ];

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
        //console.log("render graph view");
        var exampleDropOptions = {
                tolerance:"touch",
                hoverClass:"dropHover",
                activeClass:"dragActive"
            };
            

            
            // eAIRS-CFD for demo
            /*
            jsPlumb.addEndpoint('analysis1', { anchor: inputAnchors[0] }, inputEndpoint);
            jsPlumb.addEndpoint('analysis1', { anchor: outputAnchors[0] }, inputEndpoint); 
            jsPlumb.addEndpoint('analysis1', { anchor:"RightMiddle" }, inputEndpoint); 
            jsPlumb.addEndpoint('analysis1', { anchor: outputAnchors[1] }, inputEndpoint);
            
            jsPlumb.addEndpoint('input1', {anchor: "RightMiddle" }, inputEndpoint);

            jsPlumb.addEndpoint('output1', {anchor: "LeftMiddle" }, inputEndpoint);
            jsPlumb.addEndpoint('output2', {anchor: "LeftMiddle" }, inputEndpoint);
            jsPlumb.addEndpoint('output3', {anchor: "LeftMiddle" }, inputEndpoint); 
            */

            //jsPlumb.addEndpoint('rec1', { anchor:"TopRight" }, inputEndpoint); 
            //jsPlumb.addEndpoint('rec1', { anchor:"BottomRight" }, inputEndpoint); 

            //jsPlumb.addEndpoint('rec1', { anchor: anchors[4] }, inputEndpoint); 
            //jsPlumb.addEndpoint('rec1', { anchor: "TopLeft" }, inputEndpoint);
            //jsPlumb.addEndpoint('rec1', { anchor:"TopRight" }, inputEndpoint); 
            


            //jsPlumb.connect({ source: "rec1", target: "ellipse1", anchors: ["LeftMiddle", "RightMiddle"]});
            var shapes = $(".shape");
                
            // make everything draggable
            jsPlumb.draggable(shapes);

            // loop through them and connect each one to each other one.
            /*
            for (var i = 0; i < shapes.length; i++) {
                for (var j = i + 1; j < shapes.length; j++) {                       
                    var tmp1 = shapes[i];
                    var tmp2 = shapes[j];
                    console.log("tmp1 = "+tmp1);
                    console.log("tmp2 = "+tmp2);
                    
                    jsPlumb.connect({
                        source:shapes[i],  // just pass in the current node in the selector for source 
                        target:shapes[j],
                        // here we supply a different anchor for source and for target, and we get the element's "data-shape"
                        // attribute to tell us what shape we should use, as well as, optionally, a rotation value.
                         anchors:[
                            [ "Perimeter", { shape:$(shapes[i]).attr("data-shape"), rotation:$(shapes[i]).attr("data-rotation") }],
                            [ "Perimeter", { shape:$(shapes[j]).attr( "data-shape"), rotation:$(shapes[j]).attr("data-rotation") }]
                        ] 
                    });  
                }   
            } */
            //this.$el.html(this.template());
            //this.$el.append("<h4>hello world</h4>");
        return this;
    },

    createWorkflowStep: function(toolId) {
        var title = "toolTitle";
        var date = new Date();
        var creator = currentUser;

        // TODO: when workflow-tool-view is created, we should be able to check the collection of tools to find the match
        var tool = "some-tool";
        var workflowStep = new WorkflowStep({title: title, date: date, creator: creator, tool: tool});

        workflowStepCollection.add(workflowStep);

        console.log("# of steps = "+workflowStepCollection.length);
        console.log("date = "+date);
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
            var uri = e.originalEvent.dataTransfer.getData('Text');

            var workflowTool = null;
            workflowToolCollection.each(function(model) {
                if(model.get('id') === uri) {
                    workflowTool = model;
                }
            });
            
            // TODO: CMN fix this to get width/heigh dynamically, values are from CSS
            var x = e.originalEvent.offsetX - 62;
            var y = e.originalEvent.offsetY - 32;
            //console.log(e);
            //console.log('drop: '+JSON.stringify(data));
            var myapp = $("#editor-app");
            //console.log("find .wgraph");
            //console.log(myapp.find('.wgraph'));


            var id = "my-id" + incr;
            var innerText = workflowTool.get('title');
            incr++;
            var shapeClass = "shape";
            var dataShapeClass = "Rectangle";
            var divTag = document.createElement("div");

            divTag.id = id;
            divTag.setAttribute("class", shapeClass);
            divTag.setAttribute("data-shape", dataShapeClass);
            divTag.innerText = innerText;
            divTag.style.position = "absolute";
            divTag.style.left = x+'px';
            divTag.style.top = y+'px';

            $('#wgraph').append(divTag);

            var shapes = $(".shape");

            console.log(shapes);
            //console.log(shapes);
            //var anchors = [[1, 0.2, 1, 0], [0.8, 1, 0, 1], [0, 0.8, -1, 0], [0.2, 0, 0, -1] ]
            //var inputAnchors = [[-0.04, 0.5, -1, 0]];
            // make everything draggable
            jsPlumb.draggable(shapes);

            // Add input endpoints
            for(var index = 0; index < workflowTool.get('inputs'); index++) {
               jsPlumb.addEndpoint(id, { anchor: inputAnchors[index] }, targetEndpoint);  
            }

            // Add output endpoints
            for(var index = 0; index < workflowTool.get('outputs'); index++) {
               jsPlumb.addEndpoint(id, { anchor: outputAnchors[index] }, sourceEndpoint);  
            }

            toolDrop = false;
            this.createWorkflowStep()
        }
    }

});