var dwApi = datawolfOptions.dwApi;

var TempExecution = Backbone.Model.extend({
    urlRoot: dwApi + '/dw/executions'
})

var TempWorkflow = Backbone.Model.extend({
    urlRoot: dwApi + '/dw/workflows'
})

var TempModel = Backbone.Model.extend({
})

var execution =  new TempModel();
var workflow = new TempModel();

function queryStringtoJSON(querystring) {
   var paris = querystring.split('&');
   var result = {};
   paris.forEach(function(pair){
    pair = pair.split('=');
    result[pair[0]] = pair[1];
   })

   return JSON.parse(JSON.stringify(result));
}

var loadMainView = function(executeId, querystring) {

    execution = new TempExecution({id:executeId})

    execution.fetch({data: $.param(queryStringtoJSON(querystring))});
    // execution is reassign in loadMainView, so the listen function need to be here. 
    execution.on("change", function(){
        headerView.render(); 
        workflow = new TempWorkflow({id:execution.attributes.workflowId})
        workflow.fetch({data: $.param(queryStringtoJSON(querystring))});
        workflow.on("change", function(){
            graphView.render(); 
        }) 
    });
}


var AppRouter = Backbone.Router.extend({
    routes:{
        "":"",
        ":executeId?*querystring":"executionGraph"
    },

    executionGraph: function(executeId, querystring) {
        loadMainView(executeId, querystring);
    }

});


var HeaderView = Backbone.View.extend({
    // ref: http://getbootstrap.com/examples/starter-template/
    model: execution,
    el: $('#workflow-header'),
    template: _.template($("#header-template").html()),

    initialize: function() {
        this.render();
    },

    render: function() {
        console.log(this.model.attributes);
        $(this.el).empty();
        if(execution.attributes.title){
            $(this.el).html(this.template(execution.toJSON()));
        }
        return this;
    }
})

var WorkflowExecutionGraphView = Backbone.View.extend({

    model: workflow,
    el: $('#graph'),

    initialize: function() {
        this.render();
        this.delegateEvents();
    },

    render: function() {
        $(this.el).empty();
        if(execution.attributes.title && workflow.attributes.steps){

        // this view contains 3 parts: input, tool and output. each is a for loop
        var self = this;

        // sort the step by the time start
        var stepstart = execution.attributes.stepsStart
        var unsortsteps = workflow.attributes.steps;
        var steps = unsortsteps.sort(function(a, b) { return stepstart[a.id] > stepstart[b.id]});


        var initInput = steps[0].tool.inputs;

        // index is required to place svg at the right location. 
        _.each(initInput, function(input, index) {
            var m = new Backbone.Model(input);
            var inputHistory = new InputHistoryView({
                index: index, 
                model:m
            });

            self.$el.append(inputHistory.render().el.childNodes);
        });


        _.each(steps, function(step, index) {
            var m = new Backbone.Model(step);
            var toolHistory = new ToolHistoryView({
                index: index,
                model: m
            });

            self.$el.append(toolHistory.render().el.childNodes);

        });

        var initOutput = steps[steps.length-1].tool.outputs;

        _.each(initOutput, function(output, index) {
            var m = new Backbone.Model(output);
            var outputHistory = new OutputHistoryView({
                index: index, 
                stepnumber: steps.length,
                model:m
            });

            self.$el.append(outputHistory.render().el.childNodes);
        });
    }
    return this;
}

});

var InputHistoryView = Backbone.View.extend({
    tagName:"svg", 
    template: _.template($("#input-graph-template").html()),
    
    render: function() {
        $(this.el).empty();

        var index=1;
        var topProperty = "top: " + this.index * 50 + 'px;';
        var positionProperty = "position: relative";

        var modelJson = {
            "id" : this.model.attributes.id,
            "title": this.model.attributes.title,
            "top": (this.options.index * 50),
            "toptext": (this.options.index * 50 +20)
        }

        this.$el.html(this.template(modelJson));
        this.$el.css({
         'position': positionProperty,
         'top': topProperty 
     });
        return this;
    }
})

var ToolHistoryView = Backbone.View.extend({
    template: _.template($("#execution-graph-template").html()),
    
    render: function() {
        $(this.el).empty();

        var modelJson = {
            "id" : this.model.attributes.id.split('-')[0],
            "title": this.model.attributes.title,
            "description": this.model.attributes.tool.description,
            "left": this.options.index * 150 +200,
            "loglength": this.model.attributes.tool.description.length/10 +170
        }
        $(this.el).html(this.template(modelJson));
        return this;
    }
})

var OutputHistoryView = Backbone.View.extend({
    template: _.template($("#output-graph-template").html()),
    
    render: function() {
        $(this.el).empty();

        var topProperty = "top: " + this.index * 50 + 'px;';
        var positionProperty = "position: relative";

        var modelJson = {
            "id" : this.model.attributes.id,
            "title": this.model.attributes.title,
            "top": (this.options.index * 50),
            "toptext": (this.options.index * 50 +20),
            "left" : (this.options.stepnumber *150 +180)
        }

        this.$el.html(this.template(modelJson));
        this.$el.css({
         'position': positionProperty,
         'top': topProperty 
     });
        return this;
    }
})

var headerView = new HeaderView();
var graphView = new WorkflowExecutionGraphView();

var app = new AppRouter();

Backbone.history.start();
