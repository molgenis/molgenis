define(function(require, exports, module) {
	/**
	 * @module AggregateTable
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');

	var AggregateTableHeader = require('component/AggregateTableHeader');
	var AggregateTableBody = require('component/AggregateTableBody');

	var tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, label = React.DOM.label;

	var AggregateTableBody = React.createClass({
		displayName : 'AggregateTableBody',
		propTypes : {
			labels : React.PropTypes.array.isRequired,
			attr : React.PropTypes.object.isRequired,
			matrix : React.PropTypes.array.isRequired,
			dimension : React.PropTypes.number,
			threshold : React.PropTypes.number,
		},
		render : function() {
			var Rows = [];
			for (var i = 0; i < this.props.matrix.length; ++i) {
				Rows.push(this._createRow(this.props.labels[i], this.props.matrix[i], i));
			}

			// Row containing total column counts
			Rows.push(this._createRowLast());

			return (tbody(null, Rows));
		},
		_createRow : function(label, rowData, rowIndex) {
			var Cells = [];
			Cells.push(th({
				key : 'c' + rowIndex + '-first'
			}, this._toLabel(label)));

			if (this.props.dimension > 1) {
				var count = 0;
				var isAnonymized = false;
				for (var i = 0; i < rowData.length; ++i) {
					var cellCount = rowData[i];
					if (cellCount === AGGREGATE_ANONYMIZATION_VALUE) {
						cellCount = '\u2264' + this.props.threshold; // lesser
																		// than
																		// or
																		// equal
					}
					var Cell = (td({
						key : 'c' + rowIndex + '-' + i
					}, div({
						className : 'text-center'
					}, cellCount)));
					Cells.push(Cell);
				}
			}
			Cells.push(this._createColLast(rowData, 'c' + rowIndex + '-last'));

			return (tr({
				key : 'r' + rowIndex
			}, Cells));
		},
		_createColLast : function(rowData, key) {
			var total = 0;
			var isAnonymized = false;
			for (var i = 0; i < rowData.length; ++i) {
				var cellCount = rowData[i];
				if (cellCount === AGGREGATE_ANONYMIZATION_VALUE) {
					total += this.props.threshold;
					isAnonymized = true;
				} else {
					total += cellCount;
				}
			}
			return (td({
				key : key
			}, div({
				className : 'text-center'
			}, isAnonymized ? '\u2264' + total : total)));
		},
		_createRowLast : function() {
			var nrCols = this.props.matrix[0].length;

			var tableTotal = 0;
			var isTableAnonymized = false;

			var Cells = [];
			Cells.push(th({
				key : 'c-last-first'
			}, 'Total'));
			for (var col = 0; col < nrCols; ++col) {
				var colTotal = 0;
				var isColAnonymized = false;
				for (var row = 0; row < this.props.matrix.length; ++row) {
					var cellCount = this.props.matrix[row][col];
					if (cellCount === AGGREGATE_ANONYMIZATION_VALUE) {
						colTotal += this.props.threshold;
						isColAnonymized = true;
					} else {
						colTotal += cellCount;
					}
				}

				if (this.props.dimension > 1) {
					var Cell = (td({
						key : 'r-last-' + col
					}, div({
						className : 'text-center'
					}, isColAnonymized ? '\u2264' + colTotal : colTotal)));
					Cells.push(Cell);
				}

				tableTotal += colTotal;
				isTableAnonymized = isTableAnonymized | isColAnonymized;
			}

			// Cell containing total of totals
			var TableTotalCell = (td({
				key : 'c-last-last'
			}, div({
				className : 'text-center'
			}, isTableAnonymized ? '\u2264' + tableTotal : tableTotal)));
			Cells.push(TableTotalCell);

			return (tr({
				key : 'r-last'
			}, Cells));
		},
		_toLabel : function(label) {
			if (label === null) {
				return 'N/A';
			} else if (this.props.attr.refEntity) {
				return label[this.props.attr.refEntity.labelAttribute];
			} else {
				return label;
			}
		}
	});
	module.exports = React.createFactory(AggregateTableBody);
});