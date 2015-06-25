define(function (require) {
  'use strict';

  var React = require("react");
  var FileUtil = require("spa/helpers/fileUtil");
  
  var ButtonBar = React.createClass({
	  propTypes: {
		  onFileChange: React.PropTypes.func.isRequired,
		  onFileSave: React.PropTypes.func.isRequired
	  },
	  getInitialState: function() {
	        return {
	        	saveButtonEnabled: false,
	        	showSpinner: false
	        };
	   },
	  openButtonClick: function() {
		  this.refs.file.getDOMNode().click();
	  },
	  loadFile: function() {
		  var f = this.refs.file.getDOMNode().files[0];
		  if (f) {
			  if (f.type.match(/application\/(x-)?pdf|text\/pdf/)) {
				  FileUtil.readFileAsBinary(f).then(this.props.onFileChange);
			  }
		  }
	  },
	  enableSaveButton: function() {
		  this.setState({saveButtonEnabled: true});
	  },
	  disableSaveButton: function() {
		  this.setState({saveButtonEnabled: false});
	  },
	  showSpinner: function() {
		  this.setState({showSpinner: true});
	  },
	  hideSpinner: function() {
		  this.setState({showSpinner: false});
	  },
	  saveFile: function() {
		  var formData = new FormData(this.refs.uploadForm.getDOMNode());
		  this.props.onFileSave(formData);
	  },
	  render: function() {
		 return <div> {this.state.showSpinner ? <img src="/css/select2-spinner.gif" /> : 
			 			<div>
		 					<form encType="multipart/form-data" ref="uploadForm">
		 						<input accept="pdf" style={{display: "none"}} name="pdfFile" type="file" id="file" ref="file" onChange={this.loadFile} />
		 					</form>
		 					<button type="button" className="btn btn-primary" onClick={this.openButtonClick}>Open a new pdf</button>
		 					<button type="button" disabled={!this.state.saveButtonEnabled} className="btn btn-primary" onClick={this.saveFile}>
		 						Save current pdf and annotations
		 					</button>
		 				</div>}
		 		</div>
	  }
  });
  
  return ButtonBar;
});