/**
 * @module Table
 */

import React from 'react';
import _ from 'underscore';

import api from '../RestClientV2';
import Button from './Button';
import Modal from './Modal';
import TableHeader from './TableHeader';
import TableBody from './TableBody';
import EntityInspectBtn from './EntityInspectBtn';
import Spinner from './Spinner';
import Pager from './Pager';
import SelectBox from './SelectBox';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';
import AttrUtilsMixin from './mixin/AttrUtilsMixin';

var div = React.DOM.div, table = React.DOM.table, span = React.DOM.span, em = React.DOM.em, label = React.DOM.label;

/**
 * @memberOf Table
 */
var Table = React.createClass({
	mixins : [ DeepPureRenderMixin, AttrUtilsMixin ],
	displayName : 'Table',
	propTypes : {
		entity : React.PropTypes.string.isRequired,
		attrs : React.PropTypes.object,
		query : React.PropTypes.object,
		sort : React.PropTypes.object,
		maxRows : React.PropTypes.number,
		onRowAdd : React.PropTypes.func,
		onRowEdit : React.PropTypes.func,
		onRowDelete : React.PropTypes.func,
		onRowInspect : React.PropTypes.func,
		onRowClick : React.PropTypes.func,
		enableAdd : React.PropTypes.bool,
		enableEdit : React.PropTypes.bool,
		enableDelete : React.PropTypes.bool,
		enableInspect : React.PropTypes.bool,
		onSort : React.PropTypes.func
	},
	getInitialState : function() {
		return {
			data : null,
			attrs : this.props.attrs,
			sort : this.props.sort,
			start : 0,
			maxRows : this.props.maxRows
		};
	},
	getDefaultProps : function() {
		return {
			attrs : {
				'*' : null
			},
			maxRows : 20,
			onRowAdd : function() {
			},
			onRowEdit : function() {
			},
			onRowDelete : function() {
			},
			onRowInspect : null,
			enableAdd : true,
			enableEdit : true,
			enableDelete : true,
			enableInspect : true,
			onRowClick : null,
			onSort : function() {
			}
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
			return Spinner(); // entity not available yet
		}

		var writable = this.state.data.meta.writable;

		var tableHeader = TableHeader({
			entity : this.state.data.meta,
			attrs : this.state.attrs,
			sort : this.state.sort,
			enableAdd : writable && this.props.enableAdd === true,
			enableEdit : writable && this.props.enableEdit === true,
			enableDelete : writable && this.props.enableDelete === true,
			enableInspect : this.props.enableInspect === true && this.props.onRowInspect !== null,
			onSort : this._handleSort,
			onExpand : this._handleExpand,
			onCollapse : this._handleCollapse,
			onCreate : this._handleCreate
		});

		var tableBody = TableBody({
			data : this.state.data,
			attrs : this.state.attrs,
			enableEdit : writable && this.props.enableEdit === true,
			enableDelete : writable && this.props.enableDelete === true,
			enableInspect : this.props.enableInspect === true && this.props.onRowInspect !== null,
			onEdit : this._handleEdit,
			onDelete : this._handleDelete,
			onRowInspect : this.props.onRowInspect,
			onRowClick : this.props.onRowClick
		});

		var className = 'table table-striped table-condensed table-bordered molgenis-table';

		if (this.props.onRowClick !== null)
			className = className + " table-hover";

		return (div(null, div({
			className : 'molgenis-table-container'
		}, table({
			className : className
		}, tableHeader, tableBody)), div({
			className : 'row'
		}, div({
			className : 'col-md-3 form-inline'
		}, div({
			'className' : 'form-group'
		}, label(null, "Rows per page: " + String.fromCharCode(160)), SelectBox({
			options : [ {
				value : 20,
				text : 20
			}, {
				value : 30,
				text : 30
			}, {
				value : 50,
				text : 50
			}, {
				value : 100,
				text : 100
			} ],
			onChange : this._handleRowsPerPageChange
		}))), div({
			className : 'col-md-6'
		}, div({
			className : 'text-center'
		}, Pager({
			nrItems : this.state.data.total,
			nrItemsPerPage : this.state.maxRows,
			start : this.state.data.start,
			onPageChange : this._handlePageChange
		}))), div({
			className : 'col-md-3'
		}, span({
			className : 'pull-right'
		}, em(null, this.state.data.total + ' item' + (this.state.data.total !== 1 ? 's' : '') + ' found'))))));
	},
	_refreshData : function(props, state) {
		var opts = {
			attrs : {
				'~id' : null
			}, // always include the id attribute
			num : state.maxRows
		};

		// add selected attrs
		if (state.attrs && _.size(state.attrs) > 0) {
			_.extend(opts.attrs, state.attrs);
		}

		if (props.query) {
			opts.q = props.query.q;
		}
		if (state.sort) {
			opts.sort = {
				'orders' : [ {
					'attr' : state.sort.attr.name,
					'direction' : state.sort.order
				} ]
			};
		}
		if (state.start !== 0) {
			opts.start = state.start;
		}
		api.get(props.entity, opts).done(function(data) {
			var newState = _.extend({}, state, {
				data : data
			});
			if (this.isMounted()) {
				this.setState(newState);
			}
		}.bind(this));
	},
	_handleExpand : function(e) {
		var attrs = JSON.parse(JSON.stringify(this.state.attrs)); // deep
		// clone

		for (var i = 0, attrsAtDepth = attrs; i < e.attrPath.length; ++i) {
			var attr = e.attrPath[i];
			if (!attrsAtDepth[attr]) {
				attrsAtDepth[attr] = (i < e.attrPath.length - 1) ? {} : {
					'*' : null
				};
			}
			attrsAtDepth = attrsAtDepth[attr];
		}

		this._refreshData(this.props, _.extend({}, this.state, {
			attrs : attrs
		}));
	},
	_handleCollapse : function(e) {
		var attrs = _.extend({}, this.state.attrs);

		for (var i = 0, attrsAtDepth = attrs; i < e.attrPath.length; ++i) {
			var attr = e.attrPath[i];
			if (i < e.attrPath.length - 1) {
				attrsAtDepth = attrsAtDepth[attr];
			} else {
				attrsAtDepth[attr] = null;
			}
		}

		this._refreshData(this.props, _.extend({}, this.state, {
			attrs : attrs
		}));
	},
	_handleCreate : function() {
		this._resetTable();
		this.props.onRowAdd();
	},
	_handleEdit : function() {
		this._resetTable();
		this.props.onRowEdit();
	},
	_handleDelete : function() {
		this._resetTable();
		this.props.onRowDelete();
	},
	_resetTable : function() {
		this._refreshData(this.props, _.extend({}, this.state, {
			start : 0
		}));
	},
	_handleSort : function(e) {
		this._refreshData(this.props, _.extend({}, this.state, {
			sort : e
		}));
		this.props.onSort(e);
	},
	_handlePageChange : function(e) {
		this._refreshData(this.props, _.extend({}, this.state, {
			start : e.start
		}));
	},
	_handleRowsPerPageChange : function(e) {
		this._refreshData(this.props, _.extend({}, this.state, {
			start : 0,
			maxRows : parseInt(e.target.value)
		}));
	}
});

export default React.createFactory(Table)