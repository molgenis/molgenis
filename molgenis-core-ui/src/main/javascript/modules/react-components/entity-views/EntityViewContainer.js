import React from 'react';
import RestClientV2 from '../../rest-client/RestClientV2';
import $ from 'jquery';

import { Button } from '../Button';
import { EntityViewModal } from './EntityViewModal'; 
import { EntityViewContent } from './EntityViewContent';

var api = new RestClientV2();

var EntityViewContainer = React.createClass({
	displayName: 'EntityViewContainer',
	propTypes: {
		tableContentUrl: React.PropTypes.string.isRequired
	},
	getInitialState: function() {
		return {
			tableContent : null,
			isOpen: false,
			viewName: null,
			masterEntityName: null,
			refresh: true,
			showAttributeMappingTable: false,
			viewToEdit:null
		}
	},
	componentDidMount: function() {
		this.state.refresh ? this._retrieveEntityViews() : null;
	},
	render: function() {
		return <div className='row'>
			<div className='col-md-6'>
				<Button 
					id='add-new-view-btn' 
					icon='plus' 
					text='Add Entity view' 
					onClick={this._openModal} 
					style='primary'
					type='button'
				/>
				
				<EntityViewContent 
					tableContent={this.state.tableContent}
					entityEditFunction={this._editEntityView}
					entityDeleteFunction={this._deleteEntityView}
				/>
						
				<EntityViewModal 
					isOpen={this.state.isOpen}
					hideModal={this._hideModal}
					saveEntityView={this._saveEntityView}
					viewName={this.state.viewName}
					inputOnValueChange={this._setViewName}
					entitySelectOnValueChange={this._setMasterEntityName}
				/>
		  </div>
	  </div>
	},
	_retrieveEntityViews: function() {
		 var self = this;
		 $.ajax({
		     type: 'GET',
		     url: molgenis.getContextUrl() + '/get-entity-views',
		     success: function(data) {
		    	 self.setState({tableContent:data, refresh:false});
		     }
		})	            
	},
	_saveEntityView: function() {
		var self = this;
		$.ajax({
		     type: 'POST',
		     url: molgenis.getContextUrl() + '/add-entity-view',
		     data: { 
		    	 viewName: self.state.viewName, 
		    	 masterEntityName: self.state.masterEntityName 
	    	 },
		     success: function(data) {
		    	 self.setState({isOpen:false, refresh:true, viewName:null, masterEntityName:null});
		     }
		})
	},
	_openModal: function() {
		this.setState({isOpen: true});
	},	 
	_hideModal: function() {
		this.setState({isOpen: false});
	},
	_setViewName: function(viewName) {
		this.setState({viewName: viewName.value});		
	},
	_setMasterEntityName: function(masterEntityName) {
		this.setState({masterEntityName: masterEntityName.value.simpleName});
	},
	_editEntityView: function(row) {
		this.setState({viewToEdit: row.viewName, showAttributeMappingTable: true});
	},
	_deleteEntityView: function(row) {
		console.log('sneaky delete');
//		var self = this;
//		$.ajax({
//		     type: 'POST',
//		     url: molgenis.getContextUrl() + '/delete-entity-view',
//		     data: { 
//		    	 viewName: row.viewName 
//	    	 },
//		     success: function() {
//		    	 self.setState({refresh:true});
//		     }
//		})
	}
});

export { EntityViewContainer };
export default React.createFactory(EntityViewContainer);