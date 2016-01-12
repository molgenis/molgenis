define(function(require, exports, module) {
	/**
	 * @module TableCellContent
	 */
	"use strict";

	var React = require('react');
	var _ = require('underscore');
	var molgenis = require('../modules/MolgenisQuery');

	var Button = require('./Button');
	var Icon = require('./Icon');
	var Modal = require('./Modal');
	var Table = require('./Table');
	var Popover = require('./Popover');

	var AttrUtilsMixin = require('./mixin/AttrUtilsMixin');
	var ReactLayeredComponentMixin = require('./mixin/ReactLayeredComponentMixin');

	var div = React.DOM.div, a = React.DOM.a, span = React.DOM.span;

	/**
	 * @memberOf TableCellContent
	 */
	var TableCellContent = React.createClass({
		mixins : [ AttrUtilsMixin, ReactLayeredComponentMixin ],
		displayName : 'TableCellContent',
		propTypes : {
			entity : React.PropTypes.object.isRequired,
			attr : React.PropTypes.object.isRequired,
			value : React.PropTypes.any,
			className : React.PropTypes.string,
			onEdit : React.PropTypes.func
		},
		getDefaultProps : function() {
			return {
				onEdit : function() {
				}
			};
		},
		getInitialState : function() {
			return {
				showRef : false
			};
		},
		shouldComponentUpdate : function(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props.entity.name, nextProps.entity.name)
					|| !_.isEqual(this.props.attr.name, nextProps.attr.name) || !_.isEqual(this.props.value, nextProps.value);
		},
		render : function() {
			return this._createValue(this.props.value);
		},
		renderLayer : function() {
			if (this.state.showRef) {
				var refEntity = this.props.attr.refEntity;

				var operator, value;
				if (molgenis.isXrefAttr(this.props.attr)) {
					operator = 'EQUALS';
					value = this.props.value[refEntity.idAttribute];
				} else {
					operator = 'IN';
					value = _.map(this.props.value, function(item) {
						return item[refEntity.idAttribute];
					});
				}

				var table = Table({
					entity : this.props.attr.refEntity.name,
					query : {
						'q' : [ {
							'field' : refEntity.idAttribute,
							'operator' : operator,
							'value' : value
						} ]
					},
					enableAdd : false,
					enableDelete : false,
					enableInspect : false,
					onRowEdit : function(e) {
						this.props.onEdit(e);
					}.bind(this)
				});

				var OkBtn = (div({
					className : 'row',
					style : {
						textAlign : 'right'
					}
				}, div({
					className : 'col-md-12'
				}, Button({
					text : 'Ok',
					style : 'primary',
					onClick : this._toggleModal.bind(null, false)
				}, 'Ok'))));

				return Modal({
					title : this.props.attr.label,
					show : true,
					onHide : this._toggleModal.bind(null, false)
				}, table, OkBtn);
			} else {
				return null;
			}
		},
		_createValue : function(value) {
			var CellContent, attr = this.props.attr;

			if (value === undefined || value === null) {
				CellContent = span(null, String.fromCharCode(160)); // &nbsp;
			} else {
				switch (attr.fieldType) {
				case 'BOOL':
					CellContent = span(null, value.toString());
					break;
				case 'CATEGORICAL':
				case 'XREF':
					CellContent = a({
						href : '#',
						onClick : this._toggleModal.bind(null, true)
					}, span(null, value[attr.refEntity.labelAttribute]));
					break;
				case 'FILE':
					CellContent = (div(null, a({
						href : '#',
						onClick : this._toggleModal.bind(null, true)
					}, span(null, value[attr.refEntity.labelAttribute])), ' ', a({
						href : value['url']
					}, Icon({
						name : 'download',
						style : {
							cursor : 'pointer'
						}
					}))));
					break;
				case 'CATEGORICAL_MREF':
				case 'MREF':
					CellContent = (span(null, _.flatten(_.map(value, function(item, i) {
						var Anchor = a({
							href : '#',
							onClick : this._toggleModal.bind(null, true),
							key : 'a' + i
						}, span(null, item[attr.refEntity.labelAttribute]));
						var Seperator = i < value.length - 1 ? span({
							key : 's' + i
						}, ',') : null;
						return [ Anchor, Seperator ];
					}.bind(this)))));
					break;
				case 'EMAIL':
					CellContent = a({
						href : 'mailto:' + value
					}, value);
					break;
				case 'HYPERLINK':
					CellContent = a({
						href : value,
						target : '_blank'
					}, value);
					break;
				case 'HTML':
				case 'SCRIPT':
				case 'TEXT':
					var maxLength = 50;
					if (value.length > maxLength) {
						CellContent = span(null, Popover({
							value : value.substring(0, maxLength - 3) + '...',
							popoverValue : value
						}));
					} else {
						CellContent = span(null, value);
					}
					break;
				case 'IMAGE':
					throw 'Unsupported data type: ' + attr.fieldType;
				default:
					CellContent = span(null, value);
					break;
				}
			}

			return CellContent;
		},
		_toggleModal : function(show) {
			this.setState({
				showRef : show
			});
		}
	});

	module.exports = React.createFactory(TableCellContent);
});