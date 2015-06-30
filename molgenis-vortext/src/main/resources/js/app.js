/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2; js-indent-level: 2; -*- */
define(function (require) {
  'use strict';
  
  var _ = require("underscore");
  var React = require("react");
  var FileUtil = require("spa/helpers/fileUtil");
  var Backbone = require("backbone");
  var Molgenis = window.molgenis;
  var self = this;
  
  // Models
  var documentModel = new (require("spa/models/document"))();
  var marginaliaModel = new (require("spa/models/marginalia"))();

  // Components
  var Document = React.createFactory(require("jsx!spa/components/document"));
  var Marginalia = React.createFactory(require("jsx!spa/components/marginalia"));
  var ButtonBar = React.createFactory(require("jsx!component/ButtonBar"));
  
  var documentComponent = React.render(new Document({pdf: documentModel, marginalia: marginaliaModel}), document.getElementById("viewer"));
  var marginaliaComponent = React.render(new Marginalia({marginalia: marginaliaModel}), document.getElementById("marginalia"));
  
  /**
   * Show the new opened pdf in the viewer and upload it to the server to be annotated 
   */
  var processFile = function(data, file) {
	  buttonBarComponent.showSpinner();
	  
	  documentModel.loadFromData(data);
	  
	  var uri = "/plugin/textmining/annotate?filename=" + encodeURIComponent(file.name) + "&size=" + file.size;
	  FileUtil.upload(uri, data).then(function(result) {
		  var marginalia = JSON.parse(result);
		  marginaliaModel.reset(marginaliaModel.parse(marginalia));
		 
		  buttonBarComponent.hideSpinner();
	 });
  };
  
  /**
   * Save the currently visible annotations
   */
  var saveAnnotations = function() {
	  buttonBarComponent.disableSaveButton();
	  buttonBarComponent.showSpinner();
	  
	  $.ajax({
		  url: "/plugin/textmining/save",
	      type: "POST",
	      contentType : 'application/json',
	      async: true,
	      data: JSON.stringify({marginalia: marginaliaModel.toJSON()}),
	      success: function() {
	    	  buttonBarComponent.hideSpinner();
	      },
	      error: function() {
	    	  buttonBarComponent.hideSpinner();
	      }
	  }); 
  };
  
  var openExistingPdf = function(pdfFile) {
	  showPdf(pdfFile.id);
  }
  
  var buttonBarComponent = React.render(new ButtonBar({onFileChange: processFile, onSave: saveAnnotations, onPdfSelect: openExistingPdf}), document.getElementById("buttonBar"));

  /**
   * Download a pdf with marginalia and show it
   */
  var showPdf = function(fileMetaId) {
	  buttonBarComponent.disableSaveButton();
	  buttonBarComponent.showSpinner();
	
	  documentModel.loadFromUrl("/files/" + fileMetaId);
	  
	  $.ajax({
		  url: "/plugin/textmining/" + fileMetaId + "/annotations",
	      type: "GET",
	      async: true,
	      success: function(marginalia) {
	    	  marginaliaModel.reset(marginaliaModel.parse(marginalia));
	    	  buttonBarComponent.hideSpinner();
		  }
	  });
  }
  
  //Check if there is a previous viewed pdf (from session)
  if (FILE_META_ID !== "") {
	 showPdf(FILE_META_ID);
  }
  
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
		  documentModel.annotate(marginaliaModel.getActive());
		  marginaliaComponent.forceUpdate();
		  break;
	  case "annotations:add":
	  case "annotations:remove":
		  documentModel.annotate(marginaliaModel.getActive());
		  marginaliaComponent.forceUpdate();
		  buttonBarComponent.enableSaveButton();
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
