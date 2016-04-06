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
		return <Modal isOpen={this.props.isOpen} onRequestHide={this.props.hideModal}>
			<div className='modal-header'>
				<ModalClose onClick={this.props.hideModal}/>
				<h4 className='modal-title'>Modal title</h4>
			</div>
			<div className='modal-body'>
				<EntityViewForm viewName={this.props.viewName} inputOnValueChange={this.props.inputOnValueChange} entitySelectOnValueChange={this.props.entitySelectOnValueChange} />
			</div>
		  	<div className='modal-footer'>
		  		<button className='btn btn-default' onClick={this.props.hideModal}>Close</button>
		  		<button className='btn btn-primary' onClick={this.props.saveEntityView}>Save changes</button>
		  	</div>
	</Modal>	
	}
});

export { EntityViewModal };
export default React.createFactory(EntityViewModal);