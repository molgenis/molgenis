/**
 * Controls for entities and attributes
 * 
 * Dependencies: TODO
 * 
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	var api = new molgenis.RestClient();
	
	var div = React.DOM.div;
	var __spread = React.__spread;
	
	/**
	 * REST entity control
	 * 
	 * @memberOf control
	 */
	var EntitySelectBox = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'EntitySelectBox',
		propTypes: {
			entity: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			name: React.PropTypes.string,
			disabled: React.PropTypes.bool,
			readOnly: React.PropTypes.bool,
			multiple: React.PropTypes.bool,
			required: React.PropTypes.bool,
			placeholder: React.PropTypes.string,
			value: React.PropTypes.oneOfType([React.PropTypes.object, React.PropTypes.array]),
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {
				entity: null
			};
		},
		componentWillReceiveProps: function(nextProps) {
			if(!_.isEqual(this.props.entity, nextProps.entity)) {
				this._initEntity(nextProps.entity);
			}
		},
		componentDidMount: function() {//console.log('componentDidMount AttributeFormControl');
			this._initEntity(this.props.entity);
		},
		render: function() {//console.log('render EntitySelectBox', this.state, this.props);
			if(this.state.entity === null) {
				// entity meta data not fetched yet
				return div({});
			}
			
			var props = this.props;
			var entity = this.state.entity;
			
			var formatResult = function(item) {
				var attrs = this._getAttrs();
				if (attrs.length > 1) {
					var items = [];
					items.push('<div class="row">');

					var width = Math.round(12 / attrs.length); // FIXME fix in case of 5, 7 etc. lookup attributes
					$.each(attrs, function(i, attrName) {
						var attrLabel = entity.attributes[attrName].label;
						var attrValue = item[attrName] !== undefined ? item[attrName] : '';
						items.push('<div class="col-md-' + width + '">');
						items.push(htmlEscape(attrLabel) + ': <b>' + htmlEscape(attrValue) + '</b>');
						items.push('</div>');
					});

					items.push('</div>');

					return items.join('');
				} else {
					return item[attrs[0]];
				}
			}.bind(this);
			
			var formatSelection = function(item) {
				return item[entity.labelAttribute];
			};
			
			var options = {
				enable: !this.props.disabled,
				id: entity.idAttribute,
				multiple: props.multiple,
				allowClear : props.required ? false : true,
				placeholder : props.placeholder || ' ', // cannot be an empty string
				closeOnSelect: props.multiple !== true,
				initSelection: function(element, callback) {
					if(this.props.value) {
						callback([this.props.value]);
					}
				}.bind(this),
			    query: function (query) {
			    	var num = 25;
				    var q = {
						q : {
							start : (query.page - 1) * num, 
							num : num,
							orders : [ {
								direction : 'ASC',
								property : this._getAttrs()[0]
							} ],
							q: query.term.length > 0 ? this._createQuery(query.term) : undefined
						}
					};
			    	
			    	api.getAsync(entity.hrefCollection, q).done(function(data) {
			    		query.callback({results: data.items, more: data.nextHref ? true : false});
			    	});
			    }.bind(this),
			    formatResult: formatResult,
			    formatSelection: formatSelection
			};
			return molgenis.control.wrapper.Select2({
				options : options,
				name : this.props.name,
				disabled : this.props.disabled,
				readOnly : this.props.readOnly,
				value : this.props.value,
				onChange : this._handleChange
			});
		},
		_handleChange: function(value) {//console.log('_handleChange EntitySelectBox', value);
			var val = this.props.multiple && value.length === 0 ? undefined : value;
			this.props.onValueChange({value: val});
		},
		_isLoaded: function(entity) {
			return entity.idAttribute !== undefined;
		},
		_initEntity: function(entity) {
			// fetch entity meta if not exists
			if(typeof entity === 'object') {
				if(!this._isLoaded(entity)) {
					this._loadEntity(entity.href);
				} else {
					this.setState({entity: entity});
				}
			} else if (typeof entity === 'string') {
				this._loadEntity(entity);
			}
		},
		_loadEntity: function(href) {
			api.getAsync(href, {'expand': ['attributes']}).done(function(entity) {
				if (this.isMounted()) {
					this.setState({entity: entity});
				}
			}.bind(this));
		},
		_createQuery: function(term) {
			var rules = [];
			var attrs = this._getAttrs();
			for(var i = 0; i < attrs.length; ++i) {
				if(i > 0) {
					rules.push({operator: 'OR'});	
				}
				rules.push({field: attrs[i], operator: 'LIKE', value: term});
			}
			return rules;
		},
		_getAttrs: function() {
			// display lookup attributes in dropdown or label attribute if no lookup attributes are defined
			var entity = this.state.entity; 
			if(entity.lookupAttributes && entity.lookupAttributes.length > 0) {
				return entity.lookupAttributes;
			} else {
				return [entity.labelAttribute];
			}
		}
	});
	
	/**
	 * REST entity attribute control
	 * 
	 * @memberOf control
	 */
	var AttributeControl = React.createClass({
		mixins: [molgenis.DeepPureRenderMixin],
		displayName: 'AttributeControl',
		propTypes: {
			attr: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.object]),
			onValueChange: React.PropTypes.func.isRequired
		},
		getInitialState: function() {
			return {
				attr: null
			};
		},
		componentWillReceiveProps: function(nextProps) {
			if(!_.isEqual(this.props.attr, nextProps.attr)) {
				this._initAttr(nextProps.attr);
			}
		},
		componentDidMount: function() {//console.log('componentDidMount AttributeFormControl');
			this._initAttr(this.props.attr);
		},
		render: function() {//console.log('render AttributeControl', this.state, this.props);	
			if(this.state.attr === null) {
				// attribute not available yet
				return div({});
			}
			
			var props = this.props;
			var attr = this.state.attr;
			
			if(attr.expression === undefined) {
				switch(attr.fieldType) {
					case 'BOOL':
						var layout = props.layout || 'checkbox';
						return molgenis.control.BoolControl({
							id: props.id,
							name: attr.name,
							label : props.label,
							required : !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : layout,
							value : props.value,
							onValueChange : this._handleValueChange
						});
					case 'CATEGORICAL':
						if(this.state.options === undefined) {
							// options not yet available
							return div();
						}
						
//						xcategoricalreadonly_value: {
//							href: "/api/v1/org_molgenis_test_TypeTestRef/ref1"
//							value: "ref1"
//							label: "label1"
//						}
						
						var value = props.value !== undefined ? {
							value: props.value['value'], // FIXME get idAttr
							label: props.value['label'] // FIXME get labelAttr
						} : undefined;
						
						var layout = props.layout || 'vertical';
						var Control = props.multiple === true ? molgenis.control.CheckboxGroupControl : molgenis.control.RadioGroupControl;
						return Control({
							id: props.id,
							name: attr.name,
							options : this.state.options,
							required : !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : layout,
							value : value !== undefined ? value['value'] : undefined, // FIXME get idAttr
							onValueChange : this._handleValueChange // FIXME go from id to entity
						});
					case 'CATEGORICAL_MREF':
						if(this.state.options === undefined) {
							// options not yet available
							return div();
						}
	
						return molgenis.control.CheckboxGroupControl({
							id: props.id,
							name: attr.name,
							options : this.state.options,
							required : !attr.nillable,
							disabled : props.disabled,
							readOnly : attr.readOnly,
							layout : 'vertical', // FIXME make configurable
							value : props.value,
							onValueChange : this._handleValueChange
						});
					case 'DATE':
						var placeholder = props.placeholder || 'Date';
						return this._createDateControl(false, placeholder);
					case 'DATE_TIME':
						var placeholder = props.placeholder || 'Date';
						return this._createDateControl(true, placeholder);
					case 'DECIMAL':
						return this._createNumberControl('any');
					case 'EMAIL':
						var placeholder = props.placeholder || 'Email';
						return this._createStringControl('email', placeholder);
					case 'ENUM':
						if(this.state.options === undefined) {
							// options not yet available
							return div();
						}
						
						var Control = props.multiple === true ? molgenis.control.CheckboxGroupControl : molgenis.control.RadioGroupControl;
						return Control({
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
						return molgenis.control.CodeEditorControl({
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
						var placeholder = props.placeholder || 'URL';
						return this._createStringControl('url', placeholder);
					case 'INT':
					case 'LONG':
						return this._createNumberControl('1');
					case 'XREF':
						var placeholder = props.placeholder || 'Search for a Value';
						var multiple = props.multiple || false;
						return this._createEntitySelectBox(multiple, placeholder);
					case 'MREF':
						var placeholder = props.placeholder || 'Search for Values';
						var multiple = props.multiple || true;
						return this._createEntitySelectBox(multiple, placeholder);
					case 'SCRIPT':
						return molgenis.control.CodeEditorControl({
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
						var placeholder = props.placeholder || '';
						return this._createStringControl('text', placeholder);
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
				return molgenis.control.InputControl({
					type: 'text',
					disabled: true,
					placeholder: 'This value is computed automatically',
					onValueChange : function() {}
				});
			}
		},
		_handleValueChange: function(event) {//console.log('_handleChange AttributeControl', event);
			this.props.onValueChange(__spread({}, event, {attr: this.state.attr.name}));
		},
		_createNumberControl: function(step) {
			var min = this.props.range ? this.props.range.min : undefined;
			var max = this.props.range ? this.props.range.max : undefined;
			var placeholder = this.props.placeholder || 'Number';
			return molgenis.control.InputControl({
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
			return molgenis.control.InputControl({
				type : type,
				id : this.props.id,
				name : this.state.attr.name,
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				disabled : this.props.disabled,
				readOnly : this.state.attr.readOnly,
				maxlength : '255',
				value : this.props.value,
				onValueChange : this._handleValueChange,
				onBlur : this.props.onBlur
			});
		},
		_createDateControl: function(time, placeholder) {
			return molgenis.control.DateControl({
				id : this.props.id,
				name : this.state.attr.name,
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				disabled : this.props.disabled,
				readOnly : this.state.attr.readOnly,
				time : time,
				value : this.props.value,
				onValueChange : this._handleValueChange
			});
		},
		_createTextAreaControl: function() {
			return molgenis.control.TextAreaControl({
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
		_createEntitySelectBox: function(multiple, placeholder) {
			var props = this.props;
			return molgenis.control.EntitySelectBox({
				id : props.id,
				name : this.state.attr.name,
				placeholder : placeholder,
				required : !this.state.attr.nillable,
				multiple : multiple,
				disabled : props.disabled,
				readOnly : this.state.attr.readOnly,
				entity : this.state.attr.refEntity,
				value : props.value,
				onValueChange : this._handleValueChange
			});
		},
		_isLoaded: function(attr) {
			return attr.name !== undefined;
		},
		_initAttr: function(attr) {
			// fetch attribute meta if not exists
			if(typeof attr === 'object') {
				if(!this._isLoaded(attr)) {
					this._loadAttr(attr.href, this._onAttrInit);
				} else {
					this.setState({attr: attr});
					this._onAttrInit(attr);
				}
			} else if (typeof attr === 'string') {
				this._loadAttr(attr, this._onAttrInit);
			}
		},
		_loadAttr: function(href, callback) {
			api.getAsync(href).done(function(attr) {
				if (this.isMounted()) {
					this.setState({attr: attr});
					callback(attr);
				}
			}.bind(this));
		},
		_onAttrInit: function(attr) {
			if(attr.fieldType === 'CATEGORICAL' || attr.fieldType === 'CATEGORICAL_MREF') {
				// retrieve all categories
				api.getAsync(attr.refEntity.href).done(function(meta) {
					var idAttr = meta.idAttribute;
					var lblAttr = meta.labelAttribute;
					
					if (this.isMounted()) {
						api.getAsync(attr.refEntity.hrefCollection, {'attributes' : [idAttr, lblAttr]}).done(function(data) { // FIXME problems in case of large number of categories
							if (this.isMounted()) {
								var options = $.map(data.items, function(entity) {
									return {value: entity[idAttr], label: entity[lblAttr]};
								});
								this.setState({options: options});
							}
						}.bind(this));	
					}
				}.bind(this));
			}
			else if(attr.fieldType === 'ENUM') {
				var options = $.map(attr.enumOptions, function(option) {
					return {value : option, label : option};
				});
				this.setState({options: options});
			}
		}
	});
	
	// export module
	molgenis.control = molgenis.control || {};
	
	$.extend(molgenis.control, {
		EntitySelectBox: React.createFactory(EntitySelectBox),
		AttributeControl: React.createFactory(AttributeControl)
	});
}($, window.top.molgenis = window.top.molgenis || {}));