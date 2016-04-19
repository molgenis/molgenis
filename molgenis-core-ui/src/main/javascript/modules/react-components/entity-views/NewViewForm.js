import React from 'react';
import { Input } from '../Input';
import { EntitySelectBox } from '../EntitySelectBox';

var NewViewForm = React.createClass({
	displayName: 'NewViewForm',
	propTypes: {
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
	getDefaultProps: function() {
		return {
			selectedViewName: null,
			selectedMasterEntity: null,
			selectedSlaveEntity: null,
			selectedMasterAttributes: null,
			selectedSlaveAttributes: null
		}
	},
	render: function() {
		return <form>
			<div className='form-group'>
				<label htmlFor='view-name-input-field'>Specify a name for the new view</label>
				<Input type='text' id='view-name-input-field' onValueChange={this.props.viewNameSelect} focus={true} value={this.props.selectedViewName} />
			</div>
				
			<div className='form-group'>
				<label>Select a master entity</label>
				<EntitySelectBox entity='entities' onValueChange={this.props.masterEntitySelect} />
			</div>
			
			<div className='form-group'>
				<label>Select a slave entity</label>
				<EntitySelectBox entity='entities' onValueChange={this.props.slaveEntitySelect} />
			</div>
			
			{this.props.selectedMasterEntity != null  && this.props.selectedSlaveEntity != null ?
			<div>
				<div className='form-group'>
					<label>Select a master attribute</label>
					<select onChange={this.props.masterAttributeSelect} value={this.props.selectedMasterAttribute} className="form-control">
						<option></option>
						{this.props.selectedMasterAttributes.map(function(attribute, index) {
							return <option key={index} value={attribute.identifier}>{attribute.label}</option>
						})}
					</select> 
				</div>
		
				<div className='form-group'>
					<label>Select a slave attribute</label>
					<select onChange={this.props.slaveAttributeSelect} value={this.props.selectedSlaveAttribute} className="form-control">
						<option></option>
						{this.props.selectedSlaveAttributes.map(function(attribute, index) {
							return <option key={index} value={attribute.identifier}>{attribute.label}</option>
						})}
					</select>
				</div>
			</div>
			: null}
		</form>
	}
});

export { NewViewForm };
export default React.createFactory(NewViewForm);