/**
 * @module TableCell
 */

import React from 'react';
import _ from 'underscore';

import TableCellContent from './TableCellContent';

import AttrUtilsMixin from './mixin/AttrUtilsMixin';

var td = React.DOM.td, br = React.DOM.br;

/**
 * @memberOf TableCell
 */
var TableCell = React.createClass({
	displayName : 'TableCell',
	mixins : [ AttrUtilsMixin ],
	propTypes : {
		entity : React.PropTypes.object.isRequired,
		attr : React.PropTypes.object.isRequired,
		value : React.PropTypes.any,
		expanded : React.PropTypes.bool,
		className : React.PropTypes.string,
		onEdit : React.PropTypes.func,
		parentAttr : React.PropTypes.object
	},
	shouldComponentUpdate : function(nextProps, nextState) {
		return !_.isEqual(this.state, nextState) || !_.isEqual(this.props.entity.name, nextProps.entity.name)
				|| !_.isEqual(this.props.attr.name, nextProps.attr.name) || !_.isEqual(this.props.value, nextProps.value);
	},
	render : function() {
		var CellContentBlocks;
		// treat expanded mref differently
		if (this.props.expanded && _.isArray(this.props.value) && this.props.parentAttr.fieldType === "MREF") {
			CellContentBlocks = _.flatten(_.map(this.props.value, function(value, i) {
				if (value !== null && value !== undefined) {
					var CellContentForValue = this._createTableCellContent(value, 'c' + i);
					return i < this.props.value.length - 1 ? [ CellContentForValue, br({
						key : 'b' + i
					}) ] : CellContentForValue;
				} else {
					return br();
				}
			}.bind(this)));
		} else {
			CellContentBlocks = this.props.value !== null && this.props.value !== undefined ? [ this._createTableCellContent(this.props.value) ] : [];
		}

		return td({
			className : this.props.className
		}, CellContentBlocks);
	},
	_createTableCellContent : function(value, key) {
		return TableCellContent({
			entity : this.props.entity,
			attr : this.props.attr,
			value : value,
			className : this.props.className,
			onEdit : this.props.onEdit,
			key : key
		});
	}
});
export default React.createFactory(TableCell);
