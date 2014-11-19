// Modal Dialog displaying tabs to provide information about a new command line tool
var CommandLineView = Backbone.View.extend({
	template: _.template($('#command-line-form').html()),

	events: {
		"click button#new-tool-create-btn" : "createTool",
		"click button#new-tool-cancel-btn" : "cancel"
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
		var title = $('#tool-title').val();
		var version = $('#tool-version').val();
		var description = $('#tool-description').val();

		var tool = createWorkflowTool(title, description, version, "commandline");

        var exec = $('#tool-executable').val();
        var commandLineImpl = new CommandLineImplementation();
        commandLineImpl.setExecutable(exec);

        if($('#capture-stdout').is(":checked")) {
        	var title = $('#stdout-title').val();
        	var stdout = createWorkflowToolData(title, 'stdout of external tool', 'text/plain');
        	stdout.set('dataId', 'stdout');
        	outputs.push(stdout);

       		commandLineImpl.setCaptureStdOut(stdout.get('dataId'));
       		if($('#join-stdout-stderr').is(":checked")) {
       			commandLineImpl.setJoinStdOutStdErr(true);
       		} 
        }

        if(!$('#join-stdout-stderr').is(":checked") && $('#capture-stderr').is(":checked")) {
        	var title = $('#stderr-title').val();
        	var stderr = createWorkflowToolData(title, 'stderr of external tool.', 'text/plain');
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

        // Update Environment
        commandLineImpl.setEnv(commandLineEnvView.getEnvironmentMap());

        tool.set('implementation', JSON.stringify(commandLineImpl));
        tool.set('inputs', inputs);
        tool.set('outputs', outputs);
        tool.set('parameters', parameters);
        tool.set('blobs', blobs);

        var files = $('#tool-file-form')[0][0].files;
        this.numFiles = files.length;
        this.fileCounter = 0;
        var zipfile = new JSZip();
        var instance = this;
        
        if(this.numFiles > 0) {
			var blobFolder = zipfile.folder('blobs');
	        for(var index = 0; index < files.length; index++) {

		        var reader = new FileReader();
		        reader.onload = (function(file) {
		        	return function(e) {
			        	var fileDescriptor = {};
						fileDescriptor.id = generateUUID();
				        fileDescriptor.filename = file.name;
				        fileDescriptor.mimeType = file.type;
				        fileDescriptor.size = file.size;
				        blobFolder.file(fileDescriptor.id + '/' + file.name, e.target.result);
						blobs.push(fileDescriptor)

			        	instance.fileCounter++;
				        // Check if ready to upload tool
			        	instance.checkReadyState(tool, zipfile);
		        	};
		        })(files[index]);
		        reader.readAsArrayBuffer(files[index]);
		    }
		} else {
			this.checkReadyState(tool, zipfile);
		} 
	    $('#modalWorkflowToolView').modal('hide');
	    console.log("exit create tool");
        
	},

	cancel: function(e) {
		e.preventDefault();
		$('#modalWorkflowToolView').modal('hide');
	},

	checkReadyState: function(tool, zipfile) {
		if(this.fileCounter == this.numFiles) {
			zipfile.file('tool.json', JSON.stringify(tool));
			//console.log(JSON.stringify(tool, undefined, 2));
			postTool(zipfile);
		}
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
		"click button#add-tool-value-btn" : "showAddToolValue",
		"click button#edit-cloption-btn" : "editCommandlineOption",
		"click button#move-cloption-up-btn" : "commandLineOptionUp",
		"click button#move-cloption-down-btn" : "commandLineOptionDown",
		"click button#delete-cloption-btn" : "deleteCommandlineOption",
		"keyup input#tool-executable" : "updateExecutionOptions",
		"click input#capture-stdout" : "updateExecutionOptions",
		"click input#capture-stderr" : "updateExecutionOptions",
		"click input#join-stdout-stderr" : "updateExecutionOptions"

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
		return this;
	},

	getCommandLineOptionsListView: function() {
		return this.clOptionsView;
	},

	showAddToolParameter: function() {
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
		this.parameters[cmdLineOption.getOptionId()] = wfParameter;
		this.addCommandLineOption(cmdLineOption);
	},

	addData: function(cmdLineOption, wfToolData) {
		this.data[cmdLineOption.getOptionId()] = wfToolData;
		this.addCommandLineOption(cmdLineOption);
	},

	addCommandLineOption: function(cmdLineOption) {
		this.optionModel.add(cmdLineOption);
	},

	getOptionModel: function() {
		return this.optionModel;
	},

	getParameter: function(cmdLineOption) {
		return this.parameters[cmdLineOption.getOptionId()];
	},

	getData: function(cmdLineOption) {
		return this.data[cmdLineOption.getOptionId()];
	},

	commandLineOptionUp: function(e) {
		e.preventDefault();
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			if(index > 0) {
				this.getOptionModel().swapElements(index, index-1);
				this.clOptionsView.render();
				this.updateExecutionOptions();
			}
		}
	},

	commandLineOptionDown: function(e) {
		e.preventDefault();
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			if(index < this.getOptionModel().size()-1) {
				this.getOptionModel().swapElements(index, index+1);
				this.clOptionsView.render();
				this.updateExecutionOptions();
			}
		}
	},

	deleteCommandlineOption: function(e) {
		e.preventDefault();
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			var model = this.optionModel.at(index);
			if(model in this.parameters) {
				delete this.parameters[model];	
			}
			this.optionModel.remove(model);
			this.updateExecutionOptions();
		} else {
			console.log('nothing selected');
		}

	},

	editCommandlineOption: function() {
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			var clOption = this.optionModel.at(index);

			// Determine which kind of option (data/value/parameter)
			if(clOption.get('type') === 'PARAMETER') {
				var model = this.getParameter(clOption);
				$('#newCommandLineOptionLabel').text("CommandLine Parameter");
	        	$('#add-parameter-view').html(new CommandLineParameterView({model: model, clOption: clOption}).render().el);
			} else if(clOption.get('type') === 'DATA') {
				var model = this.getData(clOption);
				$('#newCommandLineOptionLabel').text("CommandLine Input/Output");
	        	$('#add-parameter-view').html(new CommandLineAddDataView({model: model, clOption: clOption}).render().el);
			} else {
				$('#newCommandLineOptionLabel').text("CommandLine Parameter");
	        	$('#add-parameter-view').html(new CommandLineAddValueView({model: clOption}).render().el);
			}

			// Display view to edit option
        	$('#modalParameterView').modal('show');
		}
	},

	updateExecutionOptions: function() {
		var executionText = $('#tool-executable').val();

		executionText += " ";
		this.optionModel.each(function(option) {
			executionText += getOptionString(option) + " "; 
		});	

		if($('#join-stdout-stderr').is(":checked")) {
			executionText += " 2>&1";
		} else if($('#capture-stderr').is(":checked")) {
			executionText += " 2>" + $('#stderr-title').val();
		}

		if($('#capture-stdout').is(":checked")) {
			executionText += " >" + $('#stdout-title').val();
		}

		$('#tool-execution-line').val(executionText);
	}

});

