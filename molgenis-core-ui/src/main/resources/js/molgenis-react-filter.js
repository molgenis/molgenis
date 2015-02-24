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

	var div = React.DOM.div, label = React.DOM.label, span = React.DOM.span, button=React.DOM.button;
	var __spread = React.__spread;
	
	var ValueQueryMixin = {
		_toQuery: function(event) {
			return event.value !== undefined && event.value !== '' ? {
					field : event.attr,
					operator : 'EQUALS',
					value : event.value
				} : null;
		},
		_toValue: function(query) {
			return query ? query.value : undefined;
		}
	};
	
	/**
	 * Attribute value filter shared functions
	 */
	var ValueFilterMixin = {
		_createValueFilter: function(additionalProps) {
			switch(this.props.attr.fieldType) {
				case 'DATE':
				case 'DATE_TIME':
				case 'DECIMAL':
				case 'INT':
				case 'LONG':
					return RangeValueFilter(__spread({},  this.props, this.additionalProps, {onQueryChange: this.props.onQueryChange}));
//					return (<RangeValueFilter {...this.props} {...additionalProps} onQueryChange={this.props.onQueryChange} />);
				default:
//					return (<AttributeFilter {...this.props} {...additionalProps} onQueryChange={this.props.onQueryChange} />);
					return AttributeFilter(__spread({},  this.props, this.additionalProps, {onQueryChange: this.props.onQueryChange}));
			}
		}
	};
	
	/**
	 *  Attribute filter with one required value input and a null select
	 */
	var NillableValueFilter = React.createClass({
		mixins: [ValueQueryMixin, ValueFilterMixin],
		displayName: 'NillableValueFilter',
		getDefaultProps: function() {
			return {
				cols: 12
			};
		},
		getInitialState: function() {
			return {
				disabled: this._disableFilter(this.props.query)
			};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({
				disabled: this._disableFilter(nextProps.query)
			});
		},
		render: function() {console.log('render NillableValueFilter', this.state, this.props);
			var filter = this._createValueFilter({
				attr: $.extend({}, this.props.attr, {nillable: false}), // non-nillable attr
				disabled: this.state.disabled,
				cols: this.props.cols
			});

			var colClassName = "col-md-" + this.props.cols;
			var checked = this._disableFilter(this.props.query);
			return (
				div({},
					filter,
					div({className: 'form-group'},
						div({className: colClassName},
							controls.BoolControl({value: checked, label: 'N/A', onValueChange: this._handleNillableValueChange})
						)
					)
				)
//				<div>
//					{filter}
//					<div className="form-group">
//						<div className={colClassName}>
//							<controls.BoolControl value={checked} label="N/A" onValueChange={this._handleNillableValueChange} />
//						</div>
//					</div>
//				</div>
		    );
		},
		_handleNillableValueChange: function(event) {console.log('_handleNillableValueChange NillableValueFilter', event);
			this.setState({
				disabled: event.value
			});
			event.value = event.value === true ? null : undefined;
			this.props.onQueryChange({attr: this.props.attr.name, query: this._toQuery({attr: this.props.attr.name, value: event.value})});
		},
		_disableFilter: function(query) {
			return this._toValue(query) === null;
		}
	});
	
	/**
	 * Attribute filter with one required value input
	 */
	var NonNillableValueFilter = React.createClass({
		mixins: [ValueFilterMixin],
		displayName: 'NonNillableValueFilter',
		render: function() {console.log('render NonNillableValueFilter', this.state, this.props);
			var filter = this._createValueFilter(this.state);
			return filter;
		}
	});
	
	/**
	 * Attribute filter
	 */
	var ValueFilter = React.createClass({
		displayName: 'ValueFilter',
		render: function() {console.log('render ValueFilter', this.state, this.props);
			if(this.props.attr.nillable) {
//				return (<NillableValueFilter cols={this.props.cols} {...this.props} onQueryChange={this.props.onQueryChange} />);
				return NillableValueFilter(__spread({},  this.props, {cols: this.props.cols, onQueryChange: this.props.onQueryChange}));
			} else {
//				return (<NonNillableValueFilter cols={this.props.cols} {...this.props} onQueryChange={this.props.onQueryChange} />);
				return NonNillableValueFilter(__spread({},  this.props, {cols: this.props.cols, onQueryChange: this.props.onQueryChange}));
			}
		}
	});
				
	var ComposedFilterPart = React.createClass({
		displayName: 'ComposedFilterPart',
		render: function() {console.log('render ComposedFilterPart', this.state, this.props);
			if(this.props.query && this.props.query.operator === 'OR') { // FIXME not elegant
				return (
					div({className: 'form-group'},
						div({className: 'col-md-offset-3 col-md-3'},
							div({className: 'text-center'}, 'OR')
						)
					)
//						<div className="form-group">
//							<div className="col-md-offset-3 col-md-3">
//								<div className="text-center">OR</div>
//							</div>
//						</div>
			    );
			}

			var addBtnClasses = 'btn btn-default' + (this.props.hideAddBtn ? ' hidden' : '');
			var removeBtnClasses = 'btn btn-default' + (this.props.hideRemoveBtn ? ' hidden' : '');			
//			var filter = <ValueFilter {...this.props} query={this.props.query} onQueryChange={this._handleOnQueryChange} />;
			var filter = ValueFilter(__spread({},  this.props, {query: this.props.query, onQueryChange: this._handleQueryChange}));
					
			return (
					div({className: 'row'},
						div({className: 'col-md-9'},
							filter
						),
						div({className: 'col-md-3'},
							button({className: addBtnClasses, type: 'button', title: 'Add a filter clause', onClick: this._handleAddFilterPartClick},
								span({className: 'glyphicon glyphicon-plus'})
							),
							button({className: removeBtnClasses, type: 'button', title: 'Remove this filter clause', onClick: this._handleRemoveFilterPartClick},
								span({className: 'glyphicon glyphicon-minus'})
							)
						)
					)
//				<div className="row">
//					<div className="col-md-9">
//						{filter}
//					</div>
//					<div className="col-md-3">
//						<button className={addBtnClasses} type="button" title="Add a filter clause" onClick={this._handleAddFilterPartClick}>
//							<span className="glyphicon glyphicon-plus"></span>
//						</button>
//						<button className={removeBtnClasses} type="button" title="Remove this filter clause" onClick={this._handleRemoveFilterPartClick}>
//							<span className="glyphicon glyphicon-minus"></span>
//						</button>
//					</div>
//				</div>
		    );
		},
		_handleOnQueryChange: function(event) {console.log('_handleOnQueryChange ComposedFilterPart', event);
			this.props.onFilterPartQueryChange({index: this.props.index, event: event});
		},
		_handleAddFilterPartClick: function() {console.log('_handleAddFilterPartClick ComposedFilterPart');
			this.props.onAddFilterPart();
		},
		_handleRemoveFilterPartClick: function() {console.log('_handleRemoveFilterPartClick ComposedFilterPart');
			this.props.onRemoveFilterPart({index: this.props.index, attr: this.props.attr.name});
		},
	});
	
	/**
	 * Attribute filter composed of attribute filters that can be added/removed and operators that define the relations between them
	 */
	var ComposedFilter = React.createClass({
		displayName: 'ComposedFilter',
		getInitialState: function() {
			return {filters: this._toFilters(this.props.query)};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({filters: this._toFilters(nextProps.query)});
		},
		render: function() {console.log('render ComposedFilter', this.state, this.props);
			var self = this;
			var filterParts = $.map(this.state.filters, function(query, index) {
				return ComposedFilterPart(__spread({},  self.props, {
					query: query,
					index: index,
					key: 'part-' + index,
					hideAddBtn: self.props.query === null,
					hideRemoveBtn: self.props.query === null || self.state.filters.length === 1,
					onAddFilterPart: self._handleAddFilterPart,
					onRemoveFilterPart: self._handleRemoveFilterPart,
					onFilterPartQueryChange: self._handleOnFilterPartQueryChange
				}));
//				return <ComposedFilterPart {...self.props} query={query} index={index} key={'part-' + index}
//							hideAddBtn={self.props.query === null}
//							hideRemoveBtn={self.props.query === null || self.state.filters.length === 1}
//							onAddFilterPart={self._handleAddFilterPart}
//							onRemoveFilterPart={self._handleRemoveFilterPart}
//							onFilterPartQueryChange={self._handleOnFilterPartQueryChange} />;
			});
//			return (<div>{filterParts}</div>);
			return div({}, filterParts);
		},
		_handleOnFilterPartQueryChange: function(event) {console.log('_handleOnFilterPartQueryChange ComposedFilter', event);
			var filters = this.state.filters;
			filters[event.index] = event.event.query;
			this.setState({filters: filters});
			
			this.props.onQueryChange($.extend({}, event.event, {query: this._toQuery(filters)}));
		},
		_handleAddFilterPart: function() {console.log('_handleAddFilterPart ComposedFilter');
			var filters = this.state.filters;
			filters.push({operator: 'OR'}); // FIXME can be and for mref
			filters.push(null);
			this.setState({filters: filters});
		},
		_handleRemoveFilterPart: function(event) {console.log('_handleRemoveFilterPart ComposedFilter', event);
			var filters = this.state.filters;
			var removed = filters.splice(event.index > 0 ? event.index - 1 : 0, filters.length > 1 ? 2 : 1); // remove filter and operator
			console.log('removed', event.index, removed, filters);
			this.setState({filters: filters});
			
			this.props.onQueryChange({
				attr: event.attr,
				query: this._toQuery(filters)
			});
		},
		_toFilters: function(query) {
			var filters = [];
			if(query) {
				if(query.operator === 'NESTED') {
					for(var i = 0; i < query.nestedRules.length; ++i) {
						filters.push(query.nestedRules[i]);
					}
				} else {
					filters.push(query);
				}
			}
			
			if(filters.length === 0) {
				filters.push(null);
			}
			
			return filters;
		},
		_toQuery: function(filters) {
			var query;
			if(filters.length === 0) {
				query = null;
			} else if(filters.length === 1) {
				query = filters[0];
			} else {
				// remove nulls and related operators
				var rules = []; // TODO remove duplicates
				for(var i = 0; i < filters.length; i +=2) {
					if(filters[i] !== null) {
						if(i > 0) {
							rules.push(filters[i - 1]);
						}
						rules.push(filters[i]);
					}
				}
				
				if(rules.length === 0) {
					query = null;
				} else if(rules.length === 1) {
					query = filters[0];	
				} else {
					query = {operator:'NESTED', nestedRules: rules};	
				}
			}
			return query;
		}
	});
	
	
	
	/**
	 * Attribute range filter shared functions
	 */
	var RangeValueFilterMixin = {
		displayName: 'RangeValueFilterMixin',
		_handleFromQueryChange: function(event) {console.log('_handleFromQueryChange RangeValueFilterMixin', event);
			// merge query with query change
			if(this.props.query) {
				switch(this.props.query.operator) {
					case 'GREATER_EQUAL':
						if(event.query !== null) {
							event.query.operator = 'GREATER_EQUAL';
						}
						break;		
					case 'LESS_EQUAL':
						event.query.operator = 'RANGE';
						event.query.value = [event.query ? event.query.value : undefined, this.props.query.value];
						break;
					case 'RANGE':
						event.query.operator = 'RANGE';
						event.query.value = [event.query ? event.query.value : undefined, this.props.query.value[1]];
						break;
					default:
						return null;
				}
			} else {
				event.query.operator = 'GREATER_EQUAL';
			}
			this.props.onQueryChange(event);
		},
		_handleToQueryChange: function(event) {console.log('_handleToQueryChange RangeValueFilterMixin', event);
			// merge query with query change
			if(this.props.query) {
				switch(this.props.query.operator) {
					case 'GREATER_EQUAL':
						event.query.operator = 'RANGE';
						event.query.value = [this.props.query.value, event.query ? event.query.value : undefined];
						break;	
					case 'LESS_EQUAL':
						if(event.query !== null) {
							event.query.operator = 'LESS_EQUAL';
						}
						break;
					case 'RANGE':
						event.query.operator = 'RANGE';
						event.query.value = [this.props.query.value[0], event.query ? event.query.value : undefined];
						break;
					default:
						return null;
				}
			} else {
				event.query.operator = 'LESS_EQUAL';
			}
			this.props.onQueryChange(event);
		},
		_extractToQuery: function() {
			var query = this.props.query;
			if(query) {
				switch(query.operator) {
					case 'GREATER_EQUAL':
						query = null;
						break;
					case 'LESS_EQUAL':
						query = {
							field: query.field,
							operator: 'GREATER_EQUAL',
							value: query.value
						};
						break;
					case 'RANGE':
						query = {
							field: query.field,
							operator: 'GREATER_EQUAL',
							value: query.value[1]
						};
						break;
					default:
						return null;
				}
			}
			return query;
		},
		_extractFromQuery: function() {
			var query = this.props.query;
			if(query) {
				switch(query.operator) {
					case 'GREATER_EQUAL':
						query = {
							field: query.field,
							operator: 'LESS_EQUAL',
							value: query.value
						};
						break;
					case 'LESS_EQUAL':
						query = null;
						break;
					case 'RANGE':
						query = {
							field: query.field,
							operator: 'LESS_EQUAL',
							value: query.value[0]
						};
						break;
					default:
						return null;
				}
			}
			return query;
		}
	};
	
	/**
	 * Attribute range filter with a 'from' and a 'to' input next to each other 
	 */
	var HorizontalRangeValueFilter = React.createClass({
		mixins: [RangeValueFilterMixin],
		displayName: 'HorizontalRangeValueFilter',
		render: function() {console.log('render HorizontalRangeValueFilter', this.state, this.props);
			return (
					div({className: 'form-group'},
						div({className: 'col-md-6'},
							AttributeFilter(__spread({},  this.props, {placeholder: 'From', query: this._extractFromQuery(), onQueryChange: this._handleFromQueryChange}))
						),
						div({className: 'col-md-6'},
							AttributeFilter(__spread({},  this.props, {placeholder: 'To', query: this._extractToQuery(), onQueryChange: this._handleToQueryChange}))
						)
					)
//				<div className="form-group">
//					<div className="col-md-6">
//						<AttributeFilter {...this.props} placeholder="From" query={this._extractFromQuery()} onQueryChange={this._handleFromQueryChange} />
//					</div>
//					<div className="col-md-6">
//						<AttributeFilter {...this.props} placeholder="To" query={this._extractToQuery()} onQueryChange={this._handleToQueryChange} />
//					</div>
//				</div>	
		    );
		}
	});
	
	/**
	 * Attribute range filter with a 'to' input below a 'from' input 
	 */
	var VerticalRangeValueFilter = React.createClass({
		mixins: [RangeValueFilterMixin],
		displayName: 'VerticalRangeValueFilter',
		render: function() {console.log('render VerticalRangeValueFilter', this.state, this.props);
			return(
				div({},
					div({className: 'form-group'},
						div({className: 'col-md-12'},
							AttributeFilter(__spread({},  this.props, {placeholder: 'From', query: this._extractFromQuery(), onQueryChange: this._handleFromQueryChange}))
						)
					),
					div({className: 'form-group'},
						div({className: 'col-md-12'},
							AttributeFilter(__spread({},  this.props, {placeholder: 'To', query: this._extractToQuery(), onQueryChange: this._handleToQueryChange}))
						)
					)
				)
//				<div>
//					<div className="form-group">
//						<div className="col-md-12">
//							<AttributeFilter {...this.props} placeholder="From" query={this._extractFromQuery()} onQueryChange={this._handleFromQueryChange} />
//						</div>
//					</div>
//					<div className="form-group">
//						<div className="col-md-12">
//							<AttributeFilter {...this.props} placeholder="To" query={this._extractToQuery()} onQueryChange={this._handleToQueryChange} />
//						</div>
//					</div>
//				</div>
			);
		}
	});
	
	/**
	 * Attribute range filter with a slider 
	 */
	var RangeSliderFilter = React.createClass({// FIXME init with value
		mixins: [RangeValueFilterMixin],
		displayName: 'RangeSliderFilter',
		render: function() {console.log('render RangeSliderFilter', this.state, this.props);
			var value = this._toValue(this.props.query);
			return(
				div({className: 'form-group'},
					div({className: 'col-md-offset-1 col-md-10'},
						controls.RangeSlider({range: this.props.attr.range, step: this.props.step, disabled: this.props.disabled, value: value, onValueChange: this._handleValueChange})
					)
				)
//				<div className="form-group">
//					<div className="col-md-offset-1 col-md-10">
//						<controls.RangeSliderControl range={this.props.attr.range} step={this.props.step} disabled={this.props.disabled} value={value} onValueChange={this._handleValueChange} />
//					</div>
//				</div>
			); //FIXME handle change event
		},
		_handleValueChange: function(event) {console.log('_handleValueChange RangeSliderFilter', event);
			var attr = this.props.attr;

			var fromValue = event.value[0] !== attr.range.min ? event.value[0] : undefined;
			var toValue = event.value[1] !== attr.range.max ? event.value[1] : undefined;

			var query;
			if(fromValue && toValue) {
				query = {
					field : attr.name,
					operator : 'RANGE',
					value : [fromValue, toValue]
				};
			} else if(fromValue) {
				query = {
					field : attr.name,
					operator : 'GREATER_EQUAL',
					value : fromValue
				};
			} else if(toValue) {
				query = {
					field : attr.name,
					operator : 'LESS_EQUAL',
					value : toValue
				};
			} else {
				query = null;
			}
			
			this.props.onQueryChange({attr: attr.name, query: query});
		},
		_toValue: function(query) {
			var value;
			if(query) {
				if(query.operator === 'GREATER_EQUAL') {
					value = [query.value, undefined];
				} else if(query.operator === 'LESS_EQUAL') {
					value = [undefined, query.value];
				} else if(query.operator === 'RANGE') {
					value = query.value;
				} else {
					value = undefined;
				}
			} else {
				value = undefined;
			}
			return value;
		}
	});
	
	/**
	 * Attribute range filter with a 'from' and 'to' input
	 */
	var RangeValueFilter = React.createClass({
		displayName: 'RangeValueFilter',
		render: function() {console.log('render RangeValueFilter', this.state, this.props);
			var attrType = this.props.attr.fieldType;
			switch(attrType) {
				case 'DECIMAL':
				case 'INT':
				case 'LONG':
					if(this.props.attr.range) {
						var step = attrType === 'DECIMAL' ? false : '1';
//						return (<RangeSliderFilter {...this.props} step={step} onQueryChange={this.props.onQueryChange} />);
						return RangeSliderFilter(__spread({},  this.props, {step: step, onQueryChange: this.props.onQueryChange}));
					}
					else {
//						return (<HorizontalRangeValueFilter {...this.props} onQueryChange={this.props.onQueryChange} />);
						return HorizontalRangeValueFilter(__spread({},  this.props, {onQueryChange: this.props.onQueryChange}));
					}
				default:
//					return (<VerticalRangeValueFilter {...this.props} onQueryChange={this.props.onQueryChange} />);
					return VerticalRangeValueFilter(__spread({},  this.props, {onQueryChange: this.props.onQueryChange}));
			}
		}
	});
	
	
	
	
	
	var AttributeFilter = React.createClass({
		mixins: [ValueQueryMixin],
		displayName: 'AttributeFilter',
		getDefaultProps: function() {
			return {
				cols: 12
			};
		},
		render: function() {console.log('render AttributeFilter', this.state, this.props);
			var value = this._toValue(this.props.query);

			var colClassName = "col-md-" + this.props.cols;
			return (
				div({className: 'row'},
					div({className: colClassName},
						controls.AttributeControl(__spread({},  this.props, {value: value, layout: 'radio', onValueChange: this._handleValueChange}))
					)
				)
				
//				<div className="row">
//					<div className={colClassName}>
//						<controls.AttributeControl value={value} {...this.props} layout="radio" onValueChange={this._handleValueChange} />
//					</div>
//				</div>
			);
		},
		_handleValueChange: function(event) {console.log('_handleValueChange AttributeFilter', event);
			var query = this._toQuery({attr: this.props.attr.name, value: event.value});
			this.props.onQueryChange({attr: this.props.attr.name, query: query});
		}
	});
	

	/**
	 * Attribute filter with clear control
	 */
	var Filter = React.createClass({
		displayName: 'Filter',
		getInitialState: function() {
			return {query: this.props.query};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({query: this.props.query});
		},
		render: function() {console.log('render Filter', this.state, this.props);
			var query = this.state.query;
			
			var filter;
			var attr = this.props.attr;
			switch(this.props.attr.fieldType) {
				case 'BOOL':
					var cols = 9; // align value filters with composed filters
//					filter = <ValueFilter cols={cols} {...this.props} query={query} onQueryChange={this._handleOnQueryChange} />;
					filter = ValueFilter(__spread({},  this.props, {cols: cols, query: query, onQueryChange: this._handleOnQueryChange}));
					break;
				case 'CATEGORICAL':
				case 'XREF':
				case 'ENUM':
					var multiple = true;
					var cols = 9; // align value filters with composed filters
//					filter = <ValueFilter cols={cols} {...this.props} multiple={multiple} query={query} onQueryChange={this._handleOnQueryChange} />;
					filter = ValueFilter(__spread({},  this.props, {cols: cols, multiple: multiple, query: query, onQueryChange: this._handleOnQueryChange}));
					break;
				case 'EMAIL':
				case 'HTML':
				case 'HYPERLINK':
				case 'MREF':
				case 'SCRIPT':
				case 'STRING':
				case 'TEXT':
				case 'DATE':
				case 'DATE_TIME':
				case 'DECIMAL':
				case 'INT':
				case 'LONG':
//					filter = <ComposedFilter {...this.props} query={query} onQueryChange={this._handleOnQueryChange} />;
					filter = ComposedFilter(__spread({},  this.props, {query: query, onQueryChange: this._handleOnQueryChange}));
					break;
				case 'COMPOUND' :
				case 'FILE':
				case 'IMAGE':
					throw 'Unsupported data type: ' + attr.fieldType;
				default:
					throw 'Unknown data type: ' + attr.fieldType;
			}
			
			var clearBtnClasses = 'btn btn-warning' + (query === null ? ' hidden' : '');
			return (
				div({className: 'row'},
					div({className: 'col-md-10'},
						filter
					),
					div({className: 'col-md-2'},
						button({className: clearBtnClasses, type: 'button', title: 'Reset this filter', onClick: this._handleOnClearFilterClick},
							span({className: 'glyphicon glyphicon-remove'})
						)
					)
				)
				
//				<div className="row">
//					<div className="col-md-10">
//						{filter}
//					</div>
//					<div className="col-md-2">
//						<button className={clearBtnClasses} type="button" title="Reset this filter" onClick={this._handleOnClearFilterClick}>
//							<span className="glyphicon glyphicon-remove"></span>
//						</button>
//					</div>
//				</div>
		    );
		},
		_handleOnQueryChange: function(event) {console.log('_handleOnQueryChange Filter', event);
			this.setState({query: event.query});
			this.props.onQueryChange(event);
		},
		_handleOnClearFilterClick: function(event) {console.log('_handleOnClearFilterClick Filter', event);
			this.setState({query: null});
			this._handleOnQueryChange({attr: this.props.attr.name, query: null});
		}
	});

	var FilterGroup = React.createClass({
		displayName: 'FilterGroup',
		getInitialState: function() {
			return {query: this.props.query, queries: {}}; // FIXME split query in queries
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({query: this.props.query}); // FIXME split query in queries
		},
		render: function() {console.log('render FilterGroup', this.state, this.props);
			var self = this;
			var filters = $.map(self.props.attrs, function(attr, index) {console.log(attr, index);
				var query = self.state.queries[attr.name] || null;
				return (
						div({className: 'form-group'},
							label({className: 'col-md-4 control-label'}, attr.label),
							div({className: 'col-md-8'},
								Filter({attr: attr, query: query, onQueryChange: self._handleOnQueryChange})
							)
						)
//					<div className="form-group">
//						<label for="something" className="col-md-4 control-label">{attr.label}</label>
//						<div className="col-md-8">
//							<Filter attr={attr} query={query} onQueryChange={self._handleOnQueryChange} />
//						</div>
//					</div>
				); // FIXME label-for
			});
//			return (<div className="form-horizontal">{filters}</div>);
			return div({className: 'form-horizontal'}, filters);
		},
		_handleOnQueryChange: function(event) {console.log('_handleOnQueryChange FilterGroup', event);
			this.state.queries[event.attr] = event.query;
			this.setState({queries: this.state.queries});
// FIXME boxes not ticked
			var rules = $.map(this.state.queries, function(query) {
				return [query, {operator: 'AND'}];
			});
			// FIXME remove trailing operator
			// FIXME return query instead of array of query rules
			this.props.onQueryChange({query: rules});
		}
	});
	
										
	molgenis.filters.create = function(attr, props, $container) {
		props.attr = attr; // FIXME
		props.query = props.query || null;
		
//		React.render(<Filter {...props} />, $container[0]);
		React.render(Filter(props), $container[0]);
	};

	molgenis.filters.createGroup = function(attrs, props, $container) {
		props.attrs = attrs; // FIXME
		props.query = props.query || null;
		
//		React.render(<FilterGroup {...props} />, $container[0]);
		React.render(FilterGroup(props), $container[0]);
	};
}($, window.top.molgenis = window.top.molgenis || {}));
