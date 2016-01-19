/**
 * @module TableBody
 */

import React from 'react';
import _ from 'underscore';
import molgenis from '../MolgenisQuery';

import EntityDeleteBtn from './EntityDeleteBtn';
import EntityInspectBtn from './EntityInspectBtn';
import EntityEditBtn from './EntityEditBtn';

import DeepPureRenderMixin from './mixin/DeepPureRenderMixin';
import AttrUtilsMixin from './mixin/AttrUtilsMixin';
import TableCellFactory from './TableCell';

var tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td;

/**
 * @memberOf TableBody
 */
var TableBody = React.createClass({
	mixins : [ DeepPureRenderMixin, AttrUtilsMixin ],
	displayName : 'TableBody',
	propTypes : {
		data : React.PropTypes.object.isRequired,
		attrs : React.PropTypes.object.isRequired,
		enableEdit : React.PropTypes.bool,
		enableDelete : React.PropTypes.bool,
		enableInspect : React.PropTypes.bool,
		onEdit : React.PropTypes.func,
		onDelete : React.PropTypes.func,
		onRowInspect : React.PropTypes.func,
		onRowClick : React.PropTypes.func
	},
	getDefaultProps : function() {
		return {
			onEdit : function() {
			},
			onDelete : function() {
			},
			onRowInspect : function() {
			},
			onRowClick : function() {
			}
		};
	},
	render : function() {
		return tbody(null, this._createRows(this.props.data.meta));
	},
	_createRows : function(entity) {
		var Rows = [];

		for (var i = 0; i < this.props.data.items.length; ++i) {
			var item = this.props.data.items[i];

			Rows.push(tr({
				key : '' + i,
				onClick : this.props.onRowClick !== null ? this.props.onRowClick.bind(null, item) : null
			}, this._createCols(item, entity)));
		}
		return Rows;
	},
	_createCols : function(item, entity) {
		var Cols = [];
		if (this.props.enableEdit === true) {
			var entityEditBtn = EntityEditBtn({
				name : entity.name,
				id : item[entity.idAttribute],
				onEdit : this.props.onEdit
			});
			Cols.push(td({
				className : 'compact',
				key : 'edit'
			}, entityEditBtn));
		}
		if (this.props.enableDelete === true) {
			var entityDeleteBtn = EntityDeleteBtn({
				name : entity.name,
				id : item[entity.idAttribute],
				onDelete : this.props.onDelete
			});
			Cols.push(td({
				className : 'compact',
				key : 'delete'
			}, entityDeleteBtn));
		}
		if (this.props.enableInspect === true && this.props.onRowInspect !== null) {
			var entityInspectBtn = EntityInspectBtn({
				name : entity.name,
				id : item[entity.idAttribute],
				onInspect : this.props.onRowInspect
			});
			Cols.push(td({
				className : 'compact',
				key : 'inspect'
			}, entityInspectBtn));
		}
		this._createColsRec(item, entity, entity.attributes, this.props.attrs, Cols, [], false, undefined);
		return Cols;
	},
	_createColsRec : function(item, entity, attrs, selectedAttrs, Cols, path, expanded, parentAttr) {
		if (_.size(selectedAttrs) > 0) {
			for (var j = 0; j < attrs.length; ++j) {
				var attr = attrs[j];
				if (this._isSelectedAttr(attr, selectedAttrs)) {
					if (attr.visible === true) {
						var attrPath = path.concat(attr.name);
						if (molgenis.isCompoundAttr(attr)) {
							this._createColsRec(item, entity, attr.attributes, {
								'*' : null
							}, Cols, path, expanded, parentAttr);
						} else {
							if (this._isExpandedAttr(attr, selectedAttrs)) {
								Cols.push(td({
									className : 'expanded-left',
									key : attrPath.join()
								}));
								var value = (item !== undefined && item !== null) ? (_.isArray(item) ? _.map(item, function(value) {
									return value[attr.name];
								}) : item[attr.name]) : null;
								this._createColsRec(value, attr.refEntity, attr.refEntity.attributes, selectedAttrs[attr.name], Cols, attrPath, true, attr);
							} else {
								if (this._canExpandAttr(attr, path)) {
									Cols.push(td({
										key : 'e' + attrPath.join()
									}));
								}
								var value = (item !== undefined && item !== null) ? (_.isArray(item) ? _.map(item, function(value) {
									return value[attr.name];
								}) : item[attr.name]) : null;
								var TableCell = TableCellFactory({
									className : j === attrs.length - 1 && expanded ? 'expanded-right' : undefined,
									entity : entity,
									attr : attr,
									value : value,
									expanded : expanded,
									onEdit : this.props.onEdit,
									key : attrPath.join(),
									parentAttr : parentAttr
								});
								Cols.push(TableCell);
							}
						}
					}
				}
			}
		} else {
			// add an empty non-compacted column so that compact column stay
			// compact
			Cols.push(td({
				key : 'dummy'
			}));
		}
	}
});

export default React.createFactory(TableBody);
