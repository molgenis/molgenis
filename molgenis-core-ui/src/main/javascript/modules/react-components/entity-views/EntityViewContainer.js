import React from 'react';
import RestClientV2 from '../../rest-client/RestClientV2';
import $ from 'jquery';

import { Button } from '../Button'; 
import { EntityViewsTable } from './EntityViewsTable';
import { NewViewModal } from './NewViewModal';
import { Spinner } from '../Spinner';

var api = new RestClientV2();

var EntityViewContainer = React.createClass({
	displayName: 'EntityViewContainer',
	propTypes: {},
	getInitialState: function() {
		return {
			views: null,
			selectedViewName: null,
			selectedMasterEntity: null,
			selectedSlaveEntity: null,
			selectedMasterAttribute: null,
			selectedSlaveAttribute: null,
			selectedMasterAttributes: null,
			selectedSlaveAttributes: null,
			refresh: true,
			isOpen: false,
		}
	},
	render: function() {
		// If a delete or add has been done, retrieve entity views again
		this.state.refresh ? this._retrieveEntityViews() : null;
		return <div className='row'>
			<div className='col-md-6'>
				<h1>Entity View configuration</h1>
				<p>View, add, edit, and delete EntityViews</p>
				<Button id='add-new-view-btn' icon='plus' text='Add Entity view' onClick={this._openModal} style='primary' type='button' />
				{this.state.views === null ? <Spinner /> 
					: <EntityViewsTable views={this.state.views} deleteEntityView={this._deleteEntityView} />
				}

				{this.state.isOpen && <NewViewModal
					isOpen={this.state.isOpen}
					hideModal={this._hideModal}
					saveNewView={this._saveNewView}
					selectedViewName={this.state.selectedViewName}
					selectedMasterEntity={this.state.selectedMasterEntity}
					selectedSlaveEntity={this.state.selectedSlaveEntity}
					selectedMasterAttribute={this.state.selectedMasterAttribute}
					selectedSlaveAttribute={this.state.selectedSlaveAttribute}
					selectedMasterAttributes={this.state.selectedMasterAttributes}
					selectedSlaveAttributes={this.state.selectedSlaveAttributes}
					viewNameSelect={this._viewNameSelect}
					masterEntitySelect={this._masterEntitySelect}
					slaveEntitySelect={this._slaveEntitySelect}
					masterAttributeSelect={this._masterAttributeSelect}
					slaveAttributeSelect={this._slaveAttributeSelect} 
				/>}
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
				'slaveEntities': {
					'*': false,
					'joinedAttributes': '*'
				}
			}
		};
		api.get('View', options).done(function(data) {
			self.setState({views: data.items, refresh:false});
		});
	},
	_saveNewView: function() {
		var self = this;
		$.ajax({
			url: molgenis.getContextUrl() + '/save-new-view',
			method: 'POST',
			data: {
				'viewName': this.state.selectedViewName,
				'masterEntity' : this.state.selectedMasterEntity.value.fullName,
				'slaveEntity' : this.state.selectedSlaveEntity.value.fullName,
				'masterAttribute' : this.state.selectedMasterAttribute,
				'slaveAttribute' : this.state.selectedSlaveAttribute
			}
		}).always(function() {
			self.setState({
				selectedViewName: null,
				selectedMasterEntity: null,
				selectedSlaveEntity: null,
				selectedMasterAttribute: null,
				selectedSlaveAttribute: null,
				isOpen: false,
				refresh: true
			});
		});
	},
	_deleteEntityView: function(row) {
		var viewDelete = confirm("Are you sure you want to delete this View?");
		if(viewDelete) {
			var self = this;
			$.ajax({
			     type: 'POST',
			     url: molgenis.getContextUrl() + '/delete-view',
			     data: { 
			    	 viewName: row.name 
		    	 },
			     success: function() {
			    	 self.setState({refresh:true});
			     }
			})
		}
	},
	_openModal: function() {
		this.setState({isOpen: true});
	},	 
	_hideModal: function() {
		this.setState({
			selectedViewName: null,
			selectedMasterEntity: null, 
			selectedSlaveEntity: null,
			selectedMasterAttribute: null,
			selectedSlaveAttribute: null,
			isOpen: false
		});
	},
	_viewNameSelect: function(input) {
		this.setState({selectedViewName: input.value});
	},
	_masterEntitySelect: function(selectedEntity) {
		if(selectedEntity.value != null) {
			var selectedMasterEntity = selectedEntity;
			var selectedMasterAttributes = selectedEntity.value.attributes.items;
			this.setState({selectedMasterEntity, selectedMasterAttributes});
		} else {
			this.setState({selectedMasterEntity: null, selectedMasterAttributes: null});
		}
	},
	_slaveEntitySelect: function(selectedEntity) {
		if(selectedEntity.value != null) {
			var selectedSlaveEntity = selectedEntity;
			var selectedSlaveAttributes = selectedEntity.value.attributes.items;
			this.setState({selectedSlaveEntity, selectedSlaveAttributes});
		} else {
			this.setState({selectedSlaveEntity : null, selectedSlaveAttributes: null});
		}
	},
	_masterAttributeSelect: function(selectedAttribute) {
		this.setState({selectedMasterAttribute: selectedAttribute.target.value})
	},
	_slaveAttributeSelect: function(selectedAttribute) {
		this.setState({selectedSlaveAttribute: selectedAttribute.target.value})
	}
});

export { EntityViewContainer };
export default React.createFactory(EntityViewContainer);