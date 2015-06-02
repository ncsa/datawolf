// Tracks the id of the currently displayed tool information
var previousTool = null;

// When editing tools, keep a copy to save with the new tool as reference
var oldTool = null;

var WorkflowToolListView = Backbone.View.extend({
    tagName: 'ul',
    id: 'workflowListItemView',
    className: 'workflowToolView',

    initialize: function() {
       this.$el.attr('size', '25');
       var self = this;
       this.model.bind("add", function(workflowTool) {
            $(self.el).append(new WorkflowToolListItemView({model: workflowTool}).render().el);
       });
       //this.$el.attr('draggable', 'true');
       //this.$el.bind('dragstart', _.bind(this.handleDragStart, this));
    },

    render: function(e) {
        //console.log('render list of tools');
        $(this.el).empty();
        _.each(this.model.models, function(workflowTool) {
            $(this.el).append(new WorkflowToolListItemView({model: workflowTool}).render().el);
        }, this);

        return this;
    },

    //handleDragStart: function(e) {
        //console.log(this.model.get('title'));
      //  e.originalEvent.dataTransfer.setData("Text", "test");
    //} 


});

var WorkflowToolListItemView = Backbone.View.extend({
    tagName: 'li',

    template: _.template($('#workflow-tool-list-item').html()),

    events: {
        "click" : "onClick",
        "mouseleave" : "hideDetails"
    },
    
    initialize: function() {
       this.$el.attr('draggable', 'true');
       this.$el.bind('dragstart', _.bind(this.handleDragStart, this));
       this.model.bind("destroy", this.close, this);
       //this.$el.bind('dragstart', _.bind(this._dragStartEvent, this));
    },

    attributes: function() {
        return {
            value: this.model.get('id')
        }
    },

    render: function(e) {
        $(this.el).html(this.template(this.model.toJSON()));
        $(this.el).find('.icon-wf-title').addClass(this.getToolClass(this.model.get('executor')));
        var popoverTitle = _.template($('#workflow-popover').html())
        var popoverContentTop = _.template($('#workflow-tool-popover-content-top').html());
        var popoverContentDesc = _.template($('#workflow-tool-popover-content-description').html());

        // Get Inputs
        var inputTitleTemplate = _.template($('#workflow-tool-popover-content-input-title').html());
        var popoverContentInputs = inputTitleTemplate(null);
        var inputs = new WorkflowToolDataCollection(this.model.get("inputs"));

        var tabContentTemplate = _.template($('#workflow-tool-popover-content').html());
        inputs.each(function(workflowToolData) {
            popoverContentInputs += tabContentTemplate(workflowToolData.toJSON());
        });
        var outputTitleTemplate = _.template($('#workflow-tool-popover-content-output-title').html());
        var outputs = new WorkflowToolDataCollection(this.model.get("outputs"));
        var popoverContentOutputs = outputTitleTemplate(null);
        outputs.each(function(workflowToolData) {
            popoverContentOutputs += tabContentTemplate(workflowToolData.toJSON());
        });

        var parameterTitleTemplate = _.template($('#workflow-tool-popover-content-parameter-title').html());
        var popoverContentParameters = parameterTitleTemplate(null);
        var parameters = new WorkflowToolParameterCollection(this.model.get('parameters'));
        parameters.each(function(workflowToolParameter) {
            popoverContentParameters += tabContentTemplate(workflowToolParameter.toJSON());
        });

        var popoverContentOpen = _.template($('#workflow-tool-popover-content-open').html());
        var popoverContentClose = _.template($('#workflow-tool-popover-content-close').html());

        var modelJSON = this.model.toJSON();
        var executors = JSON.parse(localStorage["executors"]);
        if(_.contains(executors, this.model.get('executor'))) {
            modelJSON.executorAvail = 'info-metadata-executor-available';
        } else {
            modelJSON.executorAvail = 'info-metadata-executor-unavailable';
        }
        //if(this.model.executor)
        
        var content = popoverContentTop(modelJSON) + popoverContentOpen(null) + popoverContentDesc(this.model.toJSON()) 
            + popoverContentInputs + popoverContentClose(null) + popoverContentOutputs + popoverContentClose(null) 
            + popoverContentParameters + popoverContentClose(null);
        $(this.el).popover({html: true, title: popoverTitle(this.model.toJSON()), content: content, trigger: 'manual'});

        return this;
    },

    close: function() {
        $(this.el).unbind();
        $(this.el).remove();
    },

    handleDragStart: function(e) {
        // Here we'll want to simply transfer the ID, not the heavyweight model
        var modelUri = this.model.get('id');
        e.originalEvent.dataTransfer.setData("Text", modelUri);
        toolDrop = true;
    },

    onClick: function(e) {
        //console.log("mouse click");
        //var selection = $('#workflowSelector').val();
        e.preventDefault();
        $('.highlight').removeClass('highlight');
        $(this.el).addClass('highlight');

        // if(showToolInfo) {
        //     $(this.el).popover('toggle');
        // }
        //$(this.el).popover('toggle');
    },

    hideDetails: function() {

    },

    getToolClass: function(executor) {
        if(executor === 'java') {
            return "icon-tool-blue";
        } else if(executor === 'commandline') {
            return "icon-tool-green"
        } else if(executor === 'hpc') {
            return "icon-tool-yellow";
        } else {
            return "icon-tool-red";
        }
        //if(title === 'eAIRS File-Transfer') {
        //    return "icon-tool-green";
        //} else if(title === 'eAIRS CFD Parameters') {
        //    return "icon-tool-yellow";
        //} else if(title === 'eAIRS Results') {
        //    return "icon-tool-red";
        //} else if(title === 'eAIRS-CFD-Tachyon-MPI') {
        //    return "icon-tool-blue";
        //} else {
        //    return "icon-tool-green";
        //}
    } 
       
});