// View displayed when Add Parameter button pressed
var CommandLineParameterView = Backbone.View.extend({
	template: _.template($('#new-parameter-view').html()),

	events: {
		"click button#add-parameter-btn" : "addToolParameter",
		"click button#cancel-parameter-btn" : "close"
	},

	render: function() {
		var flag = "";
		var title = "";
		var description = "";
		var value = "";
		var commandline = true;
		var hidden = false;
		var allowNull = false;
		var type = "STRING";
		if(this.model != undefined) {
			flag = this.options.clOption.get('flag');
			commandline = this.options.clOption.get('commandline');

			title = this.model.get('title');
			description = this.model.get('description');
			value = this.model.get('value');
			hidden = this.model.get('hidden');
			allowNull = this.model.get('allowNull');
			type = this.model.get('type');
		}
		var params = {flag: flag, title: title, description: description, value: value, commandline: commandline, hidden: hidden, allowNull: allowNull, type: type};
		$(this.el).html(this.template(params));

		return this;
	},

	addToolParameter: function(e) {
		e.preventDefault();

		var triggerUpdate = false;
		var flag = $('#tool-parameter-flag').val();
		var title = $('#tool-parameter-name').val();
		var desc = $('#tool-parameter-description').val();
		var type = $('#tool-parameter-type').val();
		var value = $('#tool-parameter-default').val();
		var hidden = $('#tool-parameter-hidden').is(':checked');
		var allowNull = $('#tool-parameter-empty').is(':checked');
		var commandline = $('#tool-parameter-commandline').is(':checked');

		var param = this.model;
		var clOption = this.options.clOption;
		if(this.model === undefined) {
			param = createWorkflowToolParameter(title, desc, allowNull, type, hidden, value);
			clOption = createCommandLineOption('PARAMETER', commandline);
			clOption.setOptionId(param.getParameterId());
			clOption.setFlag(flag);
			commandLineOptionView.addParameter(clOption, param);
		} else {
			param = updateWorkflowToolParameter(param, title, desc, allowNull, type, hidden, value);
			if(clOption.get('flag') === flag && clOption.get('commandline') === commandline) {
				triggerUpdate = true;
			} 
			clOption.setFlag(flag);
			clOption.setCommandline(commandline);

			if(triggerUpdate) {
				// Since changes are to an object that clOption refers to, we must manually fire a change event to update rendering
				clOption.trigger('change');
			}
		}
        
        $('#modalParameterView').modal('hide');
        commandLineOptionView.updateExecutionOptions();
	},

	close: function(e) {
		e.preventDefault();
		$('#modalParameterView').modal('hide');
	}
});

