import React from 'react';
import { Input } from '../Input';
import { EntitySelectBox } from '../EntitySelectBox';

var EntityViewForm = React.createClass({
	displayName: 'EntityViewForm',
	propTypes: {
		viewName: React.PropTypes.string,
		inputOnValueChange: React.PropTypes.func,
		entitySelectOnValueChange: React.PropTypes.func
	},
	render: function() {
		
		return <form>
			<div className='form-group'>
				<label htmlFor='view-name-input-field'>Specify a name for the new view</label>
				<Input 
		  			type='text' 
		  			id='view-name-input-field' 
		  			onValueChange={this.props.inputOnValueChange} 
		  			value={this.props.viewName} 
				/>
			</div>
			
			<div className='form-group'>
				<label htmlFor='entity-select-box'>Select a master entity</label>
				<EntitySelectBox
					entity='entities'
					onValueChange={this.props.entitySelectOnValueChange}
				/>
			</div>
		</form>
	}
});

export { EntityViewForm };
export default React.createFactory(EntityViewForm);