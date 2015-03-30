/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var input = React.DOM.input, div = React.DOM.div;
	
	/**
	 * @memberOf component
	 */
	var Input = React.createClass({
		displayName: 'Input',
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		propTypes: {
			type: React.PropTypes.string.isRequired,
			id: React.PropTypes.string,
			name: React.PropTypes.string,
			placeholder: React.PropTypes.string,
			required: React.PropTypes.bool,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			step: React.PropTypes.string,
			min: React.PropTypes.string,
			max: React.PropTypes.string,
			maxLength: React.PropTypes.number,
			focus: React.PropTypes.bool,
			value: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]),
			checked: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired,
			onBlur: React.PropTypes.func
		},
		getInitialState: function() {
			return this._isRadioOrCheckbox() ? {checked: this.props.checked} : {value: this.props.value};
		},
		componentDidMount: function() {
			this._focus();
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState(this._isRadioOrCheckbox() ? {checked: nextProps.checked} : {value: nextProps.value});
		},
		render: function() {
			var props = this.props;
			
			var inputProps = {
				type: props.type,
				className: this._isRadioOrCheckbox() ? undefined : 'form-control',
				id: props.id,
				name: props.name,
				placeholder: props.placeholder,
				required: props.required,
				disabled: props.disabled,
				readOnly: props.readOnly,
				step: props.step,
				min: props.min,
				max: props.max,
				maxLength: props.maxLength,
				value: this._isRadioOrCheckbox() ? this.props.value : this.state.value,
				checked: this._isRadioOrCheckbox() ? this.state.checked : undefined,
				onChange: this._handleChange,
				onBlur: this._handleBlur,
				ref: this.props.focus ? 'input' : undefined
			};
			
			if(props.readOnly && this._isRadioOrCheckbox()) {
				// readonly attribute doesn't work on checkboxes and radio buttons:
				// http://stackoverflow.com/questions/155291/can-html-checkboxes-be-set-to-readonly
				_.extend(inputProps, {name: undefined, disabled: true});
				
				 if(this.state.checked) {
					 // for checked checkboxes/radio buttons submit the value of a hidden input,
					 // disable visible input to display a readonly view to the user 
					 return div({},
						input({
							type: 'hidden',
							name: props.name,
							value: props.value
						}),
						input(inputProps)
					);	 
				 } else {
					// values of unchecked checkboxes/radio buttons are never submitted:
					// http://www.w3.org/TR/html401/interact/forms.html#h-17.13.2
				 	return input(inputProps);
				 }
			} else {
				return input(inputProps);
			}
		},
		componentDidUpdate: function() {
			this._focus();
		},
		_handleChange: function(event) {
			this.setState(this._isRadioOrCheckbox() ? {checked: event.target.checked} : {value: event.target.value});
			this._handleChangeOrBlur(event.target.value, event.target.checked, this.props.onValueChange);
		},
		_handleBlur: function(event) {
			if(this.props.onBlur) {
				this._handleChangeOrBlur(event.target.value, event.target.checked, this.props.onBlur);
			}
		},
		_handleChangeOrBlur: function(value, checked, callback) {
			var valueEvent;
			if(this._isRadioOrCheckbox()) {
				valueEvent = {value: this._emptyValueToNull(value), checked: checked};
			} else {
				var val = this._emptyValueToNull(value);
				
				if(this.props.type === 'number' && val !== null) {
					val = parseFloat(val); // convert js string to js number
				}
				valueEvent = {value: val};
			}
			callback(valueEvent);
		},
		_isRadioOrCheckbox: function() {
			return this.props.type === 'radio' || this.props.type === 'checkbox';
		},
		_emptyValueToNull: function(value) {
			return value !== '' ? value : null;
		},
		_focus: function() {
			if(this.props.focus) {
				this.refs.input.getDOMNode().focus();
			}
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Input: React.createFactory(Input)
	});
}(_, React, molgenis));