var WorkflowToolInfoView = Backbone.View.extend({
    initialize: function() {

    },

    render: function() {
        return this;
    }
});

var WorkflowToolButtonBar = Backbone.View.extend({
    template: _.template($("#workflow-tool-buttons").html()),

    events: {
        "click button#workflow-tool-info-btn" : "workflowToolInfo",
        "click button#new-workflow-tool-btn" : "createWorkflowTool",
        "select .tool-select" : "handleSelection",
        "click button#delete-tool-btn" : "deleteWorkflowTool",
        "click button#workflow-tool-export-btn" : "exportWorkflowTool",
        "click button#workflow-tool-import-btn" : "importWorkflowTool",
        "click button#workflow-tool-open-btn" : "editWorkflowTool"
    },

    initialize: function() {
        this.popoverVisible = false;
    },

    render: function(e) {
        $(this.el).html(this.template());
        
        var popoverTitle = _.template($('#workflow-tool-popover').html());
        var toolTypes = _.template($('#select-tool-type').html());
        //var popover = $(this.el).popover({html: true, title: popoverTitle({title: 'Select Tool Type'}), content: toolTypes(), trigger: 'manual'});
        $(this.el).popover({html: true, title: popoverTitle({title: 'Select Tool Type'}), content: toolTypes(), trigger: 'manual'});
        $(this.el).find("[data-toggle='tooltip']").tooltip({'container':'body', 'delay': {show: 500 }});         

        return this;
    },

    workflowToolInfo: function() {
        var selectedTool = $('#workflowListItemView').find(".highlight");
        if(selectedTool!=null && selectedTool.length !=0){
            if(previousTool==null){
                selectedTool.popover('toggle');
                previousTool=selectedTool;                
            }
            else if(selectedTool.attr("value")==previousTool.attr("value")){
                selectedTool.popover('toggle');
                previousTool=null;    
            }
            else{
                previousTool.popover('toggle');
                selectedTool.popover('toggle');
                previousTool=selectedTool;                                
            }
        }
        else{
            if(previousTool !=null){
                previousTool.popover('toggle');
                previousTool=null;
            }
        }
        //showToolInfo = !showToolInfo;
    },

    createWorkflowTool: function() {
        //$('#tool-modal-content').html(new SelectToolTypeView().render().el);
        //$('#modalWorkflowToolView').modal('show');
        if(this.popoverVisible) {
            $(this.el).popover('hide');
            this.popoverVisible = false;
        } else {
            this.popoverVisible = true;
            $(this.el).popover('show');
            var self = this;
            $('#tool-select').change(function(e) {
                var selection = $('#tool-select').find(':selected').val();
                if(!_.isEmpty(selection.trim())) {
                    $(self.el).popover('hide');
                    self.popoverVisible = false;
                }

                switch(selection) {
                    case 'commandline':
                        showCommandLineToolWizard();
                        break;
                    case 'java':
                        showJavaToolWizard(); 
                        break;
                    case 'hpc':
                        showHPCToolWizard();
                        break;
                }

            });
        }
    },

    deleteWorkflowTool: function(e) {
        e.preventDefault();
        var selectedToolId = $('.highlight').attr('value');
        console.log("delete workflow tool = "+selectedToolId);
        if(selectedToolId != null) {
            var workflowTool = null;
            workflowToolCollection.each(function(tool) {
                if(tool.get('id') === selectedToolId) {
                    workflowTool = tool;
                    return false;
                }
            });

            if(workflowTool != null) {
                var showWarning = false;
                // map of tools to delete from workflows
                var workflowMap = {};
                workflowCollection.each(function(workflow) {
                    var index = 0;
                    workflow.getSteps().each(function(workflowStep) {
                        if(workflowStep.getTool().get('id') === selectedToolId) {
                            showWarning = true;
                            workflowMap[workflow.get('id')] = index;
                            return false;
                        }
                        index++;
                    });
                });
                var deleteTool = false;

                if(showWarning) {
                    var performDelete = window.confirm("Warning - tool is used in 1 or more workflows. Deleting this tool will update those workflows.");
                    if(performDelete) {
                        deleteTool = true;    
                    } 
                } else {
                    deleteTool = true;
                }
                
                if(deleteTool) {
                    workflowCollection.each(function(workflow) {
                        for(var key in workflowMap) {
                            if(workflow.get('id') === key) {
                                var stepIndex = workflowMap[key];
                                var steps = workflow.get('steps');  
                                steps.splice(stepIndex, 1);
                                workflow.save({steps: steps}, {
                                    wait: true,

                                    success: function(model, response) {
                                        console.log("updated workflow - success");
                                    },

                                    error: function(model, error) {
                                        console.log("failed to update workflow");
                                    }
                                });
                            }
                        }
                    });
                    workflowTool.destroy({
                        wait: true,
                        dataType: "text",
                        success: function(model, response) {
                            console.log("delete tool - success");
                        },

                        error: function(model, response) {
                            console.log("delete tool - failed. " + response);
                        }
                    }); 
                }
            }
        }
    },

    importWorkflowTool: function() {
        eventBus.trigger("clicked:importworkflowtool", null);
    },

    exportWorkflowTool: function() {
        var selectedToolId = $('.highlight').attr('value');
        if(selectedToolId != null) {
            var workflowTool = null;
            workflowToolCollection.each(function(tool) {
                if(tool.get('id') === selectedToolId) {
                    workflowTool = tool;
                    return false;
                }
            });

            if(workflowTool != null) {
                eventBus.trigger("clicked:exportworkflowtool", selectedToolId);
            }
        }
    },

    editWorkflowTool: function() {
        var selectedToolId = $('.highlight').attr('value');
        if(selectedToolId != null) {
            var workflowTool = null;
            workflowToolCollection.each(function(tool) {
                if(tool.get('id') === selectedToolId) {
                    workflowTool = tool;
                    return false;
                }
            });

            // TODO add support for editing Java and HPC tools
            if(workflowTool != null && workflowTool.get('executor') === 'commandline') {
                oldTool = workflowTool;
                showCommandLineToolWizard();
            } else {
                window.alert("Editing Java and HPC tools is currently not supported.");
            }
        }
    }
});

