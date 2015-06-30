define(function (require) {
  'use strict';

  var React = require("react");
  var FileUtil = require("spa/helpers/fileUtil");
  require("bootstrap");
  var restApi = new window.molgenis.RestClient();
 
  var ButtonBar = React.createClass({
	  propTypes: {
		  onFileChange: React.PropTypes.func.isRequired,
		  onSave: React.PropTypes.func.isRequired,
		  onPdfSelect: React.PropTypes.func.isRequired
	  },
	  getInitialState: function() {
	        return {
	        	saveButtonEnabled: false,
	        	showSpinner: false,
	        	publications: []
	        };
	   },
	  openNewButtonClick: function() {
		  this.refs.file.getDOMNode().click();
	  },
	  openExistingButtonClick: function() {
		  var self = this;
		  restApi.getAsync('/api/v1/Publication',{expand:['pdfFile']}, function(result) {
			  self.$modal.modal('show');
			  self.setState({publications: result.items});
		 });
	  },
	  onPdfRowClick: function(pdfFile) {
		  this.props.onPdfSelect(pdfFile);  
		  this.$modal.modal('hide');
	  },
	  loadFile: function() {
		  var f = this.refs.file.getDOMNode().files[0];
		  var self = this;
		  if (f) {
			  if (f.type.match(/application\/(x-)?pdf|text\/pdf/)) {
				  FileUtil.readFileAsBinary(f).then(function(data) {
					  self.props.onFileChange(data, f);
				  });
			  }
		  }
	  },
	  showSpinner: function() {
		  this.setState({showSpinner: true});
	  },
	  hideSpinner: function() {
		  this.setState({showSpinner: false});
	  },
	  disableSaveButton: function() {
		  this.setState({saveButtonEnabled: false});
	  },
	  enableSaveButton: function() {
		  this.setState({saveButtonEnabled: true});
	  },
	  componentDidMount: function() {
		  this.$modal =  $(this.refs.modal.getDOMNode());
	  },
	  componentWillUnmount: function() {
    	  this.$modal.modal('hide'); // remove modal backdrop
    	  this.$modal.off();
    	  this.$modal.data('bs.modal', null); // see http://stackoverflow.com/a/18169689
      },
      render: function() {	 
    	  var self = this;
		  
		  return <div>
		  
		 			<input accept="pdf" style={{display: "none"}} name="pdfFile" type="file" id="file" ref="file" onChange={this.loadFile} />
		 			 
		 			{this.state.showSpinner ? <img src="/css/select2-spinner.gif" /> : 
		 				 <div>
		 				 	<button type="button" className="btn btn-primary" onClick={this.openNewButtonClick}>Open new pdf</button>
		 				 	<button type="button" className="btn btn-primary" onClick={this.openExistingButtonClick}>Open existing pdf</button>
		 				 	<button type="button" className="btn btn-primary" disabled={!this.state.saveButtonEnabled} onClick={this.props.onSave}>Save changes</button>
		 				 </div>}
		 			
		 			<div className="modal" tabIndex="-1" ref="modal" role="dialog">
		 				<div className="modal-dialog" role="document">
		 			    	<div className="modal-content">
		 			    		<div className="modal-header">
		 			    			<button type="button" className="close" data-dismiss="modal" aria-label="Close"><span aria-hidden="true">&times;</span></button>
		 			    			<h4 className="modal-title">Choose pdf</h4>
		 			    		</div>
		 			    		<div className="modal-body">
		 			    			<table className="table table-hover">
		 			    				<thead>
		 			    	        		<tr><th>Name</th></tr>
		 			    	        	</thead>
		 			    	        	<tbody>
		 			    	        		{this.state.publications.map(function(publication, i) {
		 			    	        			return <tr key={i} onClick={self.onPdfRowClick.bind(null, publication.pdfFile)}><td>{publication.pdfFile.filename}</td></tr>
		 			    	        		})}
		 			    	        	</tbody>
		 			    	        </table>
		 			    		</div>
		 			    	</div>
		 			    </div>
		 			</div>
		 			
		 		</div>
	  }
  });
  
  return ButtonBar;
});