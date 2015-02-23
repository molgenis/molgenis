/**
 * MOLGENIS attribute controls for all data types
 * 
 * Dependencies: TODO
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.controls = molgenis.controls || {};

	var api = new molgenis.RestClient();
	
	// parent class for: DECIMAL, INT, LONG
	var AbstractNumberControl = function(attr, props, $container) {
		if (this.constructor === AbstractNumberControl) {
			throw new Error("can't instantiate abstract class");
		}
		
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	AbstractNumberControl.prototype = {
		template: Handlebars.compile('<input type="number" class="form-control" step="{{step}}"{{#if value}} value="{{value}}"{{/if}}{{#if placeholder}} placeholder="{{placeholder}}"{{/if}}{{#if nillable}}{{else}} required{{/if}}>'),
				
		init: function() {
			var self = this;
			var props = this.props;
			var $container = this.$container;
			
			$container.off();
			$container.empty();
			
			$container.on('change', 'input', function(e) {
				var rawVal = $(this).val();
				self.value = rawVal !== '' ? parseFloat(rawVal) : null;
				
				if(props.onValueChange) {
					props.onValueChange(self.value);
				}
			});
			
			this.value = props.value ? parseFloat(props.value) : null;
			this.render();
		},
		
		render: function() {
			var self = this;
			var props = this.props;
			
			this.$container.html(this.template({
				nillable : this.attr.nillable,
				value : self.value,
				step : props.step,
				placeholder : props.placeholder
			}));
		},

		getValue: function() {
			return this.value;
		}
	};
	
	// parent class for: CATEGORICAL, MREF, XREF
	var AbstractRefControl = function(attr, props, $container) {
		if (this.constructor === AbstractRefControl) {
			throw new Error("can't instantiate abstract class");
		}
		
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	AbstractRefControl.prototype = {
		template: Handlebars.compile('<input type="hidden">'),

		init: function() {
			var self = this;
			
			var attr = this.attr;
			var props = this.props;
			var $container = this.$container;
			
			$container.off();
			$container.empty();
			
			$container.on('change', 'input', function(e) {
				var rawVal = $(this).val();
				self.value = rawVal !== '' ? (rawVal !== '%%null%%' ? rawVal : null) : undefined;
				if(props.onValueChange) {
					props.onValueChange(self.value);
				}
			});
			
			this.value = props.value;
			this.render();
		},
		
		render: function() {
			var self = this;
			var attr = self.attr;
			var props = self.props;
			var $container = self.$container;
			
			function _render() {
				var idAttr = attr.refEntity.idAttribute;
				var labelAttr = attr.refEntity.labelAttribute;
				
				var format = function(item) {
					if (item)
						return item[labelAttr];
				};
				
				var opts = {
					id: idAttr,
					multiple: props.multiple,
					allowClear : attr.nillable ? true : false,
					placeholder : props.placeholder || ' ', // cannot be an empty string
					initSelection: function(element, callback) {
						if(self.value)
							callback(self.value);
					},
				    query: function (query) {
				    	var num = 25;
					    var q = {
							q : {
								start : (query.page - 1) * num, 
								num : num
							}
						};
				    	
				    	api.getAsync(attr.refEntity.hrefCollection, q).done(function(data) {
				    		var items = data.items;
				    		
				    		// add entry to dropdown for 'null' item 
				    		if(props.includeNillable && query.page === 1) {
				    			var nillableItem = {};
				    			nillableItem[idAttr] = '%%null%%';
				    			nillableItem[labelAttr] = 'N/A';
				    			items.unshift(nillableItem);
				    		}
				    		query.callback({results: items, more: data.nextHref ? true : false});
				    	});
				    },
				    formatResult: format,
				    formatSelection: format,
				    minimumResultsForSearch: -1, // permanently hide the search field
				    width: '100%'
				};
				
				$container.html(self.template({}));
				$('input', $container).select2(opts).select2('val', []); // create select2 and trigger initSelection	
			}
			
			// retrieve meta data for referred entity if meta data is incomplete
			if(attr.refEntity.labelAttribute === undefined) {
				api.getAsync(attr.refEntity.href).done(function(refEntityMeta) {
					attr.refEntity = refEntityMeta;
					_render();
				});
			} else {
				_render();	
			}
		},
		

		/**
		 * Returns undefined in case no value is selected, returns null
		 * in case the nillable value is part of the dropdown and is
		 * selected else returns the selected value.
		 */
		getValue: function() {
			return this.value;
		}
	};
	
	// parent class for: EMAIL, HYPERLINK, STRING
	var AbstractStringControl = function(attr, props, $container) {
		if (this.constructor === AbstractStringControl) {
			throw new Error("can't instantiate abstract class");
		}
		
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	AbstractStringControl.prototype = {
		template: Handlebars.compile('<input type="{{type}}" class="form-control"{{#if value}} value="{{value}}"{{/if}}{{#if placeholder}} placeholder="{{placeholder}}"{{/if}}{{#if nillable}}{{else}} required{{/if}}>'),
				
		init: function() {
			var self = this;
			
			var props = self.props;
			var $container = self.$container;
			
			$container.off();
			$container.empty();
			
			$container.on('change', 'input', function(e) {
				var rawVal = $(this).val();
				self.value = rawVal !== '' ? rawVal : undefined;
				if(props.onValueChange) {
					props.onValueChange(self.value);
				}
			});
			
			this.value = props.value;
			this.render();
		},
		
		render: function() {
			var self = this;
			var props = this.props;
			this.$container.html(this.template({
				type : props.type,
				nillable : this.attr.nillable,
				value : self.value,
				placeholder : props.placeholder
			}));
		},
		
		getValue: function() {
			return this.value;
		}
	};
	
	// parent class for: HTML, SCRIPT, TEXT
	var AbstractTextControl = function(attr, props, $container) {
		if (this.constructor === AbstractTextControl) {
			throw new Error("can't instantiate abstract class");
		}
		
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	AbstractTextControl.prototype = {
		template: Handlebars.compile('<textarea class="form-control"{{#if nillable}}{{else}} required{{/if}}>{{#if value}}{{value}}{{/if}}</textarea>'),
				
		init: function() {
			var self = this;
			var $container = self.$container;
			var props = self.props;
			
			$container.off();
			$container.empty();
			
			$container.on('change', 'input', function(e) {
				var rawValue = $(this).val();
				self.value = rawValue !== '' ? rawValue : null;
				if(props.onValueChange) {
					props.onValueChange(self.value);
				}
			});
			
			this.render();
		},
		
		render: function() {
			var self = this;
			self.$container.html(self.template({
				type : self.type,
				nillable : self.attr.nillable,
				value : self.value,
				placeholder : self.placeholder
			}));
		},
		
		getValue: function() {
			return this.value;
		}
	};
	
	// BOOL
	var BoolControl = function(attr, props, $container) {
		this.attr = attr;
		this.props = props;
		this.value = this._normalizeValue(this.props.value);
		this.$container = $container;
		
		this.init();
	};
	
	BoolControl.prototype = {
		template: Handlebars.compile('\
				{{#if nillable}}\
				<label class="radio-inline">\
					<input type="radio" name="bool-radio" value="true"{{#ifCond value "===" "true"}} checked{{/ifCond}}> True\
				</label>\
				<label class="radio-inline">\
					<input type="radio" name="bool-radio" value="false"{{#ifCond value "===" "false"}} checked{{/ifCond}}> False\
				</label>\
				<label class="radio-inline">\
					<input type="radio" name="bool-radio" value="null"{{#ifCond value "===" "null"}} checked{{/ifCond}}> N/A\
				</label>\
				{{else}}\
				<div class="checkbox">\
				<label>\
				    <input type="checkbox"{{#if id}} id="{{id}}"{{/if}}{{#ifCond value "===" "true"}} checked{{/ifCond}}>{{#if label}} {{label}}{{else}}&nbsp;{{/if}}\
				</label>\
				</div>\
				{{/if}}'),
				
		init: function() {
			var self = this;
			
			var attr = this.attr;
			var props = this.props;
			var $container = this.$container;
			
			$container.off();
			$container.empty();
			
			if(attr.nillable) {
				$container.on('change', 'input', function(e) {
					var val = $('input:checked', $container).val();
					self.value = val === 'true' ? true : (val === 'false' ? false : null);
					if(props.onValueChange) {
						props.onValueChange(self.value);
					}
				});
			} else {
				$container.on('change', 'input', function(e) {
					self.value = $('input', $container).prop('checked');
					if(props.onValueChange) {
						props.onValueChange(self.value);
					}
				});
			}
			
			this.render();
		},
		
		render: function() {
			var self = this;
			var attr = this.attr;
			var props = this.props;
			var $container = this.$container;
			
			$container.html(this.template({
				id: props.id,
				nillable: attr.nillable,
				value : self.value === true ? 'true' : (self.value === false ? 'false' : (self.value === null ? 'null' : undefined)),
				label: props.label
			}));
		},
		
		getValue: function() {
			return this.value;
		},
		
		_normalizeValue : function(value) {
			switch(value) {
				case 'true':
					return true;
				case 'false':
					return false;
				case 'null':
					return null;
				default:
					return value;
			}
		}
	};
	
	// CATEGORICAL
	var CategoricalControl = function(attr, props, $container) {
		AbstractRefControl.call(this, attr, $.extend({}, props, {multiple: false}), $container);
	};
	
	CategoricalControl.prototype = Object.create(AbstractRefControl.prototype);
	
	// DATE and DATE_TIME
	var DateControl = function(attr, props, $container) {
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
		
	DateControl.prototype = {
			template: Handlebars.compile('\
					<div class="input-group date group-append">\
						<input type="text" class="form-control" data-date-format="{{dateFormat}}"{{#if value}} value="{{value}}"{{/if}}{{#if placeholder}} placeholder="{{placeholder}}"{{/if}}>\
						{{#if nillable}}\
						<span class="input-group-addon">\
							<span class="glyphicon glyphicon-remove empty-date-input"></span>\
						</span>\
						{{/if}}\
						<span class="input-group-addon datepickerbutton">\
							<span class="glyphicon glyp2icon-calendar"></span>\
						</span>\
					</div>'),
			init: function() {
				var self = this;
				var props = self.props;
				var $container = self.$container;
				
				$container.off();
				$container.empty();
				
				this.value = props.value;
				
				$container.on('change', 'div.date', function(e) {
					self.value = $('input', $container).val();
					if (props.onValueChange) {
						props.onValueChange(self.value);
					}
				});
				
				if(self.attr.nillable) {
					$container.on('click', 'span.glyphicon-remove', function(e) {
						self.value = null;
						$('input[name="date-selector"]', $container).val(self.value);
						if(props.onValueChange) {
							props.onValueChange(self.value);
						}
					});
				}
				this.render();
			},
			
			render: function() {
				var self = this;
				 
				self.$container.html(self.template({
					dateFormat : self.attr.fieldType === 'DATE' ? 'YYYY-MM-DD' : 'YYYY-MM-DDTHH:mm:ssZZ',
					value: self.value,
					nillable: self.attr.nillable,
					placeholder: self.props.placeholder
				}));
				
				var datetimepickerSettings = self.attr.fieldType === 'DATE' ? { pickTime : false } : { pickTime : true, useSeconds : true };
				$('.input-group', self.$container).datetimepicker(datetimepickerSettings);
			},
			
			getValue: function() {
				return this.value;
			}
	};
	
	// DECIMAL
	var DecimalControl = function(attr, props, $container) {
		AbstractNumberControl.call(this, attr, $.extend({}, props, {step: 'any'}), $container);
	};
		
	DecimalControl.prototype = Object.create(AbstractNumberControl.prototype);
	
	// EMAIL
	var EmailControl = function(attr, props, $container) {
		AbstractStringControl.call(this, attr, $.extend({}, props, {type: 'email'}), $container);
	};
		
	EmailControl.prototype = Object.create(AbstractStringControl.prototype);
	EmailControl.prototype.getValue = function() { return this.value; };
	
	// ENUM
	var EnumControl = function(attr, props, $container) {
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	EnumControl.prototype = {
		template: Handlebars.compile('\
				<select class="form-control select2">\
					<option></option>\
				{{#each values}}\
					<option value="{{this}}"{{#ifCond this "===" ../value}} selected{{/ifCond}}>{{this}}</option>{{this}}</option>\
				{{/each}}\
				</select>'),
				
		init: function() {
			var self = this;
			var props = self.props;
			var $container = self.$container;
			
			$container.off();
			$container.empty();
			
			$container.on('change', '.select2', function(e) {
				var rawVal = $(this).val();
				self.value = rawVal !== '' ? rawVal : null;
				if(props.onValueChange) {
					props.onValueChange(self.value);
				}
			});
			
			this.value = self.props.value;
			this.render();
		},
		
		render: function() {
			var attr = this.attr;
			var props = this.props;
			var $container = this.$container;
			
			$container.html(this.template({
				value : this.value,
				values : attr.enumOptions
			}));
			
			var opts = {
				allowClear : attr.nillable ? true : false,
				placeholder: props.placeholder
			}
			$('.select2', $container).select2(opts);
		},
		
		getValue: function() {
			return this.value;
		}
	};
	
	// HTML
	var HtmlControl = function(attr, props, $container) {
		AbstractTextControl.call(this, attr, $.extend({}, props, {}), $container);
	};
		
	HtmlControl.prototype = Object.create(AbstractTextControl.prototype);
	
	// HYPERLINK
	var HyperlinkControl = function(attr, props, $container) {
		AbstractStringControl.call(this, attr, $.extend({}, props, {type: 'url'}), $container);
	};
		
	HyperlinkControl.prototype = Object.create(AbstractStringControl.prototype);
	
	// INT
	var IntControl = function(attr, props, $container) {
		AbstractNumberControl.call(this, attr, $.extend({}, props, {step: '1'}), $container);
	};
		
	IntControl.prototype = Object.create(AbstractNumberControl.prototype);
	
	// LONG
	var LongControl = function(attr, props, $container) {
		AbstractNumberControl.call(this, attr, $.extend({}, props, {step: '1'}), $container);
	};
		
	LongControl.prototype = Object.create(AbstractNumberControl.prototype);
	
	// MREF
	var MrefControl = function(attr, props, $container) {
		AbstractRefControl.call(this, attr, $.extend({}, props, {multiple: true}), $container);
	};
		
	MrefControl.prototype = Object.create(AbstractRefControl.prototype);
	
	// SCRIPT
	var ScriptControl = function(attr, props, $container) {
		AbstractTextControl.call(this, attr, $.extend({}, props, {}), $container);
	};
		
	ScriptControl.prototype = Object.create(AbstractTextControl.prototype);
	
	// STRING
	var StringControl = function(attr, props, $container) {
		AbstractStringControl.call(this, attr, $.extend({}, props, {type: 'text'}), $container);
	};
	
	StringControl.prototype = Object.create(AbstractStringControl.prototype);

	// TEXT
	var TextControl = function(attr, props, $container) {
		AbstractStringControl.call(this, attr, $.extend({}, props, {}), $container);
	};
	
	TextControl.prototype = Object.create(AbstractTextControl.prototype);
	
	// XREF
	var XrefControl = function(attr, props, $container) {
		AbstractRefControl.call(this, attr, $.extend({}, props, {multiple: false}), $container);
	};
	
	XrefControl.prototype = Object.create(AbstractRefControl.prototype);
	
	molgenis.controls.create = function(attr, props, container) {
		switch(attr.fieldType) {
			case 'BOOL':
				return new BoolControl(attr, props, container);
			case 'CATEGORICAL':
				var controlProps = $.extend({}, {placeholder: 'Select a Category'}, props);
				return new CategoricalControl(attr, controlProps, container);
			case 'DATE':
			case 'DATE_TIME':
				return new DateControl(attr, props, container);
			case 'DECIMAL':
				var controlProps = $.extend({}, {placeholder: 'Number'}, props);
				return new DecimalControl(attr, controlProps, container);
			case 'EMAIL':
				var controlProps = $.extend({}, {placeholder: 'Email'}, props);
				return new EmailControl(attr, controlProps, container);
			case 'ENUM':
				var controlProps = $.extend({}, {placeholder: 'Select an Option'}, props);
				return new EnumControl(attr, controlProps, container);
			case 'HTML':
				return new HtmlControl(attr, props, container);
			case 'HYPERLINK':
				var controlProps = $.extend({}, {placeholder: 'URL'}, props);
				return new HyperlinkControl(attr, controlProps, container);
			case 'INT':
				var controlProps = $.extend({}, {placeholder: 'Number'}, props);
				return new IntControl(attr, controlProps, container);
			case 'LONG':
				var controlProps = $.extend({}, {placeholder: 'Number'}, props);
				return new LongControl(attr, controlProps, container);
			case 'MREF':
				var controlProps = $.extend({}, {placeholder: 'Search for values'}, props);
				return new MrefControl(attr, controlProps, container);
			case 'SCRIPT':
				return new ScriptControl(attr, props, container);
			case 'STRING':
				return new StringControl(attr, props, container);
			case 'TEXT':
				return new TextControl(attr, props, container);
			case 'XREF':
				var controlProps = $.extend({}, {placeholder: 'Search for a Value'}, props);
				return new XrefControl(attr, controlProps, container);
			case 'COMPOUND' :
				throw 'TODO discuss';
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attr.fieldType;
			default:
				throw 'Unknown data type: ' + attr.fieldType;
		}
	};
}($, window.top.molgenis = window.top.molgenis || {}));