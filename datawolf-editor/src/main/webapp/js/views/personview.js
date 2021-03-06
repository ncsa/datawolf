var PersonListView = Backbone.View.extend({
	tagName: "select",
	id: "personSelector",
	className: 'cbi-select',

	initialize: function() {
		this.$el.attr("size", "10");

		var _this = this;
		this.model.fetch({success: function() {
			_this.render();
		}});
	},

	render: function() {
		$(this.el).empty();
		_.each(this.model.models, function(person) {
			$(this.el).append(new PersonListItemView({model: person}).render().el);
		}, this);

		return this;
	}
});

var PersonListItemView = Backbone.View.extend({
	tagName: 'option',
	template: _.template($('#person-list-item').html()),

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	}
});

var UserView = Backbone.View.extend({
	template: _.template($('#userview-template').html()),

	events: {
		"click #logout-btn" : "logout",
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	},

	logout: function(e) {
		e.preventDefault();
		// Remove current user from local storage
		localStorage.removeItem("currentUser");
		location.replace('login.html');
	}

});
