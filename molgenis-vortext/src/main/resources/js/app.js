/* -*- tab-width: 2; indent-tabs-mode: nil; c-basic-offset: 2; js-indent-level: 2; -*- */
define(function (require) {
  'use strict';
  
  var React = require("react");
  var FileUtil = require("spa/helpers/fileUtil");
  var Backbone = require("backbone");
  
  // Models
  var documentModel = new (require("spa/models/document"))();
  var marginaliaModel = new (require("spa/models/marginalia"))();

  // Components
  var Document = React.createFactory(require("jsx!spa/components/document"));
  var Marginalia = React.createFactory(require("jsx!spa/components/marginalia"));

  var documentComponent = React.render(
		    new Document({pdf: documentModel, marginalia: marginaliaModel}),
		    document.getElementById("viewer")
  		);
  
  var marginaliaComponent = React.render(
		    new Marginalia({marginalia: marginaliaModel}),
		    document.getElementById("marginalia")
		);
  
  // Set CSRF
  var _sync = Backbone.sync;
  Backbone.sync = function(method, model, options){
    options.beforeSend = function(xhr){
      xhr.setRequestHeader('X-CSRF-Token', CSRF_TOKEN);
    };
    return _sync(method, model, options);
  };
  
  $('#uploadPdfButton').on('click', function() {
	  $('#file').click();
  });
  
  $('#file').on('change', function() {
	  var f = this.files[0];
	  
	  if (f) {
		  if (f.type.match(/application\/(x-)?pdf|text\/pdf/)) {
			  FileUtil.readFileAsBinary(f).then(processFile);
		  }
	  }
  });
  
  var processFile = function(data) {
	    var upload = FileUtil.upload("https://robot-reviewer.vortext.systems/topologies/gen2phen", data);
	    documentModel.loadFromData(data);
	    upload.then(function(result) {
	      var marginalia = JSON.parse(result);
	      marginaliaModel.reset(marginaliaModel.parse(marginalia));
	    });
  };
  
  
//  $('#uploadPdfButton').on('click', function() {
//	  React.render(molgenis.ui.Form({
//			mode: 'create',
//			entity: 'MutationArticle',
//			modal: true,
//			onSubmitSuccess: fileUploaded
//		}), document.getElementById('createForm'));
//  });
  
//  var fileUploaded = function(response) {
//	  var pdfUri = reponse.location;
//	  
//	  var request = new XMLHttpRequest();
//      request.open("GET", pdfUri, true);
//      request.responseType = "arraybuffer";
//      request.onload = function (e) {
//    	  var arrayBuffer = request.response; // Note: not request.responseText
//    	  if (arrayBuffer) {
//    		  var byteArray = new Uint8Array(arrayBuffer);
//    		  processFile(byteArray);
//    	  }
//      };
//      request.send(null);
//  };
  

  /*
   * 
  // Components
  var UploadButton = React.createFactory(require("jsx!component/UploadButton"));
  var uploadButtonComponent = React.render(
		  new UploadButton({onLoad: processUpload}),
		  document.getElementById("upload")
  		);
  
  var processUpload = function(data) {
	  alert(data);
  };
  
 
    var _ = require("underscore");
  var FileUtil = require("spa/helpers/fileUtil");
   var Backbone = require("backbone");
 
    // Set CSRF
  var _sync = Backbone.sync;
  Backbone.sync = function(method, model, options){
    options.beforeSend = function(xhr){
      xhr.setRequestHeader('X-CSRF-Token', CSRF_TOKEN);
    };
    return _sync(method, model, options);
  };
  
  // Models
  var documentModel = new (require("spa/models/document"))();
  var marginaliaModel = new (require("spa/models/marginalia"))();

  // Components
  var TopBar = React.createFactory(require("jsx!components/topBar"));
  var Document = React.createFactory(require("jsx!spa/components/document"));
  var Marginalia = React.createFactory(require("jsx!spa/components/marginalia"));

  var process = function(data) {
  //  var upload = FileUtil.upload("/topologies/gen2phen", data);
    documentModel.loadFromData(data);
    //upload.then(function(result) {
     // var marginalia = JSON.parse(result);
      //marginaliaModel.reset(marginaliaModel.parse(marginalia));
    //});
  };

  var topBarComponent = React.render(
    new TopBar({
      callback: process,
      accept: ".pdf",
      mimeType: /application\/(x-)?pdf|text\/pdf/
    }),
    document.getElementById("top-bar")
  );
	
  
  var documentComponent = React.render(
    new Document({pdf: documentModel, marginalia: marginaliaModel}),
    document.getElementById("viewer")
  );

  var request = new XMLHttpRequest();
  request.open("GET", "/files/AAAACTQ25MS4HKBAMYH3IOAAAE", true);
  request.responseType = "arraybuffer";
  request.onload = function (e) {
    var arrayBuffer = request.response; // Note: not request.responseText
    if (arrayBuffer) {
      var byteArray = new Uint8Array(arrayBuffer);
      process(byteArray);
    }
  };
  request.send(null);
  
  var marginaliaComponent = React.render(
    new Marginalia({marginalia: marginaliaModel}),
    document.getElementById("marginalia")
  );
	
  */
  
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
