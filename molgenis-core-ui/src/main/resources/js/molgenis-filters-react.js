/** @jsx React.DOM */
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
	 * Attribute filter with clear control
	 */
	var Filter = React.createClass({
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
					var cols = 9;
					filter = <ValueFilter cols={cols} {...this.props} query={query} onQueryChange={this._handleOnQueryChange} />;
					break;
				case 'CATEGORICAL':
					var multiple = true;
					var cols = 9;
					filter = <ValueFilter cols={cols} {...this.props} multiple={multiple} query={query} onQueryChange={this._handleOnQueryChange} />;
					break;
				case 'XREF':
				case 'EMAIL':
				case 'ENUM':
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
					filter = <ComposedFilter {...this.props} query={query} onQueryChange={this._handleOnQueryChange} />;
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
				<div className="row">
					<div className="col-md-10">
						{filter}
					</div>
					<div className="col-md-2">
						<button className={clearBtnClasses} type="button" onClick={this._handleOnClearFilterClick}>
							<span className="glyphicon glyphicon-remove"></span>
						</button>
					</div>
				</div>
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

	var ComposedFilter = React.createClass({
		getInitialState: function() {
			return {filters: this._toFilters(this.props.query)};
		},
		componentWillReceiveProps : function(nextProps) {
			this.setState({filters: this._toFilters(nextProps.query)});
		},
		render: function() {console.log('render Filter', this.state, this.props);
			var self = this;
			var filterParts = $.map(this.state.filters, function(query, index) {
				return <ComposedFilterPart {...self.props} query={query} index={index} key={'part-' + index}
							hideAddBtn={self.props.query === null}
							hideRemoveBtn={self.props.query === null || self.state.filters.length == 1}
							onAddFilterPart={self._handleAddFilterPart}
							onRemoveFilterPart={self._handleRemoveFilterPart}
							onFilterPartQueryChange={self._handleOnFilterPartQueryChange} />;
			});
			return (<div>{filterParts}</div>);
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
			this.setState({filters: filters});
			
			if(removed[removed.length - 1] !== null) {
				this.props.onQueryChange({
					attr: event.attr,
					query: this._toQuery(filters)
				});
			}
		},
		_toFilters: function(query) {
			var filters = [];
			if(query) {
				if(query.operator === 'NESTED') {
					for(var i = 0; i < query.nestedRules.length; ++i) {
						if(i > 0) {
							filters.push({operator: 'OR'}); // FIXME can be and for mref
						}
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
				for(var i = 0; i < filters.length; i +=2) { // FIXME query on two inputs, first empty, second filled, extra OR clause
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
	
	var ComposedFilterPart = React.createClass({
		render: function() {console.log('render ComposedFilterPart', this.state, this.props);
			if(this.props.query && this.props.query.operator === 'OR') { // FIXME not elegant
				return (
						<div className="form-group">
							<div className="col-md-offset-3 col-md-3">
								<div className="text-center">OR</div>
							</div>
						</div>
				    );
			}

			var addBtnClasses = 'btn btn-default' + (this.props.hideAddBtn ? ' hidden' : '');
			var removeBtnClasses = 'btn btn-default' + (this.props.hideRemoveBtn ? ' hidden' : '');			
			var filter = <ValueFilter {...this.props} query={this.props.query} onQueryChange={this._handleOnQueryChange} />;
					
			return (
				<div className="row">
					<div className="col-md-9">
						{filter}
					</div>
					<div className="col-md-3">
						<button className={addBtnClasses} type="button" onClick={this._handleAddFilterPartClick}>
							<span className="glyphicon glyphicon-plus"></span>
						</button>
						<button className={removeBtnClasses} type="button" onClick={this._handleRemoveFilterPartClick}>
							<span className="glyphicon glyphicon-minus"></span>
						</button>
					</div>
				</div>
		    );
		},
		_handleOnQueryChange: function(event) {console.log('_handleOnQueryChange ComposedFilterPart', event);
			this.props.onFilterPartQueryChange({index: this.props.index, event: event});
		},
		_handleAddFilterPartClick: function() {console.log('_handleAddFilterPartClick ComposedFilterPart');
			this.props.onAddFilterPart();
		},
		_handleRemoveFilterPartClick: function() {console.log('_handleRemoveFilterPartClick ComposedFilterPart');
			this.props.onRemoveFilterPart({index: this.props.index, attr: this.props.attr.name}); // FIXME add query?
		},
	});
	
	/**
	 * Attribute range filter shared functions
	 */
	var RangeValueFilterMixin = {
		_handleFromQueryChange: function(event) {console.log('_handleFromQueryChange RangeValueFilterMixin', event);
			// merge query with query change
			if(this.props.query) {
				switch(this.props.query.operator) {
					case 'GREATER_EQUAL':
						event.query.operator = 'GREATER_EQUAL';
						break;		
					case 'LESS_EQUAL':
						event.query.operator = 'RANGE';
						event.query.value = [event.query.value, this.props.query.value];
						break;
					case 'RANGE': // FIXME from > to check
						event.query.value = [event.query.value, this.props.query.value];
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
						event.query.value = [this.props.query.value, event.query.value];
						break;	
					case 'LESS_EQUAL':
						event.query.operator = 'LESS_EQUAL';
						break;
					case 'RANGE': // FIXME from > to check
						event.query.value = [this.props.query.value, event.query.value];
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
		render: function() {console.log('render HorizontalRangeValueFilter', this.state, this.props);
			return (
				<div className="form-group">
					<div className="col-md-6">
						<AttributeFilter {...this.props} query={this._extractFromQuery()} onQueryChange={this._handleFromQueryChange} />
					</div>
					<div className="col-md-6">
						<AttributeFilter {...this.props} query={this._extractToQuery()} onQueryChange={this._handleToQueryChange} />
					</div>
				</div>	
		    );
		}
	});
	
	/**
	 * Attribute range filter with a 'to' input below a 'from' input 
	 */
	var VerticalRangeValueFilter = React.createClass({
		mixins: [RangeValueFilterMixin],
		render: function() {console.log('render VerticalRangeValueFilter', this.state, this.props);
			return(
				<div>
					<div className="form-group">
						<div className="col-md-12">
							<AttributeFilter {...this.props} query={this._extractFromQuery()} onQueryChange={this._handleFromQueryChange} />
						</div>
					</div>
					<div className="form-group">
						<div className="col-md-12">
							<AttributeFilter {...this.props} query={this._extractToQuery()} onQueryChange={this._handleToQueryChange} />
						</div>
					</div>
				</div>
			);
		}
	});
	
	/**
	 * Attribute range filter with a slider 
	 */
	var RangeSliderFilter = React.createClass({// FIXME init with value
		mixins: [RangeValueFilterMixin],
		render: function() {console.log('render RangeSliderFilter', this.state, this.props);
			return(
				<div className="form-group">
					<div className="col-md-offset-1 col-md-10">
						<controls.RangeSliderControl range={this.props.attr.range} disabled={this.props.disabled} />
					</div>
				</div>
			); //FIXME handle change event
		}
	});
	
	/**
	 * Attribute RANGE filter with a 'from' and 'to' input
	 */
	var RangeValueFilter = React.createClass({
		render: function() {console.log('render RangeValueFilter', this.state, this.props);
			switch(this.props.attr.fieldType) {
				case 'DECIMAL':
				case 'INT':
				case 'LONG':
					if(this.props.attr.range)
						return (<RangeSliderFilter {...this.props} onQueryChange={this.props.onQueryChange} />); // FIXME value
					else
						return (<HorizontalRangeValueFilter {...this.props} onQueryChange={this.props.onQueryChange} />);
				default:
					return (<VerticalRangeValueFilter {...this.props} onQueryChange={this.props.onQueryChange} />);
			}
		}
	});
	
	/**
	 * Attribute filter
	 */
	var ValueFilter = React.createClass({
		render: function() {console.log('render ValueFilter', this.state, this.props);
			if(this.props.attr.nillable) {
				return (<NillableValueFilter cols={this.props.cols} {...this.props} onQueryChange={this.props.onQueryChange} />);
			} else {
				return (<NonNillableValueFilter cols={this.props.cols} {...this.props} onQueryChange={this.props.onQueryChange} />);
			}
		}
	});
	
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
					return (<RangeValueFilter {...this.props} {...additionalProps} onQueryChange={this.props.onQueryChange} />);
				default:
					return (<AttributeFilter {...this.props} {...additionalProps} onQueryChange={this.props.onQueryChange} />);
			}
		}
	};
	
	/**
	 *  Attribute filter with one required value input and a null select
	 */
	var NillableValueFilter = React.createClass({
		mixins: [ValueQueryMixin, ValueFilterMixin],
		getDefaultProps: function() {
			return {
				cols: 12
			};
		},
		getInitialState: function() {
			return {
				disabled: false
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
				<div>
					{filter}
					<div className="form-group">
						<div className={colClassName}>
							<controls.BoolControl value={checked} label="N/A" onValueChange={this._handleNillableValueChange} />
						</div>
					</div>
				</div>
		    );
		},
		_handleNillableValueChange: function(event) {console.log('_handleNillableValueChange NillableValueFilter', event);
			this.setState({
				disabled: event.value
			});
			event.value = event.value === true ? null : undefined;
			this.props.onQueryChange({attr: this.props.attr.name, query: this._toQuery(event)});
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
		render: function() {console.log('render NonNillableValueFilter', this.state, this.props);
			var filter = this._createValueFilter(this.state);
			return filter;
		}
	});

	var AttributeFilter = React.createClass({
		mixins: [ValueQueryMixin],
		getDefaultProps: function() {
			return {
				cols: 12
			};
		},
		render: function() {console.log('render AttributeFilter', this.state, this.props);
			var value = this._toValue(this.props.query);

			var colClassName = "col-md-" + this.props.cols;
			return (
				<div className="row">
					<div className={colClassName}>
						<controls.AttributeControl value={value} {...this.props} layout="radio" onValueChange={this._handleValueChange} />
					</div>
				</div>
			);
		},
		_handleValueChange: function(event) {console.log('_handleValueChange AttributeFilter', event);
			var query = this._toQuery({attr: this.props.attr.name, value: event.value});
			this.props.onQueryChange({attr: this.props.attr.name, query: query});
		}
	});
									
	molgenis.filters.create = function(attr, props, $container) {
		props.attr = attr; // FIXME
		props.initialQuery = props.query;
		props.query = null;
		
		React.render(<Filter {...props} />, $container[0]);
	};
}($, window.top.molgenis = window.top.molgenis || {}));
