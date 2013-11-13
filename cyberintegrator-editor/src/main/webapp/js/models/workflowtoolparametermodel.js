var WorkflowToolParameter = Backbone.Model.extend({
	initialize: function() {
	},

	setId: function(id) {
		this.set('id', id);
	},

	setTitle: function(title) {
		this.set('title', title);
	},

	setDescription: function(description) {
		this.set('description', description);
	},

	setType: function(type) {
		this.set('type', type);
	},

	setHidden: function(hidden) {
		this.set('hidden', hidden);
	},

	setAllowNull: function(allowNull) {
		this.set('allowNull', allowNull);
	},

	setValue: function(value) {
		this.set('value', value);
	},

	setParameterId: function(parameterId) {
		this.set('parameterId', parameterId);
	},

	getParameterId: function() {
		return this.get('parameterId');
	}

});

var WorkflowToolParameterCollection = Backbone.Collection.extend({
	model: WorkflowToolParameter
});