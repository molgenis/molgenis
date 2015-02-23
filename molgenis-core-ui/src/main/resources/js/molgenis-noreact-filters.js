/**
 * MOLGENIS attribute filters for all data types
 * 
 * Dependencies: TODO
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.filters = molgenis.filters || {};

	var controls = molgenis.controls;
	var api = new molgenis.RestClient();
	
	var ResettableFilter = function(Filter) {
		this.Filter = Filter;
	}
	
	ResettableFilter.prototype = {
		template: Handlebars.compile('\
				<div class="row">\
					<div class="col-md-10 filter-container"></div>\
					<div class="col-md-2">\
						<button class="btn btn-warning clear-filter-btn{{#if hideButton}} hidden{{/if}}" type="button"><span class="glyphicon glyphicon-remove"></span></button>\
					</div>\
				</div>'),
		
					
		init: function($container) {
			var self = this;
			self.$container = $container;
			
			$container.off();
			$container.empty();
			
			$container.on('click', '.clear-filter-btn', function(e) {
				self.Filter.setQuery(null);
			});
			
			var onQueryChange = self.Filter.props.onQueryChange;
			self.Filter.setProps({
				onQueryChange: function(query) {
					if(query !== null) {
						$('.clear-filter-btn', $container).removeClass('hidden');
					} else {
						$('.clear-filter-btn', $container).addClass('hidden');
					}
					if(onQueryChange) {
						onQueryChange(query);
					}
				}
			})
			self.render();
			return self;
		},
		
		render: function() {
			var $container = this.$container;
			var query = this.Filter.getQuery();
			$container.html(this.template({hideButton: query === null || query === undefined}));
			this.Filter.init($('.filter-container', $container));
		},
		
		getAttribute: function() {
			return this.Filter.getAttribute();
		},
		
		getQuery: function() {
			return this.Filter.getQuery();
		},
		
		setQuery: function(query) {
			return this.Filter.setQuery(query);
			this.render();
		},
		
		setProps: function(props) {
			this.props = $.extend(this.props, props);
		},
	}
	
	var ComplexFilter = function(attr, props, Filter) {
		this.attr = attr;
		this.props = props;
		this.Filter = Filter;
		this.query = props.query;
	}
	
	ComplexFilter.prototype = {
		init: function($container) {
			this.$container = $container;
			var props = this.props;
			
			$container.off();
			$container.empty();
			
			this.filters = [];
			this.operators = this.attr.fieldType === 'MREF' ? ['OR', 'AND'] : ['OR'];
			
			this.render();
			return this;
		},
		
		render: function() {
			var query = this.query;
			this.filters = [];
			this.$container.empty();
			
			if(query) {
				if(query.operator === 'NESTED') {
					var rules = query.nestedRules;
					if (rules.length === 0) {
						this._createFilterComponent();
					} else {
						for (var i = 0; i < rules.length; i += 2) {
							this._createFilterComponent(rules[i], i > 0 ? rules[i - 1] : null);
						}
					}
				} else {
					this._createFilterComponent(query);
				}
			} else {
				this._createFilterComponent();
			}
		},
		
		getAttribute: function() {
			return this.attr;
		},
		
		getQuery: function() {
			return this.query;
		},
		
		setQuery: function(query) {
			this.query = query;
			this.render();
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		},
		
		setProps: function(props) {
			this.props = $.extend(this.props, props);
		},
		
		_updateQuery: function() {
			var filters = this.filters;
			if(filters.length === 0) {
				this.query = null;
			}
			else if(filters.length === 1) {
				var filterQuery = filters[0].getQuery();
				
				// remove filters with null values resulting from empty inputs
				if(filterQuery && filterQuery.value !== null) { // FIXME undefined?
					this.query = filterQuery;
				} else {
					this.query = null;
				}
			} else {
				var rules = [];
				for(var i = 0; i < filters.length; i += 2) {
					var filterQuery = filters[i].getQuery();
					var operatorQuery = i > 0 ? filters[i - 1].getQuery() : null;
					
					// remove filters with null values resulting from empty inputs
					if(filterQuery && filterQuery.value !== null) {
						if(operatorQuery && rules.length > 0) {
							rules.push(operatorQuery);	
						}
						rules.push(filterQuery);
					}
				}
				if(rules.length === 0) {
					this.query = null;
				} else if(rules.length === 1) {
					this.query = rules[0];
				} else {
					this.query = {operator:'NESTED', nestedRules: rules};
				}
			}
			
			if(this.props.onQueryChange) {
				this.props.onQueryChange(this.getQuery());
			}
		},
		
		_createFilterComponent: function(query, operator) {
			var self = this;
			var $container = self.$container;
			
			if(self.filters.length > 0) {
				// add query operator component
				var operatorProps = {
					index: self.filters.length,
					query: operator,
					operators: self.operators,
					onQueryChange: function() {
						self._updateQuery();
					}
				};
				var $operatorContainer = $('<div></div>');
				$container.append($operatorContainer);
				
				var filterOperator = new ComplexFilterComponentOperator(self.attr, operatorProps);
				self.filters.push(filterOperator);
				filterOperator.init($operatorContainer);
			}
			
			// add filter component
			var componentProps = $.extend({}, self.props, {
				index: self.filters.length,
				query: query,
				add: true,
				remove: self.filters.length > 1,
				onAddFilterComponent: function() {
					self._onAddFilterComponent();
				},
				onRemoveFilterComponent: function(index) {
					self._onRemoveFilterComponent(index);
				},
				onQueryChange: function() {
					self._updateQuery();
				}
			});
			var $componentContainer = $('<div></div>');
			$container.append($componentContainer);
			
			var filterOperator = new ComplexFilterComponent(self.attr, componentProps, self.Filter);
			self.filters.push(filterOperator);
			filterOperator.init($componentContainer);
		},
		
		_onAddFilterComponent: function() {
			this._createFilterComponent(null, {operator: 'OR'});
		},
		
		_onRemoveFilterComponent: function(index) {
			var self = this;
			
			// remove filter from DOM and filter list
			self.filters[index].$container.remove();
			self.filters[index] = null;
			
			if(index > 0) {
				// remove query operator component from DOM and filter list
				self.filters[index - 1].$container.remove();
				self.filters[index - 1] = null;	
			}
			
			self.filters.clean(null);
			
			self._updateQuery();
		}
	};
	
	var ComplexFilterComponent = function(attr, props, Filter) {
		this.attr = attr;
		this.props = props;
		this.Filter = Filter;
	}
	
	ComplexFilterComponent.prototype = {
		template: Handlebars.compile('\
					<div class="row">\
						<div class="col-md-9 filter-component-container"></div>\
						<div class="col-md-3">\
							{{#if add}}<button class="btn btn-default add-filter-component-btn" type="button"><span class="glyphicon glyphicon-plus"></span></button>{{/if}}\
							{{#if remove}}<button class="btn btn-default remove-filter-component-btn" type="button"><span class="glyphicon glyphicon-minus"></span></button>{{/if}}\
						</div>\
					</div>'),
					
		init: function($container) {
			this.$container = $container; 
			var props = this.props;
			
			$container.off();
			$container.empty();
			
			if(props.add) {
				$container.on('click', '.add-filter-component-btn', function(e) {
					if(props.onAddFilterComponent) {
						props.onAddFilterComponent();
					}
				});
			}
			
			if(props.remove) {
				$container.on('click', '.remove-filter-component-btn', function(e) {
					if(props.onRemoveFilterComponent) {
						props.onRemoveFilterComponent(props.index);
					}
				});
			}
			
			this.filter = new this.Filter(this.attr, props); // FIXME store in props
			this.render();
			return this;
		},
		
		render: function() {
			var props = this.props;
			var $container = this.$container; 
			$container.html(this.template({add: props.add, remove: props.remove}));
			this.filter.init($('.filter-component-container', $container));
		},
		
		getAttribute: function() {
			return this.filter.getAttribute();
		},
		
		getQuery : function() {
			return this.filter.getQuery();
		},
		
		setQuery: function(query) {
			this.Filter.setQuery(query);
			this.render();
		},
		
		setProps: function(props) {
			this.props = $.extend(this.props, props);
		},
	};

	var ComplexFilterComponentOperator = function(attr, props) {
		this.attr = attr;
		this.props = props;
		this.query = props.query;
	}
	
	ComplexFilterComponentOperator.prototype = {
		template: Handlebars.compile('\
				<div class="form-group">\
					<div class="col-md-offset-3 col-md-3">\
					{{#ifCond operators.length "===" 1}}\
						<div class="text-center">{{operators.[0]}}</div>\
					{{else}}\
						<select class="form-control input-sm">\
						{{#each operators}}\
							<option value="{{this}}"{{#ifCond this "===" operator}} selected{{/ifCond}}>{{this}}</option>\
						{{/each}}\
						</select>\
					{{/ifCond}}\
					</div>\
				</div>'),
					
		init: function($container) {
			this.$container = $container;
			var self = this;
			var props = this.props;
			
			$container.off();
			$container.empty();
			
			var operators = this.props.operators;
			if(operators.length > 1) {
				$container.on('change', 'select', function(e) {
					self._updateQuery($(this).val());
				});
			}
			this.render();
			return this;
		},
		
		render: function() {
			this.$container.html(this.template({
				operator: this.props.operator,
				operators: this.props.operators
			}));
		},
		
		getAttribute: function() {
			return this.attr;
		},
		
		getQuery: function() {
			return this.query;
		},
		
		setQuery: function(query) {
			this.query = query;
			this.render();
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		},
		
		_updateQuery: function(operator) {
			this.query = {operator: $(this).val()};
			if(props.onQueryChange) {
				props.onQueryChange(this.query);
			}
		}
	};
	
	var RangeSliderFilter = function(attr, props, Filter) {
		this.attr = attr;
		this.props = props;
		this.query = props.query;
	};
	
	RangeSliderFilter.prototype = {
		template: Handlebars.compile('\
				<div class="form-group">\
					<div class="col-md-offset-1 col-md-10">\
						<div class="range-slider"></div>\
					</div>\
				</div>'),

		init: function($container) {
			this.$container = $container;
			var self = this;
			
			$container.off();
			$container.empty();

			$container.on('userValuesChanged', '.range-slider', function(e, data){
				self._updateQuery(data.values.min, data.values.max);
			});
			
			this.render();
			return this;
		},
		
		render: function() {
			var self = this;
			var attr = self.attr;
			var query = self.query;
			var props = self.props;
			
			var $container = self.$container;
			$container.html(self.template({}));
			
			var fromValue, toValue;
			if(query) {
				if(query.operator === 'NESTED') {
					fromValue = query.nestedRules[0].value;
					toValue = query.nestedRules[2].value;
				} else if(query.operator === 'GREATER_EQUAL') {
					fromValue = query.value;
					toValue = attr.range.max;
				} else if(query.operator === 'LESS_EQUAL') {
					fromValue = attr.range.min;
					toValue = query.value;
				}
			} else {
				fromValue = attr.range.min;
				toValue = attr.range.max;
			}
			
			$('.range-slider', $container).editRangeSlider({
				symmetricPositionning: true,
				bounds: {min: attr.range.min, max: attr.range.max},
				defaultValues: {min: fromValue, max: toValue},
				step: props.step,
				type: 'number'
			});
		},
		
		getAttribute: function() {
			return this.attr;
		},
		
		getQuery: function() {
			return this.query;
		},
		
		setQuery: function(query) {
			this.query = query;
			this.render();
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		},
		
		setProps: function(props) {
			this.props = $.extend(this.props, props);
		},
		
		_updateQuery: function(fromValue, toValue) {
			var attr = this.attr;
			var fromQuery, toQuery;

			if(fromValue !== attr.range.min) {
				fromQuery = {
					field : attr.name,
					operator : 'GREATER_EQUAL',
					value : fromValue
				};
			}
			
			if(toValue !== attr.range.max) {
				toQuery = {
					field : attr.name,
					operator : 'LESS_EQUAL',
					value : toValue
				};
			}
			
			if(fromQuery && toQuery) {
				this.query = {
					operator: 'NESTED',
					nestedRules: [fromQuery, {operator : 'AND'}, toQuery]
				};
			} else if(fromQuery) {
				this.query = fromQuery;
			} else if(toQuery) {
				this.query = toQuery;
			} else {
				this.query = null;
			}
			
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		}
	};
	
	var RangeValueFilter = function(attr, props, Filter) {
		this.attr = attr;
		this.props = props;
		this.query = props.query;
	};
	
	RangeValueFilter.prototype = {
		template: Handlebars.compile('\
				{{#ifCond layout "===" "horizontal"}}\
				<div class="form-group">\
					<div class="col-md-6 from-filter-container"></div>\
					<div class="col-md-6 to-filter-container"></div>\
				</div>\
				{{else}}\
				<div class="form-group">\
					<div class="col-md-12 from-filter-container"></div>\
				</div>\
				<div class="form-group">\
					<div class="col-md-12 to-filter-container"></div>\
				</div>\
				{{/ifCond}}'),

		init: function($container) {
			this.$container = $container;
			
			$container.off();
			$container.empty();
			
			this.render();
			return this;
		},
		
		render: function() {
			var self = this;
			
			var attr = self.attr;
			var props = self.props;
			var query = self.query;
			var $container = self.$container; 
			$container.html(self.template({
				layout: props.layout
			}));

			var fromValue, toValue;
			if(query) {
				if(query.operator === 'NESTED') {
					fromValue = query.nestedRules[0].value;
					toValue = query.nestedRules[2].value;
				} else if(query.operator === 'GREATER_EQUAL') {
					fromValue = query.value;
					toValue = null;
				} else if(query.operator === 'LESS_EQUAL') {
					fromValue = null;
					toValue = query.value;
				}
			} else {
				fromValue = null;
				toValue = null;
			}
			
			this.controls = {
				from: controls.create(attr, {
					value: fromValue,
					onValueChange : function(value) {
						self._updateQuery();
					},
					placeholder: props.fromPlaceholder
				}, $('.from-filter-container', $container)),
				
				to: controls.create(attr, {
					value: toValue,
					onValueChange : function(value) {
						self._updateQuery();
					},
					placeholder: props.toPlaceholder
				}, $('.to-filter-container', $container))
			}
		},
		
		getAttribute: function() {
			return this.attr;
		},
		
		getQuery: function() {
			return this.query;
		},
		
		setQuery: function(query) {
			this.query = query;
			this.render();
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		},
		
		setProps: function(props) {
			this.props = $.extend(this.props, props);
		},
		
		_updateQuery: function() {
			var fromQuery, toQuery;

			var fromValue = this.controls.from.getValue();
			if(fromValue !== null) {
				fromQuery = {
					field : this.attr.name,
					operator : 'GREATER_EQUAL',
					value : fromValue
				};
			}
			
			var toValue = this.controls.to.getValue();
			if(toValue !== null) {
				toQuery = {
					field : this.attr.name,
					operator : 'LESS_EQUAL',
					value : toValue
				};
			}
			
			if(fromQuery && toQuery) {
				this.query = {
					operator: 'NESTED',
					nestedRules: [fromQuery, {operator : 'AND'}, toQuery]
				};
			} else if(fromQuery) {
				this.query = fromQuery;
			} else if(toQuery) {
				this.query = toQuery;
			} else {
				this.query = null;
			}
			
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		}
	};
	
	var ValueFilter = function(attr, props, Filter) {
		this.attr = attr;
		this.props = props;
		this.query = props.query;
	};
	
	ValueFilter.prototype = {
		template: Handlebars.compile('\
				<div class="form-group">\
					<div class="col-md-{{cols}} value-filter-container"></div>\
				</div>'),
					
		init: function($container) {
			this.$container = $container;
			
			this.render();
			return this;
		},
		
		render: function() {
			var self = this;
			var $container = self.$container;
			
			$container.html(self.template({cols: self.props.cols || 12}));
			
			var controlProps = $.extend({}, self.props, {
				value : self.query ? self.query.value : undefined, 
				onValueChange : function(value) {
					self._updateQuery();
				}
			});
			self.control = controls.create(self.attr, controlProps, $('.value-filter-container', $container));
		},
		
		getAttribute: function() {
			return this.attr;
		},
		
		getQuery: function() {
			return this.query;
		},
		
		setQuery: function(query) {
			this.query = query;
			this.render();
			if (this.props.onQueryChange) {
				this.props.onQueryChange(this.query);
			}
		},
		
		setProps: function(props) {
			this.props = $.extend(this.props, props);
		},
		
		_updateQuery: function() {
			var self = this;
			
			var value = self.control.getValue();
			
			if(value !== undefined) {
				self.query = {
					field : self.attr.name,
					operator : 'EQUALS',
					value : value
				};
			} else {
				self.query = null;
			}
			
			if (self.props.onQueryChange) {
				self.props.onQueryChange(self.query);
			}
		}
	};
	
	molgenis.filters.create = function(attr, props, $container) {
		var filter;
		switch(attr.fieldType) {
			case 'BOOL':
				var controlProps = $.extend({}, props, {cols: 9}); // align value filters with complex filters
				filter = new ValueFilter(attr, controlProps);
				break;
			case 'CATEGORICAL':
			case 'XREF':
				// use nillable control for non-nillable attributes
				var controlAttr = $.extend({}, attr, {nillable: false});
				
				// align value filters with complex filters, add nillable  
				var controlProps = $.extend({}, props, {cols: 9, includeNillable: attr.nillable});
				filter = new ValueFilter(controlAttr, controlProps);
				break;
			case 'EMAIL':
			case 'ENUM':
			case 'HTML':
			case 'HYPERLINK':
			case 'MREF':
			case 'SCRIPT':
			case 'STRING':
			case 'TEXT':
				filter = new ComplexFilter(attr, props, ValueFilter);
				break;
			case 'DATE':
			case 'DATE_TIME':
				var controlProps = $.extend({}, props, {layout: 'vertical', fromPlaceholder: 'Start date', toPlaceholder: 'End date'});
				filter = new ComplexFilter(attr, controlProps, RangeValueFilter);
				break;
			case 'DECIMAL':
				var controlProps = $.extend({}, props, {layout: 'horizontal', fromPlaceholder: 'Start number', toPlaceholder: 'End number'});
				filter = new ComplexFilter(attr, controlProps, RangeValueFilter);
				break;
			case 'INT':
			case 'LONG':
				if(attr.range) {
					var controlProps = $.extend({}, props, {step: 1});
					filter = new ComplexFilter(attr, controlProps, RangeSliderFilter);
				} else {
					var controlProps = $.extend({}, props, {layout: 'horizontal', fromPlaceholder: 'Start number', toPlaceholder: 'End number'});
					filter = new ComplexFilter(attr, controlProps, RangeValueFilter);
				}
				break;
			case 'COMPOUND' :
				throw 'TODO discuss';
			case 'FILE':
			case 'IMAGE':
				throw 'Unsupported data type: ' + attr.fieldType;
			default:
				throw 'Unknown data type: ' + attr.fieldType;
		}
		
		return new ResettableFilter(filter).init($container);
	};
}($, window.top.molgenis = window.top.molgenis || {}));
