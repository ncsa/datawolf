var WorkflowToolListView = Backbone.View.extend({
    tagName: 'ul',
    id: 'workflowListItemView',

    initialize: function() {
       this.$el.attr('size', '10');
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
    tagName: 'ol',

    template: _.template($('#workflow-list-item').html()),
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

        return this;
    },
    handleDragStart: function(e) {
        // Here we'll want to simply transfer the ID, not the heavyweight model
        var modelUri = this.model.get('id');
        e.originalEvent.dataTransfer.setData("Text", modelUri);
        toolDrop = true;
    } 
       
});