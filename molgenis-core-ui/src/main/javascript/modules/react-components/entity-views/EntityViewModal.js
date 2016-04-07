import React from 'react';
import {Modal, ModalClose} from 'react-modal-bootstrap';
import { EntityViewForm } from './EntityViewForm';

var EntityViewModal = React.createClass({
	displayName: 'CreateEntityViewModal',
	propTypes: {
		isOpen: React.PropTypes.bool,
		hideModal: React.PropTypes.func,
		saveEntityView: React.PropTypes.func,
		viewName: React.PropTypes.string,
		inputOnValueChange: React.PropTypes.func,
		entitySelectOnValueChange: React.PropTypes.func
	},
	render: function() {
		var { isOpen, hideModal, viewName, inputOnValueChange, entitySelectOnValueChange, saveEntityView} = this.props;
		return <Modal isOpen={isOpen} onRequestHide={hideModal}>
			<div className='modal-header'>
				<ModalClose onClick={hideModal} />
				<h4 className='modal-title'>Modal title</h4>
			</div>
			<div className='modal-body'>
				<EntityViewForm 
					viewName={viewName} 
					inputOnValueChange={inputOnValueChange} 
					entitySelectOnValueChange={entitySelectOnValueChange} 
				/>
			</div>
		  	<div className='modal-footer'>
		  		<button className='btn btn-default' onClick={hideModal}>Cancel</button>
		  		<button className='btn btn-primary' onClick={saveEntityView}>Save</button>
		  	</div>
	</Modal>	
	}
});

export { EntityViewModal };
export default React.createFactory(EntityViewModal);