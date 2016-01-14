/**
 * @module TableHeaderCell
 */

import React from 'react';
import _ from 'underscore';

import Popover from './Popover';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';
import AttrUtilsMixin from './mixin/AttrUtilsMixin';

var th = React.DOM.th, span = React.DOM.span;

/**
 * @memberOf TableHeaderCell
 */
var TableHeaderCell = React.createClass({
	mixins : [ DeepPureRenderMixin, AttrUtilsMixin ],
	displayName : 'TableHeaderCell',
	propTypes : {
		attr : React.PropTypes.object.isRequired,
		path : React.PropTypes.array.isRequired,
		canSort : React.PropTypes.bool,
		sortOrder : React.PropTypes.oneOf([ 'asc', 'desc' ]),
		onSort : React.PropTypes.func,
		className : React.PropTypes.string
	},
	getDefaultProps : function() {
		return {
			sortOrder : null,
			onSort : function() {
			},
		};
	},
	render : function() {
		var SortIcon = this.props.sortOrder !== null ? molgenis.ui.Icon({
			style : {
				marginLeft : 5
			},
			name : this.props.sortOrder === 'asc' ? 'sort-by-alphabet' : 'sort-by-alphabet-alt'
		}) : null;

		var Label = this.props.attr.description ? span(null, Popover({
			value : this.props.attr.label,
			popoverValue : this.props.attr.description
		})) : this.props.attr.label;

		return (th({
			className : this.props.className
		}, this.props.canSort ? span({
			style : {
				cursor : 'pointer'
			},
			onClick : this._handleSort
		}, Label, SortIcon) : Label));
	},
	_handleSort : function(e) {
		this.props.onSort({
			attr : this.props.attr,
			path : this.props.path,
			order : this.props.sortOrder === null ? 'asc' : (this.props.sortOrder === 'asc' ? 'desc' : 'asc')
		});
	}
});

export default React.createFactory(TableHeaderCell);
