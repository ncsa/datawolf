var WorkflowToolListView = Backbone.View.extend({
    tagName: 'ul',
    id: 'workflowListItemView',
    className: 'workflowToolView',

    initialize: function() {
       this.$el.attr('size', '25');
       //this.$el.attr('draggable', 'true');
       //this.$el.bind('dragstart', _.bind(this.handleDragStart, this));
    },

    render: function(e) {
        //console.log('render list of tools');
        $(this.el).empty();
        _.each(this.model.models, function(workflowTool) {
            $(this.el).append(new WorkflowToolListItemView({model: workflowTool}).render().el);
        }, this);

        return this;
    },

    //handleDragStart: function(e) {
        //console.log(this.model.get('title'));
      //  e.originalEvent.dataTransfer.setData("Text", "test");
    //} 


});

var WorkflowToolListItemView = Backbone.View.extend({
    tagName: 'li',

    template: _.template($('#workflow-tool-list-item').html()),

    events: {
        "click" : "onClick",
        "mouseleave" : "hideDetails"
    },
    
    initialize: function() {
       this.$el.attr('draggable', 'true');
       this.$el.bind('dragstart', _.bind(this.handleDragStart, this));
       //this.$el.bind('dragstart', _.bind(this._dragStartEvent, this));
    },

    attributes: function() {
        return {
            value: this.model.get('title')
        }
    },

    render: function(e) {
        $(this.el).html(this.template(this.model.toJSON()));
        var popoverTitle = _.template($('#workflow-popover').html())
        var popoverContentTop = _.template($('#workflow-tool-popover-content-top').html());
        var popoverContentDesc = _.template($('#workflow-tool-popover-content-description').html());

        // Get Inputs
        var inputTitleTemplate = _.template($('#workflow-tool-popover-content-input-title').html());
        var popoverContentInputs = inputTitleTemplate(null);
        var inputs = new WorkflowToolDataCollection(this.model.get("inputs"));

        var tabContentTemplate = _.template($('#workflow-tool-popover-content').html());
        inputs.each(function(workflowToolData) {
            popoverContentInputs += tabContentTemplate(workflowToolData.toJSON());
        });
        var outputTitleTemplate = _.template($('#workflow-tool-popover-content-output-title').html());
        var outputs = new WorkflowToolDataCollection(this.model.get("outputs"));
        var popoverContentOutputs = outputTitleTemplate(null);
        outputs.each(function(workflowToolData) {
            popoverContentOutputs += tabContentTemplate(workflowToolData.toJSON());
        });

        var parameterTitleTemplate = _.template($('#workflow-tool-popover-content-parameter-title').html());
        var popoverContentParameters = parameterTitleTemplate(null);
        var parameters = new WorkflowToolParameterCollection(this.model.get('parameters'));
        parameters.each(function(workflowToolParameter) {
            popoverContentParameters += tabContentTemplate(workflowToolParameter.toJSON());
        });

        var popoverContentOpen = _.template($('#workflow-tool-popover-content-open').html());
        var popoverContentClose = _.template($('#workflow-tool-popover-content-close').html());

        var modelJSON = this.model.toJSON();
        if(_.contains(executors, this.model.get('executor'))) {
            console.log("executor available");
            modelJSON.executorAvail = 'info-metadata-executor-available';
        } else {
            console.log("executor unavailable: "+this.model.get('executor'));
            modelJSON.executorAvail = 'info-metadata-executor-unavailable';
        }
        //if(this.model.executor)
        
        var content = popoverContentTop(modelJSON) + popoverContentOpen(null) + popoverContentDesc(this.model.toJSON()) 
            + popoverContentInputs + popoverContentClose(null) + popoverContentOutputs + popoverContentClose(null) 
            + popoverContentParameters + popoverContentClose(null);
        $(this.el).popover({html: true, title: popoverTitle(this.model.toJSON()), content: content, trigger: 'manual'});

        return this;
    },
    handleDragStart: function(e) {
        // Here we'll want to simply transfer the ID, not the heavyweight model
        var modelUri = this.model.get('id');
        e.originalEvent.dataTransfer.setData("Text", modelUri);
        toolDrop = true;
    },

    onClick: function(e) {
        //console.log("mouse click");
        //var selection = $('#workflowSelector').val();
        e.preventDefault();
        $('.highlight').removeClass('highlight');
        $(this.el).addClass('highlight');

        if(showToolInfo) {
            $(this.el).popover('toggle');
        }
        //$(this.el).popover('toggle');
    },

    hideDetails: function() {

    } 
       
});

var WorkflowToolInfoView = Backbone.View.extend({
    initialize: function() {

    },

    render: function() {
        return this;
    }
});

var WorkflowToolButtonBar = Backbone.View.extend({
    template: _.template($("#workflow-tool-buttons").html()),

    events: {
        "click button#workflow-tool-info-btn" : "workflowToolInfo",
    },

    render: function(e) {
        $(this.el).html(this.template());

        return this;
    },

    workflowToolInfo: function() {
        var selectedTool = $('#workflowListItemView').find(".highlight");
        if(selectedTool != null) {
            selectedTool.popover('toggle');
        }

        //showToolInfo = !showToolInfo;
    }
});