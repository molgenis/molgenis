define(function(require, exports, module) {
	/**
	 * @module TableHeader
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');
	var molgenis = require('modules/MolgenisQuery');

	var TableHeaderCell = require('component/TableHeaderCell');
	var EntityCreateBtn = require('component/EntityCreateBtn');
	var EntityExpandBtn = require('component/EntityExpandBtn');
	var EntityCollapseBtn = require('component/EntityCollapseBtn');

	var AttrUtilsMixin = require('component/mixin/AttrUtilsMixin');

	var thead = React.DOM.thead, tr = React.DOM.tr, th = React.DOM.th;

	/**
	 * @memberOf TableHeader
	 */
	var TableHeader = React.createClass({
		mixins : [ AttrUtilsMixin ],
		displayName : 'TableHeader',
		propTypes : {
			entity : React.PropTypes.object.isRequired,
			attrs : React.PropTypes.object.isRequired,
			sort : React.PropTypes.object,
			onSort : React.PropTypes.func,
			onExpand : React.PropTypes.func,
			onCollapse : React.PropTypes.func,
			onCreate : React.PropTypes.func,
			enableAdd : React.PropTypes.bool,
			enableEdit : React.PropTypes.bool,
			enableDelete : React.PropTypes.bool,
			enableInspect : React.PropTypes.bool
		},
		render : function() {
			return thead(null, tr(null, this._createHeaders(this.props.attrs)));
		},
		_createHeaders : function(attrs) {
			var Headers = [];
			if (this.props.enableAdd === true) {
				Headers.push(th({
					className : 'compact',
					key : 'add'
				}, EntityCreateBtn({
					entity : this.props.entity,
					onCreate : this.props.onCreate
				})));
			}
			if (this.props.enableAdd === false && this.props.enableEdit === true) {
				Headers.push(th({
					className : 'compact',
					key : 'edit'
				}));
			}
			if (this.props.enableDelete === true) {
				Headers.push(th({
					className : 'compact',
					key : 'delete'
				}));
			}
			if (this.props.enableInspect) {
				Headers.push(th({
					className : 'compact',
					key : 'inspect'
				}));
			}
			this._createHeadersRec(this.props.entity.attributes, attrs, Headers, [], false);
			return Headers;
		},
		_createHeadersRec : function(attrs, selectedAttrs, Headers, path, expanded) {
			if (_.size(selectedAttrs) > 0) {
				for (var i = 0; i < attrs.length; ++i) {
					if (attrs[i].visible === true) {
						var attr = attrs[i];
						if (this._isSelectedAttr(attr, selectedAttrs)) {
							if (molgenis.isCompoundAttr(attr)) {
								this._createHeadersRec(attr.attributes, {
									'*' : null
								}, Headers, path, expanded);
							} else {
								var attrPath = path.concat(attr.name);
								if (this._isExpandedAttr(attr, selectedAttrs)) {
									var entityCollapseBtn = EntityCollapseBtn({
										attrPath : attrPath,
										onCollapse : this.props.onCollapse
									});
									Headers.push(th({
										className : 'expanded-left compact',
										key : 'c' + attrPath.join()
									}, entityCollapseBtn));
									this._createHeadersRec(attr.refEntity.attributes, selectedAttrs[attr.name], Headers, path.concat(attr.name), true);
								} else {
									if (this._canExpandAttr(attr, path)) {
										var entityExpandBtn = EntityExpandBtn({
											attrPath : attrPath,
											onExpand : this.props.onExpand
										});
										Headers.push(th({
											className : 'compact',
											key : 'e' + attrPath.join()
										}, entityExpandBtn));
									}
									var tableHeaderCell = TableHeaderCell({
										className : i === attrs.length - 1 && expanded ? 'expanded-right' : undefined,
										attr : attr,
										path : path,
										canSort : path.length === 0, // only
										// allow
										// sorting
										// of
										// top-level
										// attributes
										sortOrder : this._getSortOrder(attr, path),
										onSort : this.props.onSort,
										key : attrPath.join()
									});
									Headers.push(tableHeaderCell);
								}
							}
						}
					}
				}
			} else {
				// add an empty non-compacted column so that compact column stay
				// compact
				Headers.push(th({
					key : 'dummy'
				}));
			}
		},
		_getSortOrder : function(attr, path) {
			var sort = this.props.sort;
			return sort && this._getAttrId(sort.attr, sort.path) === this._getAttrId(attr, path) ? sort.order : null;
		},
		_getAttrId : function(attr, path) {
			return path.concat(attr.name).join('.');
		}
	});

	module.exports = React.createFactory(TableHeader);
});