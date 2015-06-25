/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2; js-indent-level: 2; -*- */
define(function (require) {
  'use strict';
  
  var React = require("react");
  var FileUtil = require("spa/helpers/fileUtil");
  var Backbone = require("backbone");
  var Molgenis = window.molgenis;
  
  // Models
  var documentModel = new (require("spa/models/document"))();
  var marginaliaModel = new (require("spa/models/marginalia"))();

  // Components
  var Document = React.createFactory(require("jsx!spa/components/document"));
  var Marginalia = React.createFactory(require("jsx!spa/components/marginalia"));
  var ButtonBar = React.createFactory(require("jsx!component/ButtonBar"));
  
  var documentComponent = React.render(new Document({pdf: documentModel, marginalia: marginaliaModel}), document.getElementById("viewer"));
  var marginaliaComponent = React.render(new Marginalia({marginalia: marginaliaModel}), document.getElementById("marginalia"));
  
  var processFile = function(data) {
	  var upload = FileUtil.upload("/plugin/textmining/upload", data);
	  documentModel.loadFromData(data);
	  upload.then(function(result) {
		  var marginalia = JSON.parse(result);
		  marginaliaModel.reset(marginaliaModel.parse(marginalia));
		  topBarComponent.enableSaveButton();
	 });
  };
  
  var save = function(formData) {
	  //TODO does not work in IE < 10 (FormData)
	  topBarComponent.showSpinner();
	  $.ajax({
		  url: '/api/v1/Publication',
		  type: 'POST',
		  data: formData,
		  contentType: false,
		  processData:false,
		  async: true,
		  success: function(data, textStatus, jqXHR) {
			  topBarComponent.disableSaveButton();
			  var location = jqXHR.getResponseHeader('Location');
			  var id = location.substring(location.lastIndexOf('/') + 1)
			  saveMarginalia(id);
		   },
		   error: function() {
			   topBarComponent.hideSpinner();
		   }
	  });  
  };
  
  var topBarComponent = React.render(new ButtonBar({onFileChange: processFile, onFileSave: save}), document.getElementById("buttonBar"));
  
  var saveMarginalia = function(publicationId) {
	  $.ajax({
		  url: '/plugin/textmining/updatePublication/' + publicationId,
		  type: 'POST',
		  data: JSON.stringify(marginaliaModel.toJSON()),
		  contentType: 'application/json',
		  async: true,
		  success: function(data, textStatus, jqXHR) {
			  Molgenis.createAlert([{message: "Saved pdf."}], "success");
			  topBarComponent.hideSpinner();
		  }, 
		  error: function() {
			  Molgenis.createAlert([{message: "Could not save pdf."}], "error");
			  topBarComponent.hideSpinner();
		  }
	  });  
  };
  
  // Dispatch logic
  // Listen to model change callbacks -> trigger updates to components
  marginaliaModel.on("all", function(e, obj) {
    switch(e) {
    case "reset":
      documentModel.annotate(marginaliaModel.getActive());
      marginaliaComponent.forceUpdate();
      break;
    case "annotations:change":
      break;
    case "change:active":
    case "annotations:add":
    case "annotations:remove":
      documentModel.annotate(marginaliaModel.getActive());
      marginaliaComponent.forceUpdate();
      break;
    case "annotations:select":
      documentComponent.setState({select: obj});
      break;
    default:
      marginaliaComponent.forceUpdate();
    }
  });

  documentModel.on("all", function(e, obj) {
	switch(e) {
    case "change:raw":
      documentComponent.setState({
        fingerprint: documentModel.get("fingerprint")
      });
      break;
    case "change:binary":
      marginaliaModel.reset();
      break;
    case "pages:change:state":
      if(obj.get("state") == window.RenderingStates.HAS_CONTENT) {
        documentModel.annotate(marginaliaModel.getActive());
      }
      documentComponent.forceUpdate();
      break;
    case "pages:change:annotations":
      documentModel.annotate(marginaliaModel.getActive());
      documentComponent.forceUpdate();
      break;
    default:
      break;
    }
  });
  
 
});
