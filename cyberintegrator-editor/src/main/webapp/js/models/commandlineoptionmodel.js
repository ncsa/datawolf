var CommandLineOption = Backbone.Model.extend({
	setType: function(type) {
		this.set('type', type);
	},

	getType: function() {
		return this.get('type');
	},

	setValue: function(value) {
		this.set('value', value);
	},

	getValue: function() {
		return this.get('value');
	},

	setFlag: function(flag) {
		this.set('flag', flag);
	},

	getFlag: function() {
		return this.get('flag');
	},

	setOptionId: function(optionId) {
		this.set('optionId', optionId);
	},

	getOptionId: function() {
		return this.get('optionId');
	},

	getInputOutput: function() {
		return this.get('inputOutput');
	},

	setInputOutput: function(inputOutput) {
		this.set('inputOutput', inputOutput);
	},

	getInputOutput: function() {
		return this.get('inputOutput');
	},

	setFilename: function(filename) {
		this.set('filename', filename);
	},

	getFilename: function() {
		return this.get('filename');
	},

	setCommandline: function(commandline) {
		this.set('commandline', commandline);
	},

	isCommandline: function() {
		return this.get('commandline');
	}
});

var CommandLineOptionCollection = Backbone.Collection.extend({
	model: CommandLineOption,

	swapElements: function(index0, index1) {
		this.models[index0] = this.models.splice(index1, 1, this.models[index0])[0];
	}
});

var CommandLineImplementation = Backbone.Model.extend({
	initialize: function() {
		this.set('commandLineOptions', []);
		this.set('env', {});
		this.set('captureStdOut', null);
		this.set('captureStdErr', null);
		this.set('joinStdOutStdErr', false);
	},

	setExecutable: function(exec) {
		this.set('executable', exec);	
	},

	setCommandLineOptions: function(commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
	},

	addCommandLineOption: function(commandLineOption) {
		this.get('commandLineOptions').push(commandLineOption);
	},

	setEnv: function(env) {
		this.set('env', env);
	},

	setCaptureStdOut: function(captureStdOut) {
		this.set('captureStdOut', captureStdOut);
	},

	setCaptureStdErr: function(captureStdErr) {
		this.set('captureStdErr', captureStdErr);
	},

	setJoinStdOutStdErr: function(joinStdOutStdErr) {
		this.set('joinStdOutStdErr', joinStdOutStdErr);
	},

	
});

var EnvironmentModel = Backbone.Model.extend({

});

var EnvironmentCollection = Backbone.Collection.extend({
	model: EnvironmentModel
});

var HPCToolImplementation = Backbone.Model.extend({
	initialize: function() {
		this.set('commandLineOptions', []);
		this.set('captureStdOut', null);
		this.set('captureStdErr', null);
	},

	setExecutable: function(exec) {
		this.set('executable', exec);	
	},

	setTemplate: function(template) {
		this.set('template', template);
	},

	setLog: function(log) {
		this.set('log', log);
	},

	setCaptureStdOut: function(captureStdOut) {
		this.set('captureStdOut', captureStdOut);
	},

	setCaptureStdErr: function(captureStdErr) {
		this.set('captureStdErr', captureStdErr);
	},

	setCommandLineOptions: function(commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
	},

	addCommandLineOption: function(commandLineOption) {
		this.get('commandLineOptions').push(commandLineOption);
	},

});