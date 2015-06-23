define(function (require) {
  'use strict';

  var React = require("react");
  
  var UploadButton = React.createClass({
	  showUploadForm: function() {
		  React.render(molgenis.ui.Form({
				mode: 'create',
				entity : 'FileMeta',
				modal: true,
				onSubmitSuccess : 
			}), $('<div>')[0]);
	  },
	  render: function() {
		  return <button id="uploadButton" className="btn btn-primary" onClick={this.showUploadForm}>Upload a new pdf</button>
	  }
  });
  
  return UploadButton;
});