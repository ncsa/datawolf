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
});

var CommandLineImplementation = Backbone.Model.extend({
	initialize: function() {
		this.commandLineOptions = [];
		this.env = {};
		this.captureStdOut = null;
		this.captureStdErr = null;
		this.joinStdOutStdErr = false;
	},

	setExecutable: function(exec) {
		this.set('executable', exec);	
	},

	setCommandLineOptions: function(commandLineOptions) {
		this.commandLineOptions = commandLineOptions;
	},

	addCommandLineOption: function(commandLineOption) {
		this.commandLineOptions.push(commandLineOption);
	},

	setEnv: function(env) {
		this.env = env;
	},

	setCaptureStdOut: function(captureStdOut) {
		this.captureStdOut = captureStdOut;
	},

	setCaptureStdErr: function(captureStdErr) {
		this.captureStdErr = captureStdErr;
	},

	setJoinStdOutStdErr: function(joinStdOutStdErr) {
		this.joinStdOutStdErr = joinStdOutStdErr;
	},

	
});