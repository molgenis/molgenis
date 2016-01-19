/**
 * @module AggregateTableHeader
 */

import React from 'react';
import _ from 'underscore';

var thead = React.DOM.thead;

var AggregateTableHeader = React.createClass({
	displayName : 'AggregateTableHeader',
	propTypes : {
		labels : React.PropTypes.array.isRequired,
		attr : React.PropTypes.object
	},
	render : function() {
		var HeaderCells = [];
		HeaderCells.push(this._createHeaderFirst());
		if (this.props.attr) {
			for (var i = 0; i < this.props.labels.length; ++i) {
				var label = this._toLabel(this.props.labels[i]);
				HeaderCells.push(this._createHeader(label, i));
			}
		}
		HeaderCells.push(this._createHeaderLast());

		return (thead(null, tr(null, HeaderCells)));
	},
	_createHeaderFirst : function() {
		return (td({
			style : {
				width : '18%'
			},
			key : 'first'
		}));
	},
	_createHeader : function(label, i) {
		return (th({
			key : 'c' + i
		}, div({
			className : 'text-center'
		}, label !== null ? label : 'N/A' // FIXME i18N
		// missingTemplate({})
		// FIXME label can be
		// object
		)));
	},
	_createHeaderLast : function() {
		return (th({
			key : 'last'
		}, div({
			className : 'text-center'
		}, 'Total')));
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

export default React.createFactory(AggregateTableHeader);
