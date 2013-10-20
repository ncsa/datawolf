var Execution = Backbone.Model.extend({
    urlRoot: "/executions/",
});

var ExecutionCollection = Backbone.Collection.extend({
    model: Execution,
    //localStorage: new Backbone.LocalStorage('executions'),
    url: "/executions", //"http://localhost:8080/executions",
});