var ImportWorkflowToolView = Backbone.View.extend({
    id: "import-workflow-tool-div",
    template: _.template($("#import-workflow-tool-template").html()),

    events: {
        "click button#import-workflow-tool-btn" : "importWorkflowTool",
        "click button#cancel" : "cancel"
    },

    render: function() {
        $(this.el).html(this.template());
        return this;
    },

    importWorkflowTool: function(e) {
        e.preventDefault();

        var toolFile = $("#import-workflow-tool-form")[0][0].files[0];
        
        if(toolFile != undefined) {        
            var formData = new FormData();
            formData.append("tool", toolFile);
            $.ajax({
                type: "POST",
                url: datawolfOptions.rest + "/workflowtools",
                data: formData,
                contentType: false,
                processData: false,
                success: function(response) {
                    // Response is array of tool ids
                    var toolIds = response;
                    for(var index = 0; index < toolIds.length; index++) {
                        addWorkflowTool(toolIds[index]);
                    }
                },
                error: function(response) {
                     alert('error: '+JSON.stringify(msg));
                }
            });
            $('#modal-import-view').modal('hide');
        } else {
            alert("Error - no file selected");
        } 
        
    },

    cancel: function(e) {
        e.preventDefault();
        $('#modal-import-view').modal('hide');
    }

});

var showCommandLineToolWizard = function() {
    $('#tool-modal-content').html(new CommandLineView().render().el);
    commandLineBasicView = new CommandLineBasicTab();
    commandLineOptionView = new CommandLineOptionTab();
    commandLineFileView = new CommandLineFileTab();
    commandLineEnvView = new CommandLineEnvTab();
    $('#tool-modal-content').find('#wizard-pane1').html(commandLineBasicView.render().el);
    $('#tool-modal-content').find('#wizard-pane2').html(commandLineOptionView.render().el);
    $('#tool-modal-content').find('#wizard-pane3').html(commandLineFileView.render().el);
    $('#tool-modal-content').find('#wizard-pane4').html(commandLineEnvView.render().el);
    $('#tool-modal-content').find('#wf-options-list').html(commandLineOptionView.getCommandLineOptionsListView().render().el);
    $('#modalWorkflowToolView').modal('show');
};

var showJavaToolWizard = function() {
    $('#tool-modal-content').html(new JavaToolSelectionTab().render().el);
    $('#modalWorkflowToolView').modal('show');
};

var showHPCToolWizard = function() {
    $('#tool-modal-content').html(new HPCToolView().render().el);
    commandLineBasicView = new CommandLineBasicTab();
    commandLineOptionView = new HPCToolOptionTab();
    $('#tool-modal-content').find('#wizard-pane1').html(commandLineBasicView.render().el);
    $('#tool-modal-content').find('#wizard-pane2').html(commandLineOptionView.render().el);
    $('#tool-modal-content').find('#wf-options-list').html(commandLineOptionView.getCommandLineOptionsListView().render().el);
    $('#modalWorkflowToolView').modal('show');
};
