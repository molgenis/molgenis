import React from 'react';
import RestClientV2 from '../../rest-client/RestClientV2';
import $ from 'jquery';

import { Button } from '../Button';
import { EntityViewModal } from './EntityViewModal'; 
import { EntityViewContent } from './EntityViewContent';

var api = new RestClientV2();

/**
 * EntityViewContainer
 * 
 * Props:
 *  - tableContentUrl 
 *  	Url from which the table content should be retrieved. Now hardcoded call to the ViewConfigurationController
 * 
 * Renders:
 *  - Button 
 *  	To add new EntityViews
 *  - EntityViewContent 
 *  	Content component containing Tables
 *  - EntityViewModal 
 *  	Activated when pressing the Button
 *  
 * State:
 *  - tableContent 
 *  	Set by retrieving all the EntityViews from the server
 *  - isOpen 
 *  	Boolean deciding if the modal should be open
 *  - viewName 
 *  	The viewName filled in in the modal form, submitted to the server when creating a new EntityView
 *  - masterEntityName 
 *  	The name of the selected entity in the modal form, submitted to the server when creating a new EntityView
 *  - refresh 
 *  	Boolean deciding if the server should be called again to refresh the list of EntityViews e.g. on creation or delete
 *  - tableMode
 *  	Which table should be shown, EntityView table or AttributeMapping table
 *  - viewToEdit
 *  	Which EntityView the attributeMappingTable should be loaded for
 *  - masterEntityToEdit
 *  	Which MasterEntity the attributeMappingTable should be loaded for
 */
var EntityViewContainer = React.createClass({
	displayName: 'EntityViewContainer',
	propTypes: {
		tableContentUrl: React.PropTypes.string.isRequired
	},
	getInitialState: function() {
		return {
			tableContent : null,
			viewName: null,
			masterEntityName: null,
			refresh: true,
			isOpen: false,
			rowToEdit: null,
			tableMode: 'ENTITY_VIEW_TABLE'
		}
	},
	render: function() {
		// If a delete or add has been done, retrieve entity views again
		this.state.refresh ? this._retrieveEntityViews() : null;
		return <div className='row'>
			<div className='col-md-6'>
				{this.state.tableMode === 'ENTITY_VIEW_TABLE' ? 
					<div>
						<div>
							<h1>Entity View configuration</h1>
						</div>
						<div>
							<p>View, add, edit, and delete EntityViews</p>
						</div> 
						<div>
							<Button 
								id='add-new-view-btn' 
								icon='plus' 
								text='Add Entity view' 
								onClick={this._openModal} 
								style='primary'
								type='button'
							/>
						</div>
						<div>
							<hr></hr>
						</div>
					</div>
				: null}
				
				<EntityViewContent 
					tableContent={this.state.tableContent}
					entityEditFunction={this._editEntityView}
					entityDeleteFunction={this._deleteEntityView}
					tableMode={this.state.tableMode}
					setTableMode={this._setTableMode}
					rowToEdit={this.state.rowToEdit}
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
		// Expand second level mref as well
		var options = {
			attrs: {
				'~id': false,
				'identifier': false,
				'name': false,
				'masterEntity': false,
				'joinedEntities': {
					'*': false,
					'joinedAttributes': '*'
				}
			}
		};
		api.get('View', options).done(function(data) {
			self.setState({tableContent: data, refresh:false});
		});
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
		    	 self.setState({
		    		 isOpen:false, 
		    		 refresh:true, 
		    		 viewName:null, 
		    		 masterEntityName:null
	    		 });
		     }
		})
	},
	_editEntityView: function(row) {
		this.setState({rowToEdit: row, tableMode: 'ATTRIBUTE_MAPPING_TABLE'});
	},
	_deleteEntityView: function(row) {
		var self = this;
		$.ajax({
		     type: 'POST',
		     url: molgenis.getContextUrl() + '/delete-entity-view',
		     data: { 
		    	 viewName: row.name 
	    	 },
		     success: function() {
		    	 self.setState({refresh:true});
		     }
		})
	},
	_setTableMode: function(tableMode) {
		this.setState({tableMode: tableMode});
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
	_activateView: function(row) {
		// TODO call the /activate-entity-view url to reindex the entities to make the entity view visible in the dataExplorer
	}
});

export { EntityViewContainer };
export default React.createFactory(EntityViewContainer);