// View displayed when Add Data button is pressed
var CommandLineAddDataView = Backbone.View.extend({
	template: _.template($('#add-data-view').html()),

	events: {
		"click button#add-data-btn" : "addToolData",
		"click button#cancel-data-btn" : "close"
	},

	initialize: function() {

	},

	render: function() {
		var flag = "";
		var title = "";
		var description = "";
		var contentType = "";
		var fileName = "";
		var commandline = true;
		var inputOutput = "INPUT";
		if(this.model != undefined) {
			flag = this.options.clOption.get('flag');
			fileName = this.options.clOption.get('filename');
			commandline = this.options.clOption.get('commandline');
			inputOutput = this.options.clOption.get('inputOutput');
			title = this.model.get('title');
			description = this.model.get('description');
			contentType = this.model.get('mimeType');
		}
		var dataFields = {flag: flag, title: title, description: description, mimeType: contentType, filename: fileName, commandline: commandline, inputOutput: inputOutput};
		$(this.el).html(this.template(dataFields));

		return this;
	},

	addToolData: function(e) {
		e.preventDefault();

		var triggerUpdate = false;
		var title = $('#tool-data-name').val();
		var description = $('#tool-data-description').val();
		var mimeType = $('#tool-data-content').val();
		var flag = $('#tool-data-flag').val();
		var inputOutput = $('#tool-data-type').val();
		var fileName = $('#tool-data-filename').val();
		var commandLine = $('#tool-data-commandline').is(':checked');

		var data = this.model;
		var clOption = this.options.clOption;
		if(this.model === undefined) {
			data = createWorkflowToolData(title, description, mimeType);
			clOption = createCommandLineOption('DATA', commandLine);
			clOption.setOptionId(data.get('dataId'));
			clOption.setFlag(flag);
			clOption.setInputOutput(inputOutput);
			clOption.setFilename(fileName);
			commandLineOptionView.addData(clOption, data);
		} else {
			data = updateWorkflowToolData(data, title, description, mimeType);
			if(clOption.get('flag') === flag && clOption.get('inputOutput') === inputOutput && clOption.get('filename') === fileName && clOption.get('commandline') === commandLine) {
				// If none of the attributes of clOption have changed, manually trigger an update
				triggerUpdate = true;
			}

			clOption.setCommandline(commandLine);
			clOption.setFlag(flag);
			clOption.setInputOutput(inputOutput);
			clOption.setFilename(fileName);

			if(triggerUpdate) {
				clOption.trigger('change');
			}
		}
		$('#modalParameterView').modal('hide');
		commandLineOptionView.updateExecutionOptions();
	},

	close: function(e) {
		e.preventDefault();
		$('#modalParameterView').modal('hide');
	}

});

var CommandLineAddValueView = Backbone.View.extend({
	template: _.template($("#add-value-view").html()),

	events: {
		"click button#add-value-btn" : "addValue",
		"click button#cancel-value-btn" : "close"
	},

	initialize: function() {

	},

	render: function() {
		var flag = "";
		var value = "";
		if(this.model != undefined) {
			flag = this.model.get('flag');
			value = this.model.get('value');
		}

		var valueFields = {flag: flag, value: value};
		$(this.el).html(this.template(valueFields));
		return this;
	},

	addValue: function(e) {
		e.preventDefault();

		var flag = $('#tool-value-flag').val();
		var value = $('#tool-value').val();
		var clOption = this.model;
		if(clOption === undefined) {
			clOption = createCommandLineOption('VALUE', true);
			clOption.setFlag(flag);
			clOption.setValue(value);
			commandLineOptionView.addCommandLineOption(clOption);
		} else {
			clOption.setFlag(flag);
			clOption.setValue(value);
		}

		$('#modalParameterView').modal('hide');
		commandLineOptionView.updateExecutionOptions();
	},

	close: function(e) {
		e.preventDefault();
		$('#modalParameterView').modal('hide');
	}
});

