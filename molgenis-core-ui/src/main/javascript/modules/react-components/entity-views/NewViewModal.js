import React from 'react';
import {Modal, ModalClose} from 'react-modal-bootstrap';
import { NewViewForm } from './NewViewForm';

var NewViewModal = React.createClass({
	displayName: 'NewViewModal',
	propTypes: {
		isOpen: React.PropTypes.bool,
		hideModal: React.PropTypes.func,
		saveNewView: React.PropTypes.func,
		selectedViewName: React.PropTypes.string,
		selectedMasterEntity: React.PropTypes.object,
		selectedSlaveEntity: React.PropTypes.object,
		selectedMasterAttribute: React.PropTypes.string,
		selectedSlaveAttribute: React.PropTypes.string,
		selectedMasterAttributes: React.PropTypes.array,
		selectedSlaveAttributes: React.PropTypes.array,
		viewNameSelect: React.PropTypes.func,
		masterEntitySelect: React.PropTypes.func,
		slaveEntitySelect: React.PropTypes.func,
		masterAttributeSelect: React.PropTypes.func,
		slaveAttributeSelect: React.PropTypes.func
	},
	render: function() {
		return <Modal isOpen={this.props.isOpen} onRequestHide={this.props.hideModal}>
			<div className='modal-header'>
				<ModalClose onClick={this.props.hideModal} />
				<h4 className='modal-title'>Creating a new View</h4>
			</div>
			<div className='modal-body'>
				<NewViewForm 
					selectedViewName={this.props.selectedViewName}
					selectedMasterEntity={this.props.selectedMasterEntity}
					selectedSlaveEntity={this.props.selectedSlaveEntity}
					selectedMasterAttribute={this.props.selectedMasterAttribute}
					selectedSlaveAttribute={this.props.selectedSlaveAttribute}
					selectedMasterAttributes={this.props.selectedMasterAttributes}
					selectedSlaveAttributes={this.props.selectedSlaveAttributes}
					viewNameSelect={this.props.viewNameSelect}
					masterEntitySelect={this.props.masterEntitySelect}
					slaveEntitySelect={this.props.slaveEntitySelect}
					masterAttributeSelect={this.props.masterAttributeSelect}
					slaveAttributeSelect={this.props.slaveAttributeSelect} 
				/>
			</div>
		  	<div className='modal-footer'>
		  		<button className='btn btn-default' onClick={this.props.hideModal}>Cancel</button>
		  		<button className='btn btn-primary' onClick={this.props.saveNewView}>Save</button>
		  	</div>
	  	</Modal>	
	}
});

export { NewViewModal };
export default React.createFactory(NewViewModal);