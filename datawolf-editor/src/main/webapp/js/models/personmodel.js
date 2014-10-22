var Person = Backbone.Model.extend({
    urlRoot: datawolfOptions.rest + '/persons/'
});

var PersonCollection = Backbone.Collection.extend({
    model: Person,
    url:  datawolfOptions.rest + '/persons/'
});