var CommandLineOptionListView = Backbone.View.extend({
	tagName: "select",
	className: "wf-options-list-select",
	id: "commandLineOptionListView",

	initialize: function() {
		//console.log('initialize option list');
		this.$el.attr('size', '15');
		this.clOptionViews = [];
		var self = this;
		this.model.bind("add", function(option) {
			var clOptionView = new CommandLineOptionListItemView({model: option});
			self.clOptionViews.push(clOptionView);
			$(self.el).append(clOptionView.render().el);
		});

		this.model.bind("remove", function(option) {
			console.log("remove option");
			var viewToRemove = _(self.clOptionViews).select(function(cv) {
				return cv.model === option;
			})[0];
			$(viewToRemove.el).remove();
		});
	},

	render: function() {
		$(this.el).empty();
		this.clOptionViews = [];
		_.each(this.model.models, function(option) {
			var clOptionView = new CommandLineOptionListItemView({model: option});
			this.clOptionViews.push(clOptionView);
			$(this.el).append(clOptionView.render().el);
		}, this);

		return this;
	}
});

var CommandLineOptionListItemView = Backbone.View.extend({
	tagName: "option",
	template: _.template($('#tool-option-list-item').html()),

	attributes: function() {
		return {
			value: JSON.stringify(this.model)
		}
	},

	initialize: function() {
		this.model.bind("change", this.render, this);
	},

	render: function() {
		var optionAsString = getOptionString(this.model);	

		$(this.el).html(this.template({optionAsString: optionAsString}));	
		return this;
	}
});

