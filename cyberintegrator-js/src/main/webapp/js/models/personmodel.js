var Person = Backbone.Model.extend({
    urlRoot: '/persons/'
});

var PersonCollection = Backbone.Collection.extend({
    model: Person,
    url: '/persons/'
});
