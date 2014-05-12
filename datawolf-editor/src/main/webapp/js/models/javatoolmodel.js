var JavaTool = Backbone.Model.extend({
	
});

var JavaToolCollection = Backbone.Collection.extend({
	model: JavaTool,
})

var JavaToolImplementation = Backbone.Model.extend({
	setToolClassName: function(toolClassName) {
		this.set('toolClassName', toolClassName);
	},

	getToolClassName: function() {
		return this.get('toolClassName');
	}
});