var CommandLineFileTab = Backbone.View.extend({
	template: _.template($('#new-tool-file-tab').html()),
	events: {
		"change input.tool-file-select" : "fileChange"
	},

	initialize: function() {
		
	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	fileChange: function(e) {
		e.preventDefault();
		if(!e.target.files) {
			return;
		}
		var div = document.querySelector("#selected-files");
		var myfile = $('#tool-file-form')[0][0].files[0];

		var files = e.target.files;
		var index;
		for(index = 0; index < files.length; index++) {
			var file = files[index];
			div.innerHTML += file.name + "<br/>";
		}
	}

});

var CommandLineEnvTab = Backbone.View.extend({
	template: _.template($('#new-tool-env-tab').html()),
	events: {
		"click button#add-tool-env-btn" : "addEnvironmentVariable",
	},

	initialize: function() {
		this.envModel = new EnvironmentCollection();
	},

	render: function() {
		$(this.el).html(this.template());

		this.clTableView = new CommandLineEnvTable({model: this.envModel});
		$(this.el).find('#env-table').html(this.clTableView.render().el);
		return this;
	},

	addEnvironmentVariable: function(e) {
		e.preventDefault();
		this.envModel.add(new EnvironmentModel());
	},

	getEnvironmentMap: function() {
		var envMap = {};
		var rows = this.clTableView.getTableRows()
		for(var index = 0; index < rows.length; rows++) {
			var row = rows[0];
			var envRow = row.getRowValue();
			envMap[envRow[0]] = envRow[1];
		}

		return envMap;
	},
});

var CommandLineEnvTable = Backbone.View.extend({
	tagName: 'table',
	className: 'table',
	template: _.template($('#env-tab-table-header').html()),

	initialize: function() {
		var self = this;
		this.tableRows = [];
		this.model.bind("add", function(envModel) {
			var tableRow = new CommandLineEnvRow({model: envModel});
			$(self.el).append(tableRow.render().el);
			self.tableRows.push(tableRow);
		});

		this.model.bind("remove", function(envModel) {
			var viewToRemove = _(self.tableRows).select(function(cv) {
				return cv.model === envModel;
			})[0];
			$(viewToRemove.el).remove();
		});
	},

	render: function() {
		// Without trimming the white space, the header is spaced incorrectly
		$(this.el).append(this.template().trim());
		return this;
	},

	getTableRows: function() {
		return this.tableRows;
	}
});

var CommandLineEnvRow = Backbone.View.extend({
	template: _.template($('#env-tab-row').html()),
	events: {
		"click" : "highlightRow",
		"click button#remove-env-btn" : "clear"
	},

	initialize: function() {

	},

	attributes: function() {
        return {
            value: this.model
        }
    },

	render: function() {
		this.setElement(this.template().trim());
		return this;
	},

	getRowValue: function() {
		var env = [];
		env[0] = $(this.el).find('#variable').val().trim();
		env[1] = $(this.el).find('#value').val().trim();
		return env;
	},

	highlightRow: function(e) {
		$('.highlight').removeClass('highlight');
        $(this.el).addClass('highlight');
	},

	clear: function(e) {
		this.model.destroy();
	}
}); 

// New HPC Tool
var HPCToolView = Backbone.View.extend({
	template: _.template($('#hpc-tool-form').html()),

	events: {
		"click button#new-tool-create-btn" : "createTool",
		"click button#new-tool-cancel-btn" : "cancel"
	},

	initialize: function() {

	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	cancel: function(e) {
		e.preventDefault();
		$('#modalWorkflowToolView').modal('hide');
	},

	createTool: function(e) {
		console.log("create tool");
		var inputs = [];
		var outputs = [];
		var parameters = [];
		var blobs = [];
		var title = $('#tool-title').val();
		var version = $('#tool-version').val();
		var description = $('#tool-description').val();

		var tool = createWorkflowTool(title, description, version, "hpc");

        var exec = $('#tool-executable').val();
        var hpcImpl = new HPCToolImplementation();
        hpcImpl.setExecutable(exec);	

        // Capture Gondola-Log
        var gondolaLog = createWorkflowToolData('gondola-log', 'Gondola log file from remote execution.', 'text/plain');
        gondolaLog.setDataId('gondola-log');
        outputs.push(gondolaLog);

        hpcImpl.setLog(gondolaLog.getDataId());

        // Capture Standard out
        var stdout = createWorkflowToolData('standard-out', 'standard output of remote tool execution.', 'text/plain');
        outputs.push(stdout);

        hpcImpl.setCaptureStdOut(stdout.getDataId());

        // Capture Standard error
        var stderr = createWorkflowToolData('standard-err', 'standard error of remote tool execution.', 'text/plain');
        outputs.push(stderr);

        hpcImpl.setCaptureStdErr(stderr.getDataId());

        // Add username parameter
        var username = createWorkflowToolParameter('Target Username', 'Username on remote host', false, 'STRING', false, '');

        var usernameOption = createCommandLineOption('PARAMETER', false);
        usernameOption.setOptionId(username.getParameterId());
        parameters.push(username);

        hpcImpl.addCommandLineOption(usernameOption);

        // Add HPC Target User Home
        var userhome = createWorkflowToolParameter('Target Userhome', 'User home directory on remote host.', false, 'STRING', false, '');
        var userhomeOption = createCommandLineOption('PARAMETER', false);
        userhomeOption.setOptionId(userhome.getParameterId());
        parameters.push(userhome);

        hpcImpl.addCommandLineOption(userhomeOption);

        // Add HPC Target URI
        var targetUri = createWorkflowToolParameter('Target SSH', 'Remote host SSH URI.', false, 'STRING', false, '');

        var targetUriOption = createCommandLineOption('PARAMETER', false);
        targetUriOption.setOptionId(targetUri.getParameterId());
        parameters.push(targetUri);

        hpcImpl.addCommandLineOption(targetUriOption);

        // Add User specified options
        commandLineOptionView.getOptionModel().each(function(option) {
        	hpcImpl.addCommandLineOption(option);
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

        var files = $('#hpc-tool-option-form')[0][1].files;
        this.numFiles = files.length;
        this.fileCounter = 0;
        var zipfile = new JSZip();
        var instance = this;

        if(this.numFiles > 0) {
        	hpcImpl.setTemplate(files[0].name);
	        tool.set('inputs', inputs);
	        tool.set('outputs', outputs);
	        tool.set('parameters', parameters);
	        tool.set('blobs', blobs);
	        tool.set('implementation', JSON.stringify(hpcImpl));
			var blobFolder = zipfile.folder('blobs');
	        for(var index = 0; index < files.length; index++) {

		        var reader = new FileReader();
		        reader.onload = (function(file) {
		        	return function(e) {
			        	var fileDescriptor = {};
						fileDescriptor.id = generateUUID();
				        fileDescriptor.filename = file.name;
				        fileDescriptor.mimeType = file.type;
				        fileDescriptor.size = file.size;
				        blobFolder.file(fileDescriptor.id + '/' + file.name, e.target.result);
						blobs.push(fileDescriptor)

			        	instance.fileCounter++;
				        // Check if ready to upload tool
			        	instance.checkReadyState(tool, zipfile);
		        	};
		        })(files[index]);
		        reader.readAsArrayBuffer(files[index]);
		    }
		} else {
			this.checkReadyState(tool, zipfile);
		} 
	    $('#modalWorkflowToolView').modal('hide');
	},

	checkReadyState: function(tool, zipfile) {
		if(this.fileCounter == this.numFiles) {
			zipfile.file('tool.json', JSON.stringify(tool));
			//console.log(JSON.stringify(tool, undefined, 2));
			postTool(zipfile);
		}
	}
});

var HPCToolOptionTab = Backbone.View.extend({
	template: _.template($('#new-hpc-tool-option-tab').html()),
	events: {
		"click button#add-tool-param-btn" : "showAddToolParameter",
		"click button#add-tool-data-btn" : "showAddToolData",
		"click button#add-tool-value-btn" : "showAddToolValue",
		"click button#edit-cloption-btn" : "editCommandlineOption",
		"click button#move-cloption-up-btn" : "commandLineOptionUp",
		"click button#move-cloption-down-btn" : "commandLineOptionDown",
		"click button#delete-cloption-btn" : "deleteCommandlineOption",
		"keyup input#tool-executable" : "updateExecutionOptions",

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
		return this;
	},

	getCommandLineOptionsListView: function() {
		return this.clOptionsView;
	},

	showAddToolParameter: function() {
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
		this.parameters[cmdLineOption.getOptionId()] = wfParameter;
		this.addCommandLineOption(cmdLineOption);
	},

	addData: function(cmdLineOption, wfToolData) {
		this.data[cmdLineOption.getOptionId()] = wfToolData;
		this.addCommandLineOption(cmdLineOption);
	},

	addCommandLineOption: function(cmdLineOption) {
		this.optionModel.add(cmdLineOption);
	},

	getOptionModel: function() {
		return this.optionModel;
	},

	getParameter: function(cmdLineOption) {
		return this.parameters[cmdLineOption.getOptionId()];
	},

	getData: function(cmdLineOption) {
		return this.data[cmdLineOption.getOptionId()];
	},

	commandLineOptionUp: function(e) {
		e.preventDefault();
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			if(index > 0) {
				this.getOptionModel().swapElements(index, index-1);
				this.clOptionsView.render();
				this.updateExecutionOptions();
			}
		}
	},

	commandLineOptionDown: function(e) {
		e.preventDefault();
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			if(index < this.getOptionModel().size()-1) {
				this.getOptionModel().swapElements(index, index+1);
				this.clOptionsView.render();
				this.updateExecutionOptions();
			}
		}
	},

	editCommandlineOption: function() {
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			var clOption = this.optionModel.at(index);

			// Determine which kind of option (data/value/parameter)
			if(clOption.get('type') === 'PARAMETER') {
				var model = this.getParameter(clOption);
				$('#newCommandLineOptionLabel').text("CommandLine Parameter");
	        	$('#add-parameter-view').html(new CommandLineParameterView({model: model, clOption: clOption}).render().el);
			} else if(clOption.get('type') === 'DATA') {
				var model = this.getData(clOption);
				$('#newCommandLineOptionLabel').text("CommandLine Input/Output");
	        	$('#add-parameter-view').html(new CommandLineAddDataView({model: model, clOption: clOption}).render().el);
			} else {
				$('#newCommandLineOptionLabel').text("CommandLine Parameter");
	        	$('#add-parameter-view').html(new CommandLineAddValueView({model: clOption}).render().el);
			}

			// Display view to edit option
        	$('#modalParameterView').modal('show');
		}
	},

	deleteCommandlineOption: function(e) {
		e.preventDefault();
		if($('#commandLineOptionListView').val() != null) {
			var index = $('#commandLineOptionListView')[0].selectedIndex;
			var model = this.optionModel.at(index);
			if(model in this.parameters) {
				delete this.parameters[model];	
			}
			this.optionModel.remove(model);
			this.updateExecutionOptions();
		} else {
			console.log('nothing selected');
		}
		
	},

	updateExecutionOptions: function() {
		var executionText = $('#tool-executable').val();

		executionText += " ";
		this.optionModel.each(function(option) {
			executionText += getOptionString(option) + " "; 
		});	

		if($('#join-stdout-stderr').is(":checked")) {
			executionText += " 2>&1";
		} else if($('#capture-stderr').is(":checked")) {
			executionText += " 2>" + $('#stderr-title').val();
		}

		if($('#capture-stdout').is(":checked")) {
			executionText += " >" + $('#stdout-title').val();
		}

		$('#tool-execution-line').val(executionText);
	}

});

var JavaToolSelectionTab = Backbone.View.extend({
	template: _.template($('#java-tool-jar-tab').html()),
	events: {
		"change input#java-tool-files" : "fileChange",
		"click button#check-jars-btn" : "findTools",
		"click button#java-tool-cancel-btn" : "cancel",
		"click button#java-tool-create-btn" : "createTool"
	},

	render: function() {
		$(this.el).html(this.template());
		return this;
	},

	fileChange: function(e) {
		e.preventDefault();
		if(!e.target.files) {
			return;
		}
		var div = document.querySelector("#selected-jar-files");
		var myfile = $('#java-tool-option-form')[0][0].files[0];

		var files = e.target.files;
		var index;
		for(index = 0; index < files.length; index++) {
			var file = files[index];
			div.innerHTML += file.name + "<br/>";
		}
	},

	findTools: function(e) {
		e.preventDefault();
		var files = $('#java-tool-option-form')[0][0].files;
		this.numFiles = files.length;
		this.fileCounter = 0;
		var instance = this;
		var zipfile = new JSZip();
		var blobFolder = zipfile.folder('blobs');
		for(var index = 0; index < files.length; index++) {
            var reader = new window.FileReader();
            reader.onload = (function(file) {
		        return function(e) {
		        	var filename = file.name;
		        	filename = filename.substring(0, filename.length - 4);
	                blobFolder.file(filename, e.target.result);

	                instance.fileCounter++;
	                instance.checkReadyState(zipfile);
            	};
		    })(files[index]);
		    reader.readAsArrayBuffer(files[index]);
        }
	},

	createTool: function(e) {
		e.preventDefault();
		var selection = $("#jar-tools option:selected").val();

		// TODO CMN - allow multiple tools to be uploaded
		//console.log("# of tools = "+selections.length);
		//console.log("first tool = "+$(selections[0]).val());
		//console.log("2nd tool = "+$(selections[1]).val());
		//$("#jar-tools option:selected").each(function() {

		var model = null;
		javaToolCollection.each(function(javatool) {
			if(javatool.get('toolClass') === selection) {
				model = javatool;
				return false;
			}
		});

		var inputs = [];
		var outputs = [];
		var parameters = [];
		var blobs = [];
		var title = model.get('name');
		var version = model.get('version');
		var description = model.get('description');

		var tool = createWorkflowTool(title, description, version, "java");

		// Add Inputs
		var modelInputs = model.get('inputs');
		for(var index = 0; index < modelInputs.length; index++) {
			var inputTitle = modelInputs[index].name;
			var inputDesc = modelInputs[index].description;
			var mimeType = modelInputs[index].type;
			var toolData = createWorkflowToolData(inputTitle, inputDesc, mimeType);
			toolData.set('dataId', modelInputs[index].id);

			inputs.push(toolData);
		}

		// Add Outputs
		var modelOutputs = model.get('outputs');
		for(var index = 0; index < modelOutputs.length; index++) {
			var outputTitle = modelOutputs[index].name;
			var outputDesc = modelOutputs[index].description;
			var mimeType = modelOutputs[index].type;
			var toolData = createWorkflowToolData(outputTitle, outputDesc, mimeType);
			toolData.set('dataId', modelOutputs[index].id);

			outputs.push(toolData);
		}

		// Add Parameters
		var modelParams = model.get('parameters');
		for(var index = 0; index < modelParams.length; index++) {
			var title = modelParams[index].name;
			var description = modelParams[index].description;
			var allowNull = modelParams[index].allowEmpty;
			var type = modelParams[index].type;
			var hidden = modelParams[index].hidden;
			var value = modelParams[index].value;

			var toolParam = createWorkflowToolParameter(title, description, allowNull, type, hidden, value);
			toolParam.setParameterId(modelParams[index].id);
			toolParam.set('options', modelParams[index].options);
			parameters.push(toolParam);
		}

		var javaToolImpl = new JavaToolImplementation();
		javaToolImpl.setToolClassName(model.get('toolClass'));

		tool.set('implementation', JSON.stringify(javaToolImpl));
		tool.set('inputs', inputs);
		tool.set('outputs', outputs);
		tool.set('parameters', parameters);
		tool.set('blobs', blobs);

		// TODO add Blobs
		var files = $('#java-tool-option-form')[0][0].files;
		this.numFiles = files.length;
		this.fileCounter = 0;
		var zipfile = new JSZip();
		var instance = this;
		var blobFolder = zipfile.folder('blobs');
		for(var index = 0; index < files.length; index++) {

		    var reader = new FileReader();
		    reader.onload = (function(file) {
		    	return function(e) {
		        	var fileDescriptor = {};
					fileDescriptor.id = generateUUID();
			        fileDescriptor.filename = file.name;
			        fileDescriptor.mimeType = file.type;
			        fileDescriptor.size = file.size;
			        blobFolder.file(fileDescriptor.id + '/' + file.name, e.target.result);
					blobs.push(fileDescriptor)

		        	instance.fileCounter++;
			        // Check if ready to upload tool
		        	instance.checkPostReadyState(tool, zipfile);
		    	};
		    })(files[index]);
		    reader.readAsArrayBuffer(files[index]);
		}
		$('#modalWorkflowToolView').modal('hide');
	},

	cancel: function(e) {
		e.preventDefault();
		$('#modalWorkflowToolView').modal('hide');
	},

	checkReadyState: function(zipfile) {
		if(this.fileCounter == this.numFiles) {
			findJavaTools(zipfile);
		}
	},
	checkPostReadyState: function(tool, zipfile) {
		if(this.fileCounter == this.numFiles) {
			zipfile.file('tool.json', JSON.stringify(tool));
			//console.log(JSON.stringify(tool, undefined, 2));
			postTool(zipfile);
		}
	}
});

var JavaToolListView = Backbone.View.extend({
	tagName: 'select',
	events: {
		"change" : "onChange"	
	},

	initialize: function() {
		this.$el.attr('size', 8);
		//this.$el.attr('multiple', 'multiple');
	},

	render: function() {
		//$(this.el).append(new JavaToolListItemView({model: new JavaTool({toolClass: "", name: ""})}).render().el);
		_.each(this.model.models, function(javatool) {
			$(this.el).append(new JavaToolListItemView({model: javatool}).render().el);
		}, this);
		return this;
	},

	onChange: function(e) {
		e.preventDefault();
		var selected = $(this.el).val();
		javaToolCollection.each(function(javatool) {
			if(javatool.get('toolClass') === selected) {
				$('#java-tool-name').val(javatool.get('name'));
				$('#java-tool-version').val(javatool.get('version'));
				$('#java-tool-description').val(javatool.get('description'));	
				return false;
			}
		})
	}

});

var JavaToolListItemView = Backbone.View.extend({
	template: _.template($('#java-tool-list-item').html()),
	tagName: 'option',

	attributes: function() {
		return {
			value: this.model.get('toolClass')
		}
	},

	render: function() {
		$(this.el).html(this.template(this.model.toJSON()));
		return this;
	},
});

function findJavaTools(zip) {
    var blob = zip.generate({type:"blob"});
    var data = new FormData();
    data.append('tool', blob);
    
    var oReq = new XMLHttpRequest();
    oReq.open("POST", datawolfOptions.rest + "/executors");
    oReq.onreadystatechange = function() {
        if (oReq.readyState == 4) {
            var map = JSON.parse(this.responseText);

           	javaToolCollection = new JavaToolCollection();
            for(var key in map) {
            	var javatool = new JavaTool(map[key]);
            	javatool.set('toolClass', key);
	            //console.log(JSON.stringify(javatool, undefined, 2));
	            javaToolCollection.add(javatool);
            }
           	$('#jar-tools').html(new JavaToolListView({model: javaToolCollection}).render().el);
        }
    }
    oReq.send(data); 
}

// Helper functions
var createWorkflowTool = function(title, description, version, executor) {
	var tool = new WorkflowTool();
    tool.set('id', generateUUID());
    tool.set('title', title);
    tool.set('date', new Date());
    tool.set('description', description);
    tool.set('version', version);
    tool.set('executor', executor);
    tool.set('creator', currentUser.toJSON());

    return tool;
};

var createWorkflowToolData = function(title, description, mimeType) {
	var toolData = new WorkflowToolData();
	toolData.setId(generateUUID());
	toolData.setDataId(generateUUID());
	
	return updateWorkflowToolData(toolData, title, description, mimeType);
};

var updateWorkflowToolData = function(toolData, title, description, mimeType) {
	toolData.setTitle(title);
	toolData.setDescription(description);
	toolData.setMimeType(mimeType);

	return toolData;
}

var createWorkflowToolParameter = function(title, description, allowNull, type, hidden, value) {
	var param = new WorkflowToolParameter();
    param.setId(generateUUID());
    param.setParameterId(generateUUID());
    return updateWorkflowToolParameter(param, title, description, allowNull, type, hidden, value);
};

var updateWorkflowToolParameter = function(param, title, description, allowNull, type, hidden, value) {
    param.setTitle(title);
    param.setDescription(description);
    param.setAllowNull(allowNull);
    param.setType(type);
    param.setHidden(hidden);
    param.setValue(value);
	return param;
}

var createCommandLineOption = function(type, commandline) {
	var option = new CommandLineOption({type: type, commandline: commandline});
	return option;
}

var getOptionString = function(option) {
	var optionAsString = "";
	if(option.getFlag() != null && !_.isEmpty(option.getFlag())) {
		optionAsString += option.getFlag().trim() + " ";
	}

	switch(option.getType()) {
		case 'PARAMETER': 
			var param = commandLineOptionView.getParameter(option);
			optionAsString += param.get('title');
			optionAsString += '[';
			optionAsString += param.get('type');
			optionAsString += '] = ';
			optionAsString += param.get('value');
			break;
		case 'VALUE':
			if((option.getValue() != null) && (option.getValue().trim().length > 0)) {
				optionAsString += option.getValue().trim();
			}	
			break;
		case 'DATA':
			optionAsString += "file(";
				switch(option.getInputOutput()) {
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

				if((option.getFilename() != null) && (option.getFilename().trim().length > 0)) {
					optionAsString += option.getFilename().trim();
				} else {
					optionAsString += "AUTO";
				}

				if(!option.isCommandline()) {
					optionAsString += '[not passed]';
				}

				optionAsString += ")";

			break;
	}
	return optionAsString;
}
