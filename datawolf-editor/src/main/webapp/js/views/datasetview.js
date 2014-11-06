var previousDataset=null;

var DatasetListView = Backbone.View.extend({
    tagName: 'ul',
    id: 'datasetListItemView',
    className: 'workflowDatasetView',

    initialize: function() {
        var self = this;
        this.model.bind("add", function(dataset) {
            $(self.el).append(new DatasetListItemView({model: dataset}).render().el);
        });
    },

    render: function(e) {
        $(this.el).empty();
        _.each(this.model.models, function(workflowDataset) {
            $(this.el).append(new DatasetListItemView({model: workflowDataset}).render().el);
        }, this);

        return this;
    },

});

var DatasetListItemView = Backbone.View.extend({
    tagName: 'li',
    template: _.template($('#dataset-list-item').html()),

    events: {
        "click" : "onClick",
    },

    initialize: function() {
        this.$el.attr('draggable', 'true');
        this.$el.bind('dragstart', _.bind(this.handleDragStart, this));
        this.$el.bind('dragend', _.bind(this.handleDragEnd, this));
        this.model.bind("change", this.render, this);
        this.model.bind("destroy", this.close, this);
    },

    attributes: function() {
        return {
            value: this.model.get('title'),
            id: this.model.get('id')
        }
    },

    render: function(e) {
        $(this.el).html(this.template(this.model.toJSON()));
        return this;
    },

    close: function() {
        $(this.el).unbind();
        $(this.el).remove();
    },

    handleDragStart: function(e) {
        $('.highlight').removeClass('highlight');
        // we want to simply transfer the ID, not the heavyweight model
        var currdataset = this.model.get('id');
        
        e.originalEvent.dataTransfer.setData("Text", currdataset);
        datasetDrop = true;
        $(this.el).css("color","black");
    },
    
    handleDragEnd: function(e) {
        $(this.el).css("color","white");
    },

    onClick: function(e) {
        e.preventDefault();
        $('.highlight').removeClass('highlight');
        $(this.el).addClass('highlight');
    }
       
});


var DatasetInfoView = Backbone.View.extend({
    initialize: function() {

    },

    render: function() {
        return this;
    }
});

var DatasetButtonView = Backbone.View.extend({
    template: _.template($("#dataset-buttons").html()),

    events: {
        "click button#dataset-info-btn" : "datasetInfo",
        "click button#new-dataset" : "datasetCreate",
        "click button#delete-dataset" : "datasetDelete",
        "click button#dataset-download-btn" : "datasetDownload",
    },

    render: function(e) {
        $(this.el).html(this.template());
        $(this.el).find("[data-toggle='tooltip']").tooltip({'container':'body', 'delay': {show: 500 } });
        return this;
    },

    datasetDownload: function(e) {
        e.preventDefault();
        var selectedDataset = $('#datasetListItemView').find(".highlight");
        $('.highlight').removeClass('highlight');

        // console.log(selectedDataset);
        if(selectedDataset!=null && selectedDataset.length !=0){
            var dsid = selectedDataset.attr("id");
            var dataset = getDataset(dsid);
            if(dataset.get("fileDescriptors").length == 1) {
                window.location.href = datawolfOptions.rest + "/datasets/" + dsid + "/" + dataset.get("fileDescriptors")[0].id + "/file";
            } else {
                window.location.href = datawolfOptions.rest + "/datasets/" + dsid + "/zip";
            }
        }
    },

    datasetDelete: function(e) {
        // TODO implement delete workflow completely (both REST service and client)
        e.preventDefault();
        var selectedDataset = $('#datasetListItemView').find(".highlight");
        // console.log(selectedDataset);
        if(selectedDataset!=null && selectedDataset.length !=0){
            var dsid = selectedDataset.attr("id");
            var model = getDataset(dsid);
            model.destroy({
                wait: true,
            
                success: function(model, response) {
                    console.log("deleted dataset - success: ");
                },

                error: function(model, response) {
                    console.log("deleted dataset - failed"+response);
                }

            });
        }
    },

    datasetInfo: function() {
        var selectedDataset = $('#datasetListItemView').find(".highlight");
        // console.log(selectedDataset);
        if(selectedDataset!=null && selectedDataset.length !=0){
            var dsid = selectedDataset.attr("id");
            var model = getDataset(dsid);
            var json = model.toJSON();
            if(json.creator==null){
                json.creator="";
            }

            var popoverTitle = _.template($('#dataset-popover').html())
            var popoverContent = _.template($('#dataset-popover-content').html());
            selectedDataset.popover({html: true, title: popoverTitle(model.toJSON()), content: popoverContent(json), trigger: 'manual'});

            if(previousDataset==null){
                selectedDataset.popover('toggle');
                previousDataset=selectedDataset;                
            }
            else if(selectedDataset.attr("id")==previousDataset.attr("id")){
                selectedDataset.popover('toggle');
                previousDataset=null;    
            }
            else{
                previousDataset.popover('toggle');
                selectedDataset.popover('toggle');
                previousDataset=selectedDataset;                                
            }
        }
        else{
            if(previousDataset !=null){
                previousDataset.popover('toggle');
                previousDataset=null;
            }
        }

    },

    datasetCreate: function(e){
        $('.highlight').removeClass('highlight');
        // console.log(currentUser.get('email'));
        // console.log($("#dataset-upload-form"));
        // $("#dataset-upload-form").find('[name=useremail]').val(currentUser.get('email'));
        // console.log('rendering');
        // console.log($("#dataset-upload-form").serialize());
        e.preventDefault();
        eventBus.trigger("clicked:newdataset", null);  
    },
});


var NewDatasetView = Backbone.View.extend({
    template: _.template($("#new-dataset").html()),

    events: {
        "click button#save-dataset" : "uploadDataset",
        "click button#cancel" : "cancelDataset"
    },

    render: function(e) {
        $(this.el).html(this.template(this.model.toJSON()));
        return this;
    },

    uploadDataset: function(e) {
        // $("#dataset-upload-form").submit();
        e.preventDefault();

        var title = $('input[name=datasetTitle]').val();
        var description = $('input[name=datasetDescription]').val();
        var email = currentUser.get('email');

        var data=new FormData();
        data.append("uploadedFile", $('#dataset-upload-form')[0][2].files[0]);
        data.append("useremail", email);
        data.append("description", description);
        data.append("title", title);
        $.ajax({
            type: "POST",
            url: datawolfOptions.rest + "/datasets",
            data: data,
            contentType: false,
            processData: false,
            success: function(msg) {
                addDataset(msg);
            },
            // error: function(msg) {
            //     alert('error: '+JSON.stringify(msg));
            // }

        }); 
        //eventBus.trigger("clicked:updateDatasets", null);
        $('#modalDatasetView').modal('hide');
    },

    cancelDataset: function(e) {
        e.preventDefault();
        $('#modalDatasetView').modal('hide');
    }

    
});

