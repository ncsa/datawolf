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

    template: _.template($('#workflow-list-item').html()),

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
        var popoverContentInputs = inputTitleTemplate();
        var inputs = new WorkflowToolDataCollection(this.model.get("inputs"));

        var inputTemplate = _.template($('#workflow-tool-popover-content-inputs').html());
        inputs.each(function(workflowToolData) {
            popoverContentInputs += inputTemplate(workflowToolData.toJSON());
        });
        var content = popoverContentTop(this.model.toJSON()) + popoverContentDesc(this.model.toJSON()) + popoverContentInputs;
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

        $(this.el).popover('toggle');
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