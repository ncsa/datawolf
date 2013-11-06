// Modal Dialog displaying tabs to provide information about a new command line tool
var CommandLineView = Backbone.View.extend({
	template: _.template($('#command-line-form').html()),

	events: {
		"click button#new-tool-create-btn" : "createTool"
	},

	render: function() {
		$(this.el).html(this.template());
		
		return this;
	},

	createTool: function(e) {
		e.preventDefault();
		console.log("create tool");

		var inputs = [];
		var outputs = [];
		var parameters = [];
		var blobs = [];

		var tool = new WorkflowTool();
        tool.set('id', generateUUID());
        tool.set('date', new Date());
        tool.set('executor', 'commandline');
        tool.set('creator', currentUser.toJSON());

		var title = $('#tool-title').val();
		var version = $('#tool-version').val();
		var description = $('#tool-description').val();
        tool.set('title', title);
        tool.set('description', description);
        tool.set('version', version);

        var exec = $('#tool-executable').val();
        var commandLineImpl = new CommandLineImplementation();
        commandLineImpl.setExecutable(exec);

        if($('#capture-stdout').is(":checked")) {
        	var title = $('#stdout-title').val();
        	var stdout = new WorkflowToolData();
        	stdout.set('id', generateUUID());	
        	stdout.set('title', title);
        	stdout.set('description', 'stdout of external tool.');
        	stdout.set('mimeType', 'text/plain');
        	stdout.set('dataId', 'stdout');
        	outputs.push(stdout);

       		commandLineImpl.setCaptureStdOut(stdout.get('dataId'));
       		if($('#join-stdout-stderr').is(":checked")) {
       			commandLineImpl.setJoinStdOutStdErr(true);
       		} 
        }

        if(!$('#join-stdout-stderr').is(":checked") && $('#capture-stderr').is(":checked")) {
        	var stderr = new WorkflowToolData();
        	stderr.set('id', generateUUID());
        	stderr.set('title', $('#stderr-title'));
        	stderr.set('description', "stderr of external tool.");
        	stderr.set('mimeType', 'text/plain');
        	stderr.set('dataId', 'stderr');
        	outputs.push(stderr);

        	commandLineImpl.setCaptureStdErr(stderr.get('dataId'));
        }

        commandLineOptionView.getOptionModel().each(function(option) {
        	commandLineImpl.addCommandLineOption(option);
        	switch(option.getType()) {
        		case 'VALUE':
        			break;
        		case 'PARAMETER':
        			parameters.push(commandLineOptionView.getParameter(option));
        			break;
        		case 'DATA':
        			switch(option.getInputOutput()) {
        				case 'INPUT':
        					inputs.push(commandLineOptionView.getData(option));
        					break;
        				case 'OUTPUT':
        					outputs.push(commandLineOptionView.getData(option));
        					break;
        				case 'BOTH':
        					inputs.push(commandLineOptionView.getData(option));
        					outputs.push(commandLineOptionView.getData(option));
        					break;
        			}
        			break;
        	}

        });

        tool.set('inputs', inputs);
        tool.set('outputs', outputs);
        tool.set('parameters', parameters);
        tool.set('blobs', blobs);

        console.log(JSON.stringify(tool, undefined, 2));
        //var zipfile = new JSZip();
        //zipfile.file('tool.json', JSON.stringify(tool));
		$('#modalWorkflowToolView').modal('hide');
	}
});

var CommandLineBasicTab = Backbone.View.extend({
	template: _.template($('#new-tool-basic-tab').html()),

	initialize: function() {
	},

	render: function() {
		$(this.el).empty();
		$(this.el).html(this.template());
		return this;
	}
});

var CommandLineOptionTab = Backbone.View.extend({
	template: _.template($('#new-tool-option-tab').html()),
	events: {
		"click button#add-tool-param-btn" : "showAddToolParameter",
		"click button#add-tool-data-btn" : "showAddToolData",
		"click button#add-tool-value-btn" : "showAddToolValue"
	},
	initialize: function() {
		// key is command line option, value is workflow parameter
		this.parameters = {};
		// key is command line option, value is workflow tool data
		this.data = {};
		// ordered array of command line options
		this.optionModel = new CommandLineOptionCollection();
	},

	render: function() {
		$(this.el).empty();
		$(this.el).html(this.template());
		this.clOptionsView = new CommandLineOptionListView({model: this.optionModel});
		// TODO add command line options list view
		return this;
	},

	getCommandLineOptionsListView: function() {
		return this.clOptionsView;
	},

	showAddToolParameter: function() {
		console.log("adding parameter");
		$('#newCommandLineOptionLabel').text("CommandLine Parameter");
        $('#add-parameter-view').html(new CommandLineParameterView().render().el);
        $('#modalParameterView').modal('show');
	},

	showAddToolData: function() {
		$('#newCommandLineOptionLabel').text("CommandLine Input/Output");
		$('#add-parameter-view').html(new CommandLineAddDataView().render().el);
        $('#modalParameterView').modal('show');
	},

	showAddToolValue: function() {
		$('#newCommandLineOptionLabel').text("CommandLine Value");
		$('#add-parameter-view').html(new CommandLineAddValueView().render().el);
        $('#modalParameterView').modal('show');
	},

	addParameter: function(cmdLineOption, wfParameter) {
		this.parameters[cmdLineOption] = wfParameter;
		this.addCommandLineOption(cmdLineOption);
		console.log("how many options: "+this.optionModel.size());
	},

	addData: function(cmdLineOption, wfToolData) {
		this.data[cmdLineOption] = wfToolData;
		this.addCommandLineOption(cmdLineOption);
	},

	addCommandLineOption: function(cmdLineOption) {
		this.optionModel.add(cmdLineOption);
	},

	getOptionModel: function() {
		return this.optionModel;
	},

	getParameter: function(cmdLineOption) {
		return this.parameters[cmdLineOption];
	},

	getData: function(cmdLineOption) {
		return this.data[cmdLineOption];
	}

});

