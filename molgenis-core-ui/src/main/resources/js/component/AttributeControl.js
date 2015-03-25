/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div;
	
	var api = new molgenis.RestClient();
	
	/**
	 * REST entity attribute control
	 * 
	 * @memberOf component
	 */
	var AttributeControl = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.AttributeLoaderMixin],
		displayName: 'AttributeControl',
		propTypes: {
			attr: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			focus: React.PropTypes.bool,
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {
				attr: null
			};
		},
		render: function() {	// FIXME apply focus to all controls
			if(this.state.attr === null) {
				// attribute not available yet
				return molgenis.ui.Spinner();
			}
			
			var props = this.props;
			var attr = this.state.attr;
			
			if(attr.expression === undefined) {
				switch(attr.fieldType) {
					case 'BOOL':
						return molgenis.ui.BoolControl({
							id: props.id,
							name: attr.name,
							label : props.label,
							required : !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : props.layout || 'horizontal',
							value : props.value,
							onValueChange : this._handleValueChange
						});
					case 'CATEGORICAL':
						if(this.state.options === undefined) {
							// options not yet available
							return molgenis.ui.Spinner();
						}
						
						var CategoricalControl = props.multiple === true ? molgenis.ui.CheckboxGroup : molgenis.ui.RadioGroup;
						return CategoricalControl({
							id: props.id,
							name: attr.name,
							options : this.state.options,
							required : !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : props.layout || 'vertical',
							value : props.value !== undefined ? props.value[attr.refEntity.idAttribute] : undefined,
							onValueChange : this._handleValueChange // FIXME go from id to entity
						});
					case 'CATEGORICAL_MREF':
						if(this.state.options === undefined) {
							// options not yet available
							return molgenis.ui.Spinner();
						}
	
						var values = props.value ? _.map(props.value.items, function(item) {
							return item[attr.refEntity.idAttribute];
						}) : [];
						return molgenis.ui.CheckboxGroup({
							id: props.id,
							name: attr.name,
							options : this.state.options,
							required : !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : 'vertical', // FIXME make configurable
							value : values,
							onValueChange : this._handleValueChange
						});
					case 'DATE':
						return this._createDateControl(false, props.placeholder || 'Date');
					case 'DATE_TIME':
						return this._createDateControl(true, props.placeholder || 'Date');
					case 'DECIMAL':
						return this._createNumberControl('any');
					case 'EMAIL':
						return this._createStringControl('email', props.placeholder || 'Email');
					case 'ENUM':
						if(this.state.options === undefined) {
							// options not yet available
							return molgenis.ui.Spinner();
						}
						
						var EnumControl = props.multiple === true ? molgenis.ui.CheckboxGroup : molgenis.ui.RadioGroup;
						return EnumControl({
							id: props.id,
							name: attr.name,
							options : this.state.options,
							required: !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : props.layout,
							value : props.value,
							onValueChange : this._handleValueChange
						});
					case 'HTML':
						return molgenis.ui.CodeEditor({
							id : this.props.id,
							name: attr.name,
							placeholder : this.props.placeholder,
							required : !this.state.attr.nillable,
							disabled : this.props.disabled,
							readOnly : this.state.attr.readOnly,
							language: 'html',
							value : this.props.value,
							onValueChange : this._handleValueChange
						});
					case 'HYPERLINK':
						return this._createStringControl('url', props.placeholder || 'URL');
					case 'INT':
					case 'LONG':
						return this._createNumberControl('1');
					case 'XREF':
						return this._createEntitySelectBox(props.multiple || false, props.placeholder || 'Search for a Value', props.value);
					case 'MREF':
						return this._createEntitySelectBox(props.multiple || true, props.placeholder || 'Search for Values', props.value ? props.value.items : undefined);
					case 'SCRIPT':
						return molgenis.ui.CodeEditor({
							id : this.props.id,
							name : attr.name,
							placeholder : this.props.placeholder,
							required : !this.state.attr.nillable,
							disabled : this.props.disabled,
							readOnly : this.state.attr.readOnly,
							value : this.props.value,
							onValueChange : this._handleValueChange
						});
					case 'STRING':
						return this._createStringControl('text', props.placeholder || '');
					case 'TEXT':
						return this._createTextAreaControl();
					case 'COMPOUND' :
					case 'FILE':
					case 'IMAGE':
						throw 'Unsupported data type: ' + attr.fieldType;
					default:
						throw 'Unknown data type: ' + attr.fieldType;
				}
			} else {
				return molgenis.ui.Input({
					type: 'text',
					disabled: true,
					placeholder: 'This value is computed automatically',
					onValueChange : function() {}
				});
			}
		},
		_handleValueChange: function(event) {
			this.props.onValueChange(_.extend({}, event, {attr: this.state.attr.name}));
		},
		_createNumberControl: function(step) {
			var min = this.props.range ? this.props.range.min : undefined;
			var max = this.props.range ? this.props.range.max : undefined;
			var placeholder = this.props.placeholder || 'Number';
			return molgenis.ui.Input({
				type : 'number',
				id : this.props.id,
				name : this.state.attr.name,
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				disabled : this.props.disabled,
				readOnly : this.state.attr.readOnly,
				step : step,
				min : min,
				max : max,
				value : this.props.value,
				onValueChange : this._handleValueChange,
				onBlur : this.props.onBlur
			});
		},
		_createStringControl: function(type, placeholder) {
			return molgenis.ui.Input({
				type : type,
				id : this.props.id,
				name : this.state.attr.name,
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				disabled : this.props.disabled,
				readOnly : this.state.attr.readOnly,
				maxlength : '255',
				focus: this.props.focus,
				value : this.props.value,
				onValueChange : this._handleValueChange,
				onBlur : this.props.onBlur
			});
		},
		_createDateControl: function(time, placeholder) {
			return molgenis.ui.DateControl({
				id : this.props.id,
				name : this.state.attr.name,
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				disabled : this.props.disabled,
				readOnly : this.state.attr.readOnly,
				time : time,
				focus: this.props.focus,
				value : this.props.value,
				onValueChange : this._handleValueChange
			});
		},
		_createTextAreaControl: function() {
			return molgenis.ui.TextArea({
				id : this.props.id,
				name : this.state.attr.name,
				placeholder : this.props.placeholder,
				required : !this.state.attr.nillable,
				disabled : this.props.disabled,
				readOnly : this.state.attr.readOnly,
				value : this.props.value,
				onValueChange : this._handleValueChange
			});
		},
		_createEntitySelectBox: function(multiple, placeholder, value) {
			var props = this.props;
			return molgenis.ui.EntitySelectBox({
				id : props.id,
				name : this.state.attr.name,
				mode: 'create',
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				multiple : multiple,
				disabled : props.disabled,
				readOnly : this.state.attr.readOnly,
				focus: this.props.focus,
				entity : this.state.attr.refEntity,
				value : value,
				onValueChange : this._handleValueChange
			});
		},
		_onAttrInit: function() {
			var attr = this.state.attr;
			if(attr.fieldType === 'CATEGORICAL' || attr.fieldType === 'CATEGORICAL_MREF') {
				// retrieve all categories
				api.getAsync(attr.refEntity.href).done(function(meta) {
					var idAttr = meta.idAttribute;
					var lblAttr = meta.labelAttribute;
					
					if (this.isMounted()) {
						api.getAsync(attr.refEntity.hrefCollection, {'attributes' : [idAttr, lblAttr]}).done(function(data) { // FIXME problems in case of large number of categories
							if (this.isMounted()) {
								var options = _.map(data.items, function(entity) {
									return {value: entity[idAttr], label: entity[lblAttr]};
								});
								this.setState({options: options});
							}
						}.bind(this));	
					}
				}.bind(this));
			}
			else if(attr.fieldType === 'ENUM') {
				var options = _.map(attr.enumOptions, function(option) {
					return {value : option, label : option};
				});
				this.setState({options: options});
			}
		}
	});
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		AttributeControl: React.createFactory(AttributeControl)
	});
}(_, React, molgenis));