var dwApi = "http://127.0.0.1:8888/datawolf";
var executeId = "16cea2e4-3386-47ad-a938-90b61a7830e4";

var getExecution = function(executeId) {
    var myurl = dwApi + '/executions/' + executeId;

    $.ajax({
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Content-type", "application/json");
            request.setRequestHeader("Accept", "application/json");
        },
        url: myurl,
        dataType: "text",

        success: function(msg) {
            return JSON.parse(msg);
            
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    }); 
};

var getWorkflow = function(workflowId) {
    var myurl = dwApi + '/workflows/'+ workflowId;

    $.ajax({
        type: "GET",
        beforeSend: function(request) {
            request.setRequestHeader("Content-type", "application/json");
            request.setRequestHeader("Accept", "application/json");
        },
        url: myurl,
        dataType: "text",

        success: function(msg) {
            return JSON.parse(msg);
            
        },
        error: function(msg) {
            alert('error: '+JSON.stringify(msg));
        }
    }); 

};

var TempExecution = Backbone.Model.extend({

})

var TempWorkflow = Backbone.Model.extend({

})

var execution =  new TempExecution(getExecution(executeId))
var workflow = new TempWorkflow(getWorkflow(fackexecution.attributes.workflowId));

var HeaderView = Backbone.View.extend({
    // ref: http://getbootstrap.com/examples/starter-template/
    model: execution,
    el: $('#workflow-header'),
    template: _.template($("#header-template").html()),


    initialize: function() {
        this.render();
        this.delegateEvents();
    },

    render: function() {
        $(this.el).empty();
        $(this.el).html(this.template(this.model.toJSON()));
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

        // this view contains 3 parts: input, tool and output. each is a for loop
        var self = this;

        // sort the step by the time start
        var stepstart = execution.attributes.stepsStart
        var unsortsteps = this.model.attributes.steps;
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
                model: m});

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

        return this;
    }

});

$(document).ready(function() {
   var headerView = new WorkflowExecutionGraphView();
   var graphView = new HeaderView();

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
            "id" : this.model.attributes.id,
            "title": this.model.attributes.title,
            "left": this.options.index * 150 +200
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