// View displayed when Add Parameter button pressed
var CommandLineParameterView = Backbone.View.extend({
	template: _.template($('#new-parameter-view').html()),

	events: {
		"click button#add-parameter-btn" : "addToolParameter"
	},

	render: function() {
		console.log("render parameter template");
		$(this.el).html(this.template());
		return this;
	},

	addToolParameter: function(e) {
		e.preventDefault();
		var flag = $('#tool-parameter-flag').val();
		var title = $('#tool-parameter-name').val();
		var desc = $('#tool-parameter-description').val();
		var type = $('#tool-parameter-type').val();
		var value = $('#tool-parameter-default').val();
		var hidden = $('#tool-parameter-hidden').is(':checked');
		var allowNull = $('#tool-parameter-empty').is(':checked');

		var param = new WorkflowToolParameter();
        param.set('id', generateUUID());
        param.set('title', title);
        param.set('type', type);
        param.set('hidden', hidden);
        param.set('allowNull', allowNull);
        param.set('parameterId', generateUUID());
        param.set('value', value);

        console.log('hidden = '+hidden);

        var clOption = new CommandLineOption();
        clOption.setType('PARAMETER');
        clOption.setFlag(flag);
        clOption.setOptionId(generateUUID());

        commandLineOptionView.addParameter(clOption, param);
        $('#modalParameterView').modal('hide');
	}
});

// View displayed when Add Data button is pressed
var CommandLineAddDataView = Backbone.View.extend({
	template: _.template($('#add-data-view').html()),

	events: {
		"click button#add-data-btn" : "addToolData",
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	addToolData: function(e) {
		e.preventDefault();

		var title = $('#tool-data-name').val();
		var description = $('#tool-data-description').val();
		var mimeType = $('#tool-data-content').val();

		var data = new WorkflowToolData();
		data.set('id', generateUUID());
		data.set('title', title);
		data.set('description', description);
		data.set('mimeType', mimeType);
		data.set('dataId', generateUUID());

		var flag = $('#tool-data-flag').val();
		var inputOutput = $('#tool-data-type').val();
		var fileName = $('#tool-data-filename').val();
		var commandLine = $('#tool-data-commandline').is(':checked');

		var option = new CommandLineOption();
		option.setType('DATA');
		option.setFlag(flag);
		option.setInputOutput(inputOutput);
		option.setFilename(fileName);
		option.setCommandline(commandLine);
		option.setOptionId(data.get('dataId'));

		commandLineOptionView.addData(option, data);
		$('#modalParameterView').modal('hide');
	}

});

var CommandLineAddValueView = Backbone.View.extend({
	template: _.template($("#add-value-view").html()),

	events: {
		"click button#add-value-btn" : "addValue",
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	addValue: function(e) {
		e.preventDefault();

		var flag = $('#tool-value-flag').val();
		var value = $('#tool-value').val();
		var option = new CommandLineOption();
		option.setType('VALUE');
		option.setFlag(flag);
		option.setValue(value);

		commandLineOptionView.addCommandLineOption(option);
		$('#modalParameterView').modal('hide');
	}
});

var CommandLineOptionListView = Backbone.View.extend({
	tagName: "select",
	className: "wf-options-list-select",

	initialize: function() {
		//console.log('initialize option list');
		this.$el.attr('size', '15');
		var self = this;
		this.model.bind("add", function(option) {
			console.log('option add: '+JSON.stringify(option, undefined, 2));
			$(self.el).append(new CommandLineOptionListItemView({model: option}).render().el);
		});
	},

	render: function() {
		$(this.el).empty();
		_.each(this.model.models, function(option) {
			$(this.el).append(new CommandLineOptionListItemView({model: option}).render().el);
		}, this);

		return this;
	}
});

var CommandLineOptionListItemView = Backbone.View.extend({
	tagName: "option",
	template: _.template($('#tool-option-list-item').html()),

	render: function() {
		console.log("render a option item");
		var optionAsString = "";

		if(this.model.getFlag() != null && !_.isEmpty(this.model.getFlag())) {
			optionAsString += this.model.getFlag().trim() + " ";
		}

		switch(this.model.getType()) {
			case 'PARAMETER': 
				var param = commandLineOptionView.getParameter(this.model);
				optionAsString += param.get('title');
				optionAsString += '[';
				optionAsString += param.get('type');
				optionAsString += '] = ';
				optionAsString += param.get('value');
				break;
			case 'VALUE':
				if((this.model.getValue() != null) && (this.model.getValue().trim().length > 0)) {
					optionAsString += this.model.getValue().trim();
				}	
				break;
			case 'DATA':
				optionAsString += "file(";
					switch(this.model.getInputOutput()) {
						case 'INPUT':
							optionAsString += "in:";
							break;
						case 'OUTPUT':
							optionAsString += "out:";
							break;
						case 'BOTH':
							optionAsString += "in/out:";
							break;
					}

					if((this.model.getFilename() != null) && (this.model.getFilename().trim().length > 0)) {
						optionAsString += option.getFilename().trim();
					} else {
						optionAsString += "AUTO";
					}

					if(this.model.isCommandline()) {
						optionAsString += '[not passed]';
					}

					optionAsString += ")";

				break;
		}

		$(this.el).html(this.template({optionAsString: optionAsString}));	
		return this;
	}
});