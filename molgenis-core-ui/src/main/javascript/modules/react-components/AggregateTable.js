/**
 * @module AggregateTable
 */

import React from 'react';
import _ from 'underscore';

import AggregateTableHeader from './AggregateTableHeader';
import AggregateTableBody from './AggregateTableBody';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';

var div = React.DOM.div, table = React.DOM.table, span = React.DOM.span;
import api from '../RestClientV2';

var AGGREGATE_ANONYMIZATION_VALUE = -1;

/**
 * @memberOf component
 */
var AggregateTable = React.createClass({
	mixins : [ DeepPureRenderMixin ],
	displayName : 'Table',
	propTypes : {
		entity : React.PropTypes.string.isRequired,
		x : React.PropTypes.string,
		y : React.PropTypes.string,
		distinct : React.PropTypes.string,
		query : React.PropTypes.array
	},
	getInitialState : function() {
		return {
			data : null
		};
	},
	componentDidMount : function() {
		this._refreshData(this.props, this.state);
	},
	componentWillReceiveProps : function(nextProps) {
		// reset pager on query change
		var nextState = _.extend({}, this.state, {
			attrs : nextProps.attrs
		});
		if (JSON.stringify(this.props.query) !== JSON.stringify(nextProps.query)) {
			_.extend(nextState, {
				start : 0
			});
		}
		this._refreshData(nextProps, nextState);
	},
	render : function() {
		if (this.state.data === null) {
			return molgenis.ui.Spinner(); // entity not available yet
		}

		if (this.state.data.aggs.matrix.length == 0) {
			return div(null, span(null, "No results found"));
		}

		var className = 'table table-striped';
		var aggregateTableHeader = AggregateTableHeader({
			labels : this.state.data.aggs.yLabels,
			attr : this.state.data.yAttr
		});

		var aggregateTableBody = AggregateTableBody({
			labels : this.state.data.aggs.xLabels,
			attr : this.state.data.xAttr,
			dimension : this.state.data.aggs.yLabels.length === 0 ? 1 : 2,
			matrix : this.state.data.aggs.matrix,
			threshold : this.state.data.aggs.threshold
		});

		return (div(null, table({
			className : className
		}, aggregateTableHeader, aggregateTableBody)));
	},
	_refreshData : function(props, state) {
		var opts = {
			aggs : {
				x : props.x,
				y : props.y,
				distinct : props.distinct
			},
			q : props.query
		};

		api.get(props.entity, opts).done(function(data) {
			var newState = _.extend({}, state, {
				data : data
			});
			if (this.isMounted()) {
				this.setState(newState);
			}
		}.bind(this));
	}
});

export default React.createFactory(AggregateTable);