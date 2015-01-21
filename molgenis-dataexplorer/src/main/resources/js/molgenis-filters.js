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
	
	var ComplexFilter = function(attr, props, Filter, $container) {
		this.attr = attr;
		this.props = props;
		this.Filter = Filter;
		this.$container = $container;
		
		this.init();
	}
	
	ComplexFilter.prototype = {					
		init: function() {
			var props = this.props;
			var $container = this.$container;
			
			$container.off();
			$container.empty();
			
			this.query = this.props.query;
			this.filters = [];
			this.operators = this.attr.fieldType === 'MREF' ? ['OR', 'AND'] : ['OR'];
			
			this.render();
		},
		
		render: function() {
			var query = this.query;
			
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
		
		getQuery: function() {
			return this.query;
		},
		
		_updateQuery: function() {
			var filters = this.filters;
			if(filters.length === 0) {
				this.query = null;
			}
			else if(filters.length === 1) {
				var filterQuery = filters[0].getQuery();
				
				// remove filters with null values resulting from empty inputs
				if(filterQuery && filterQuery.value !== null) {
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
				self.filters.push(new ComplexFilterComponentOperator(self.attr, operatorProps, $operatorContainer));
			}
			
			// add filter component
			var componentProps = {
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
			};
			var $componentContainer = $('<div></div>');
			$container.append($componentContainer);
			self.filters.push(new ComplexFilterComponent(self.attr, componentProps, self.Filter, $componentContainer));
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
	
	var ComplexFilterComponent = function(attr, props, Filter, $container) {
		this.attr = attr;
		this.props = props;
		this.Filter = Filter;
		this.$container = $container;
		
		this.init();
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
						
			init: function() {
				var props = this.props;
				var $container = this.$container;
				
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
				this.render();
			},
			
			render: function() {
				var props = this.props;
				var $container = this.$container; 
				$container.html(this.template({add: props.add, remove: props.remove}));
				this.filter = new this.Filter(this.attr, props, $('.filter-component-container', $container));
			},
			
			getQuery : function() {
				return this.filter.getQuery();
			}
	};

	var ComplexFilterComponentOperator = function(attr, props, $container) {
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	}
	
	ComplexFilterComponentOperator.prototype = {
			template: Handlebars.compile('\
					<div class="form-group">\
						<div class="row">\
							<div class="col-md-offset-4 col-md-2">\
							{{#ifCond operators.length "===" 1}}\
								<span>{{operators.[0]}}</span>\
							{{else}}\
								<select class="form-control input-sm">\
								{{#each operators}}\
									<option value="{{this}}"{{#ifCond this "===" operator}} selected{{/ifCond}}>{{this}}</option>\
								{{/each}}\
								</select>\
							{{/ifCond}}\
							</div>\
						</div>\
					</div>'),
						
			init: function() {
				var self = this;
				var props = this.props;
				var $container = this.$container;
				
				$container.off();
				$container.empty();
				
				this.query = this.props.query;
				
				var operators = this.props.operators;
				if(operators.length > 1) {
					$container.on('change', 'select', function(e) {
						self._updateQuery($(this).val());
					});
				}
				this.render();
			},
			
			render: function() {
				this.$container.html(this.template({
					operator: this.props.operator,
					operators: this.props.operators
				}));
			},
			
			getQuery: function() {
				return this.query;
			},
			
			_updateQuery: function(operator) {
				this.query = {operator: $(this).val()};
				if(props.onQueryChange) {
					props.onQueryChange(this.query);
				}
			}
	};
	
	var RangeValueFilter = function(attr, props, $container) {
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	RangeValueFilter.prototype = {
		template: Handlebars.compile('\
					<div class="form-group">\
						<div class="from-filter-container"></div>\
					</div>\
					<div class="form-group">\
						<div class="to-filter-container"></div>\
					</div>'),

		init: function() {
			var $container = this.$container;
			
			$container.off();
			$container.empty();
			
			this.query = this.props.query;
			
			this.render();
		},
		
		render: function() {
			var self = this;
			var attr = this.attr;
			var query = this.query;
			var $container = this.$container; 
			$container.html(this.template({}));

			var fromValue, toValue;
			if(query) {
				if(query.operator === 'NESTED') {
					fromValue = query.nestedRules[0].value;
					toValue = query.nestedRules[2].value;
				} else if(query.operator === 'GREATER_EQUAL') {
					fromValue = query.value;
					toValue = null;
				} else if(query.operator === 'LOWER_EQUAL') {
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
					}
				}, $('.from-filter-container', $container)),
				
				to: controls.create(attr, {
					value: toValue,
					onValueChange : function(value) {
						self._updateQuery();
					}
				}, $('.to-filter-container', $container))
			}
		},
		
		getQuery: function() {
			return this.query;
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
					operator : 'LOWER_EQUAL',
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
	
	var ValueFilter = function(attr, props, $container) {
		this.attr = attr;
		this.props = props;
		this.$container = $container;
		
		this.init();
	};
	
	ValueFilter.prototype = {
		template: Handlebars.compile('\
				<div class="form-group">\
					<div class="value-filter-container"></div>\
				</div>'),
					
		init: function() {
			this.query = this.props.query;
			
			this.render();
		},
		
		render: function() {
			var self = this;
			var $container = self.$container;
			
			$container.html(self.template({}));
			
			var controlProps = $.extend({}, self.props, {
				value : self.query ? self.query.value : null, 
				onValueChange : function(value) {
					self._updateQuery();
				}
			});
			self.control = controls.create(self.attr, controlProps, $('.value-filter-container', $container));
		},
		
		getQuery: function() {
			return this.query;
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
	
	molgenis.filters.create = function(attr, props, container) {
		switch(attr.fieldType) {
			case 'BOOL':
				return new ValueFilter(attr, props, container);
			case 'CATEGORICAL':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'DATE':
			case 'DATE_TIME':
				return new ComplexFilter(attr, props, RangeValueFilter, container);
			case 'DECIMAL':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'EMAIL':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'ENUM':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'HTML':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'HYPERLINK':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'INT':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'LONG':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'MREF':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'SCRIPT':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'STRING':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'TEXT':
				return new ComplexFilter(attr, props, ValueFilter, container);
			case 'XREF':
				return new ComplexFilter(attr, props, ValueFilter, container);
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
