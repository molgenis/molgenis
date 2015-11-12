/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";

	var div = React.DOM.div, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, a = React.DOM.a, span = React.DOM.span, em = React.DOM.em, br = React.DOM.br, label = React.DOM.label;

	var api = new molgenis.RestClientV2();

	var AGGREGATE_ANONYMIZATION_VALUE = -1;
	
	/**
	 * @memberOf component
	 */
	var AggregateTable = React.createClass({ // FIXME add no aggregates message + i18n
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Table',
		propTypes: {
			entity: React.PropTypes.string.isRequired,
			x: React.PropTypes.string,
			y: React.PropTypes.string,
			distinct: React.PropTypes.string,
			query: React.PropTypes.array
		},
		getInitialState: function() {
			return {
				data: null
			};
		},
		componentDidMount: function() {
			this._refreshData(this.props, this.state);
		},
		componentWillReceiveProps : function(nextProps) {
			// reset pager on query change
			var nextState = _.extend({}, this.state, {attrs: nextProps.attrs});
			if(JSON.stringify(this.props.query) !== JSON.stringify(nextProps.query)) {
				_.extend(nextState, {start: 0});
			}
			this._refreshData(nextProps, nextState);
		},
		render: function() {
			if(this.state.data === null) {
				return molgenis.ui.Spinner(); // entity not available yet
			}

			var className = 'table table-striped';
			var AggregateTableHeader = AggregateTableHeaderFactory({
				labels: this.state.data.aggs.yLabels
			});
			
			var AggregateTableBody = AggregateTableBodyFactory({
				labels: this.state.data.aggs.xLabels,
				dimension: this.state.data.aggs.yLabels.length === 0 ? 1 : 2,
				matrix: this.state.data.aggs.matrix,
				threshold: this.state.data.aggs.threshold
			});
			
			return (
				div(null,
					table({className: className},
						AggregateTableHeader,
						AggregateTableBody
					)
				)
			);
		},
		_refreshData: function(props, state) {
			var opts = {
				aggs: {
					x: props.x,
					y: props.y,
					distinct: props.distinct
				},
				q: props.q
			};
			api.get(props.entity, opts).done(function(data) {
				var newState = _.extend({}, state, {data: data});
				this.setState(newState);
			}.bind(this));
		}
	});

	var AggregateTableHeader = React.createClass({
		displayName: 'AggregateTableHeader',
		propTypes: {
			labels: React.PropTypes.array.isRequired
		},
		render: function() {
			var HeaderCells = [];
			HeaderCells.push(this._createHeaderFirst());
			for(var i = 0; i < this.props.labels.length; ++i) {
				HeaderCells.push(this._createHeader(this.props.labels[i], i));
			}
			HeaderCells.push(this._createHeaderLast());
			
			return (
				thead(null,
					tr(null,
						HeaderCells
					)
				)
			);
		},
		_createHeaderFirst: function() {
			return (
				td({style: {width: '18%'}, key: 'first'})
			);
		},
		_createHeader: function(label, i) {
			return (
				th({key: 'c' + i},
					div({className: 'text-center'},
						label !== null ? label : 'N/A' // FIXME i18N missingTemplate({}) FIXME label can be object
					)
				)
			);
		},
		_createHeaderLast: function() {
			return (
				th({key: 'last'},
					div({className: 'text-center'},
						'Total' // FIXME i18n totalTemplate({})
					)
				)
			);
		}
	});
	var AggregateTableHeaderFactory = React.createFactory(AggregateTableHeader);
	
	var AggregateTableBody = React.createClass({
		displayName: 'AggregateTableBody',
		propTypes: {
			labels: React.PropTypes.array.isRequired,
			matrix: React.PropTypes.array.isRequired,
			dimension: React.PropTypes.number,
			threshold: React.PropTypes.number,
		},
		render: function() {
			var Rows = [];
			for(var i = 0; i < this.props.matrix.length; ++i) {
				Rows.push(this._createRow(this.props.labels[i], this.props.matrix[i], i));
			}
			
			// Row containing total column counts 
			Rows.push(this._createRowLast());
			
			return (
				tbody(null,
					Rows
				)
			);
		},
		_createRow: function(label, rowData, rowIndex) {
			var Cells = [];
			Cells.push(th({key: 'c' + rowIndex + '-first'}, label !== null ? label : 'N/A')); // FIXME i18n FIXME label can be object
			
			if(this.props.dimension > 1) {
				var count = 0;
				var isAnonymized = false;
				for(var i = 0; i < rowData.length; ++i) {
					var cellCount = rowData[i];
					if(cellCount === AGGREGATE_ANONYMIZATION_VALUE) {
						cellCount = '\u2264' + this.props.threshold; // lesser than or equal
					}
					var Cell = (
						td({key: 'c' + rowIndex + '-' + i},
							div({className: 'text-center'},
								cellCount
							)
						)
					);
					Cells.push(Cell);
				}
			}
			Cells.push(this._createColLast(rowData, 'c' + rowIndex + '-last'));
			
			return (
				tr({key: 'r' + rowIndex}, Cells)
			);
		},
		_createColLast: function(rowData, key) {
			var total = 0;
			var isAnonymized = false;
			for(var i = 0; i < rowData.length; ++i) {
				var cellCount = rowData[i];
				if(cellCount === AGGREGATE_ANONYMIZATION_VALUE) {
					total += this.props.threshold;
					isAnonymized = true;
				} else {
					total += cellCount;
				}
			}
			return (
				td({key: key},
					div({className: 'text-center'},
						isAnonymized ? '\u2264' + total : total
					)
				)
			);
		},
		_createRowLast: function() {
			var nrCols = this.props.matrix[0].length;
			
			var tableTotal = 0;
			var isTableAnonymized = false;
			
			var Cells = [];
			Cells.push(th({key: 'c-last-first'}, 'Total')); // FIXME i18n
			for(var col = 0; col < nrCols; ++col) {
				var colTotal = 0;
				var isColAnonymized = false;
				for(var row = 0; row < this.props.matrix.length; ++row) {
					var cellCount = this.props.matrix[row][col];
					if(cellCount === AGGREGATE_ANONYMIZATION_VALUE) {
						colTotal += this.props.threshold;
						isColAnonymized = true;
					} else {
						colTotal += cellCount;
					}
				}
				
				if(this.props.dimension > 1) {
					var Cell = (
						td({key: 'r-last-' + col},
							div({className: 'text-center'},
								isColAnonymized ? '\u2264' + colTotal : colTotal
							)
						)
					);
					Cells.push(Cell);
				}
				
				tableTotal += colTotal;
				isTableAnonymized = isTableAnonymized | isColAnonymized;
			}
			
			
			// Cell containing total of totals
			var TableTotalCell = (
				td({key: 'c-last-last'},
					div({className: 'text-center'},
							isTableAnonymized ? '\u2264' + tableTotal : tableTotal
					)
				)
			);
			Cells.push(TableTotalCell);
			
			return (
				tr({key: 'r-last'}, Cells)
			);
		}
	});
	var AggregateTableBodyFactory = React.createFactory(AggregateTableBody);
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		AggregateTable: React.createFactory(AggregateTable)
	});
}(_, React, molgenis));