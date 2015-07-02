/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, a = React.DOM.a, span = React.DOM.span, em = React.DOM.em, br = React.DOM.br;
	
	var api = new molgenis.RestClientV2();
	
	/**
	 * @memberOf component.mixin
	 */
	var AttrUtilsMixin = {
		_isSelectedAttr: function(attr, selectedAttrs) {
			return selectedAttrs['*'] !== undefined || selectedAttrs[attr.name] !== undefined;
		},
		_isExpandedAttr: function(attr, selectedAttrs) {
			return selectedAttrs[attr.name] !== null && selectedAttrs[attr.name] !== undefined;
		},
		_canExpandAttr: function(attr, path) {
			// expanding mrefs in expanded attr not supported
			return molgenis.isRefAttr(attr) && !(molgenis.isMrefAttr(attr) && _.size(path) > 0);
		}
	};
	
	/**
	 * @memberOf component
	 */
	var Table = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, AttrUtilsMixin],
		displayName: 'Table',
		propTypes: {
			entity: React.PropTypes.string.isRequired,
			attrs: React.PropTypes.object,
			query: React.PropTypes.object,
			maxRows: React.PropTypes.number,
			onRowAdd: React.PropTypes.func,
			onRowEdit: React.PropTypes.func,
			onRowDelete: React.PropTypes.func,
			onRowInspect: React.PropTypes.func,
			enableAdd: React.PropTypes.bool,
			enableEdit: React.PropTypes.bool,
			enableDelete: React.PropTypes.bool,
			enableInspect: React.PropTypes.bool,
			onSort: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				data: null,
				attrs: this.props.attrs,
				sort: null,
				start: 0
			};
		},
		getDefaultProps: function() {
			return {
				attrs: {'*': null},
				maxRows: 20,
				onRowAdd: function() {},
				onRowEdit: function() {},
				onRowDelete: function() {},
				onRowInspect: null,
				enableAdd: true,
				enableEdit: true,
				enableDelete: true,
				enableInspect: true,
				onSort: function() {}
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
			
			var writable = this.state.data.meta.writable;
			
			var TableHeader = TableHeaderFactory({
				entity: this.state.data.meta,
				attrs : this.state.attrs,
				sort: this.state.sort,
				enableAdd: writable && this.props.enableAdd === true,
				enableEdit: writable && this.props.enableEdit === true,
				enableDelete: writable && this.props.enableDelete === true,
				enableInspect: this.props.enableInspect === true && this.props.onRowInspect !== null,
				onSort : this._handleSort,
				onExpand : this._handleExpand,
				onCollapse : this._handleCollapse,
				onCreate: this._handleCreate
			});

			var TableBody = TableBodyFactory({
				data: this.state.data,
				attrs : this.state.attrs,
				enableEdit: writable && this.props.enableEdit === true,
				enableDelete: writable && this.props.enableDelete === true,
				enableInspect: this.props.enableInspect === true && this.props.onRowInspect !== null,
				onEdit: this._handleEdit,
				onDelete: this._handleDelete,
				onRowInspect: this.props.onRowInspect
			});
			
			var className = 'table table-striped table-condensed table-bordered molgenis-table';
			
			return (
				div(null,
					div({className: 'molgenis-table-container'},
						table({className: className},
							TableHeader,
							TableBody
						)
					),
					div({className: 'row'},
						div({className: 'col-md-offset-3 col-md-6'},
							div({className: 'text-center'},
								molgenis.ui.Pager({
									nrItems: this.state.data.total,
									nrItemsPerPage: this.props.maxRows,
									start: this.state.data.start,
									onPageChange: this._handlePageChange
								})
							)
						),
						div({className: 'col-md-3'},
							span({className: 'pull-right'}, em(null, this.state.data.total + ' item' + (this.state.data.total !== 1 ? 's' : '') + ' found'))
						)
					)
				)
			);
		},
		_refreshData: function(props, state) {
			var opts = {
				attrs: {'~id' : null}, // always include the id attribute
				num : props.maxRows
			};
			
			// add selected attrs
			if(state.attrs && _.size(state.attrs) > 0) {
				_.extend(opts.attrs, state.attrs);
			}
			
			if(props.query) {
				opts.q = props.query.q; 
			}
			if(state.sort) {
				opts.sort = {
					'orders' : [ {
						'attr' : state.sort.attr.name,
						'direction' : state.sort.order
					} ]
				};
			}
			if(state.start !== 0) {
				opts.start = state.start; 
			}
			api.get(props.entity, opts).done(function(data) {
				var newState = _.extend({}, state, {data: data});
				this.setState(newState);
			}.bind(this));
		},
		_handleExpand: function(e) {
			var attrs = JSON.parse(JSON.stringify(this.state.attrs)); // deep clone
			
			for(var i = 0, attrsAtDepth = attrs; i < e.attrPath.length; ++i) {
				var attr = e.attrPath[i];
				if(!attrsAtDepth[attr]) {
					attrsAtDepth[attr] = (i < e.attrPath.length - 1) ? {} : {'*': null};
				}
				attrsAtDepth = attrsAtDepth[attr];
			}
			
			this._refreshData(this.props, _.extend({}, this.state, {attrs: attrs}));
		},
		_handleCollapse: function(e) {
			var attrs = _.extend({}, this.state.attrs);
			
			for(var i = 0, attrsAtDepth = attrs; i < e.attrPath.length; ++i) {
				var attr = e.attrPath[i];
				if(i < e.attrPath.length - 1) {
					attrsAtDepth = attrsAtDepth[attr];
				} else {
					attrsAtDepth[attr] = null;
				}
			}
			
			this._refreshData(this.props, _.extend({}, this.state, {attrs: attrs}));
		},
		_handleCreate: function() {
			this._resetTable();
			this.props.onRowAdd();
		},
		_handleEdit: function() {
			this._resetTable();
			this.props.onRowEdit();
		},
		_handleDelete: function() {
			this._resetTable();
			this.props.onRowDelete();
		},
		_resetTable: function() {
			this._refreshData(this.props, _.extend({}, this.state, {start: 0}));
		},
		_handleSort: function(e) {
			this._refreshData(this.props, _.extend({}, this.state, {sort: e}));
			this.props.onSort(e);
		},
		_handlePageChange: function(e) {
			this._refreshData(this.props, _.extend({}, this.state, {start: e.start}));
		}
	});
	
	/**
	 * @memberOf component
	 */
	var TableHeader = React.createClass({
		mixins: [AttrUtilsMixin],
		displayName: 'TableHeader',
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			attrs: React.PropTypes.object.isRequired,
			sort: React.PropTypes.object,
			onSort: React.PropTypes.func,
			onExpand: React.PropTypes.func,
			onCollapse: React.PropTypes.func,
			onCreate: React.PropTypes.func,
			enableAdd: React.PropTypes.bool,
			enableEdit: React.PropTypes.bool,
			enableDelete: React.PropTypes.bool,
			enableInspect: React.PropTypes.bool
		},
		render: function() {
			return thead(null,
				tr(null,
					this._createHeaders(this.props.attrs)
				)
			);
		},
		_createHeaders: function(attrs) {
			var Headers = [];
			if(this.props.enableAdd === true) {
				Headers.push(th({className: 'compact', key: 'add'}, EntityCreateBtnFactory({
					entity: this.props.entity,
					onCreate: this.props.onCreate
				})));
			}
			if(this.props.enableAdd === false && this.props.enableEdit === true) {
				Headers.push(th({className: 'compact', key: 'edit'}));
			}
			if(this.props.enableDelete === true) {
				Headers.push(th({className: 'compact', key: 'delete'}));
			}
			if(this.props.enableInspect) {
				Headers.push(th({className: 'compact', key: 'inspect'}));
			}
			this._createHeadersRec(this.props.entity.attributes, attrs, Headers, [], false);
			return Headers;
		},
		_createHeadersRec: function(attrs, selectedAttrs, Headers, path, expanded) {
			if(_.size(selectedAttrs) > 0) {
				for(var i = 0; i < attrs.length; ++i) {
					if(attrs[i].visible === true) {
						var attr = attrs[i];
						if(this._isSelectedAttr(attr, selectedAttrs)) {
							if(molgenis.isCompoundAttr(attr)) {
								this._createHeadersRec(attr.attributes, {'*': null}, Headers, path, expanded);
							} else {
								var attrPath = path.concat(attr.name);
								if(this._isExpandedAttr(attr, selectedAttrs)) {
									var EntityCollapseBtn = EntityCollapseBtnFactory({
										attrPath: attrPath,
										onCollapse: this.props.onCollapse
									});
									Headers.push(th({className: 'expanded-left compact', key: 'c' + attrPath.join()}, EntityCollapseBtn));
									this._createHeadersRec(attr.refEntity.attributes, selectedAttrs[attr.name], Headers, path.concat(attr.name), true);
								}
								else {
									if(this._canExpandAttr(attr, path)) {
										var EntityExpandBtn = EntityExpandBtnFactory({
											attrPath: attrPath,
											onExpand: this.props.onExpand
										});
										Headers.push(th({className: 'compact', key: 'e' + attrPath.join()}, EntityExpandBtn));
									}
									var TableHeaderCell = TableHeaderCellFactory({
										className: i === attrs.length - 1 && expanded ? 'expanded-right' : undefined,
										attr: attr,
										path: path,
										canSort: path.length === 0, // only allow sorting of top-level attributes
										sortOrder: this._getSortOrder(attr, path),
										onSort: this.props.onSort,
										key: attrPath.join()
									});
									Headers.push(TableHeaderCell);
								}
							}
						}
					}
				}
			} else {
				// add an empty non-compacted column so that compact column stay compact
				Headers.push(th({key: 'dummy'}));
			}
		},
		_getSortOrder: function(attr, path) {
			var sort = this.props.sort;
			return sort && this._getAttrId(sort.attr, sort.path) === this._getAttrId(attr, path) ? sort.order : null;
		},
		_getAttrId: function(attr, path) {
			return path.concat(attr.name).join('.');
		}
	});
	var TableHeaderFactory = React.createFactory(TableHeader);
	
	/**
	 * @memberOf component
	 */
	var TableHeaderCell = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, AttrUtilsMixin],
		displayName: 'TableHeaderCell',
		propTypes: {
			attr: React.PropTypes.object.isRequired,
			path: React.PropTypes.array.isRequired,
			canSort: React.PropTypes.bool,
			sortOrder: React.PropTypes.oneOf(['asc', 'desc']),
			onSort: React.PropTypes.func,
			className: React.PropTypes.string
		},
		getDefaultProps: function() {
			return {
				sortOrder: null,
				onSort : function() {},
			};
		},
		render: function() {			
			var SortIcon = this.props.sortOrder !== null ? molgenis.ui.Icon({
				style: {marginLeft: 5},
				name: this.props.sortOrder === 'asc' ? 'sort-by-alphabet' : 'sort-by-alphabet-alt'
			}) : null;

			var Label = this.props.attr.description ? span(null, molgenis.ui.Popover({
				value: this.props.attr.label,
				popoverValue: this.props.attr.description
			})) : this.props.attr.label;
			
			return (
				th({className: this.props.className},
					this.props.canSort ? span({style: {cursor: 'pointer'}, onClick: this._handleSort},
						Label,
						SortIcon
					) : Label
				)
			);
		},
		_handleSort: function(e) {
			this.props.onSort({
				attr: this.props.attr,
				path: this.props.path,
				order : this.props.sortOrder === null ? 'asc' : (this.props.sortOrder === 'asc' ? 'desc' : 'asc')
			});
		}		
	});
	var TableHeaderCellFactory = React.createFactory(TableHeaderCell);
	
	/**
	 * @memberOf component
	 */
	var TableBody = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, AttrUtilsMixin],
		displayName: 'TableBody',
		propTypes: {
			data: React.PropTypes.object.isRequired,
			attrs: React.PropTypes.object.isRequired,
			enableEdit: React.PropTypes.bool,
			enableDelete: React.PropTypes.bool,
			enableInspect: React.PropTypes.bool,
			onEdit: React.PropTypes.func,
			onDelete: React.PropTypes.func,
			onRowInspect: React.PropTypes.func,
		},
		getDefaultProps: function() {
			return {
				onEdit: function() {},
				onDelete: function() {},
				onRowInspect: function() {}
			};
		},
		render: function() {
			return tbody(null, 
				this._createRows(this.props.data.meta)
			);
		},
		_createRows: function(entity) {
			var Rows = [];
			
			for(var i = 0; i < this.props.data.items.length; ++i) {
				var item = this.props.data.items[i];

				Rows.push(tr({
					key : '' + i
				}, this._createCols(item, entity)));
			}
			
			return Rows;
		},
		_createCols: function(item, entity) {
			var Cols = [];
			if(this.props.enableEdit === true) {
				var EntityEditBtn = EntityEditBtnFactory({
					name: entity.name,
					id : item[entity.idAttribute],
					onEdit: this.props.onEdit
				});
				Cols.push(td({className: 'compact', key: 'edit'}, EntityEditBtn));
			}
			if(this.props.enableDelete === true) {		
				var EntityDeleteBtn = EntityDeleteBtnFactory({
					name: entity.name,
					id : item[entity.idAttribute],
					onDelete: this.props.onDelete
				});
				Cols.push(td({className: 'compact', key: 'delete'}, EntityDeleteBtn));
			}
			if(this.props.enableInspect === true && this.props.onRowInspect !== null) {
				var EntityInspectBtn = EntityInspectBtnFactory({
					name: entity.name,
					id : item[entity.idAttribute],
					onInspect: this.props.onRowInspect
				});
				Cols.push(td({className: 'compact', key: 'inspect'}, EntityInspectBtn));
			}
			this._createColsRec(item, entity, entity.attributes, this.props.attrs, Cols, [], false);
			return Cols;
		},
		_createColsRec: function(item, entity, attrs, selectedAttrs, Cols, path, expanded) {
			if(_.size(selectedAttrs) > 0) {
				for(var j = 0; j < attrs.length; ++j) {
					var attr = attrs[j];
					if(this._isSelectedAttr(attr, selectedAttrs)) {
						if(attr.visible === true) {
							var attrPath = path.concat(attr.name);
							if(molgenis.isCompoundAttr(attr)) {
								this._createColsRec(item, entity, attr.attributes, {'*': null}, Cols, attrPath, expanded);
							} else {
								
								if(this._isExpandedAttr(attr, selectedAttrs)) {
									Cols.push(td({className: 'expanded-left', key : attrPath.join()}));
									var value = item !== undefined && item !== null ? item[attr.name] : null;
									this._createColsRec(value, attr.refEntity, attr.refEntity.attributes, selectedAttrs[attr.name], Cols, attrPath, true);
								} else {
									if(this._canExpandAttr(attr, path)) {
										Cols.push(td({key: 'e' + attrPath.join()}));
									}
									var value = (item !== undefined && item !== null) ? (_.isArray(item) ? _.map(item, function(value) { return value[attr.name];}) : item[attr.name]) : null;
									var TableCell = TableCellFactory({
										className: j === attrs.length - 1 && expanded ? 'expanded-right' : undefined, 
										entity: entity,
										attr : attr,
										value: value,
										expanded: expanded,
										onEdit: this.props.onEdit,
										key : attrPath.join()
									});
									Cols.push(TableCell);
								}
							}
						}
					}
				}
			} else {
				// add an empty non-compacted column so that compact column stay compact
				Cols.push(td({key: 'dummy'}));
			}
		}
	});
	var TableBodyFactory = React.createFactory(TableBody);
	
	/**
	 * @memberOf component
	 */
	var TableCell = React.createClass({
		displayName: 'TableCell',
		mixins: [AttrUtilsMixin],
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			attr: React.PropTypes.object.isRequired,
			value: React.PropTypes.any,
			expanded: React.PropTypes.bool,
			className: React.PropTypes.string,
			onEdit: React.PropTypes.func
		},
		shouldComponentUpdate: function(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props.entity.name, nextProps.entity.name)
					|| !_.isEqual(this.props.attr.name, nextProps.attr.name) || !_.isEqual(this.props.value, nextProps.value);
		},
		render: function() {
			var CellContentBlocks;
			// treat expanded mref differently
			if(this.props.expanded && _.isArray(this.props.value)) {
				CellContentBlocks = _.flatten(_.map(this.props.value, function(value, i) {
					if(value !== null && value !== undefined) {
						var CellContentForValue = this._createTableCellContent(value, 'c' + i); 
						return i < this.props.value.length - 1 ? [CellContentForValue, br({key: 'b' + i})] : CellContentForValue;
					} else {
						return null;
					}
				}.bind(this)));
			} else {
				CellContentBlocks = this.props.value !== null && this.props.value !== undefined ? [this._createTableCellContent(this.props.value)] : [];
			}
				
			return td({className: this.props.className}, CellContentBlocks);
		},
		_createTableCellContent: function(value, key) {
			return TableCellContentFactory({
				entity: this.props.entity,
				attr: this.props.attr,
				value: value,
				className: this.props.className,
				onEdit: this.props.onEdit,
				key: key
			});
		}
	});
	var TableCellFactory = React.createFactory(TableCell);
	
	/**
	 * @memberOf component
	 */
	var TableCellContent = React.createClass({
		mixins: [AttrUtilsMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'TableCellContent',
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			attr: React.PropTypes.object.isRequired,
			value: React.PropTypes.any,
			className: React.PropTypes.string,
			onEdit: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				onEdit: function() {}
			};
		},
		getInitialState: function() {
			return {
				showRef: false
			};
		},
		shouldComponentUpdate: function(nextProps, nextState) {
			return !_.isEqual(this.state, nextState) || !_.isEqual(this.props.entity.name, nextProps.entity.name)
					|| !_.isEqual(this.props.attr.name, nextProps.attr.name) || !_.isEqual(this.props.value, nextProps.value);
		},
		render: function() {
			return this._createValue(this.props.value);
		},
		renderLayer: function() {
			if(this.state.showRef) {
				var refEntity = this.props.attr.refEntity;
				
				var operator, value;
				if(molgenis.isXrefAttr(this.props.attr)) {
					operator = 'EQUALS';
					value = this.props.value[refEntity.idAttribute];
				} else {
					operator = 'IN';
					value = _.map(this.props.value, function(item) {
						return item[refEntity.idAttribute];
					});
				}
				
				var Table = molgenis.ui.Table({
					entity: this.props.attr.refEntity.name,
					query : {
						'q' : [ {
							'field' : refEntity.idAttribute,
							'operator' : operator,
							'value' : value
						} ]
					},
					enableAdd: false,
					enableDelete: false,
					enableInspect: false,
					onRowEdit: function(e) {
						this.props.onEdit(e);
					}.bind(this)
				});
				
				var OkBtn = (
					div({className: 'row', style: {textAlign: 'right'}},
						div({className: 'col-md-12'},
							molgenis.ui.Button({text: 'Ok', style: 'primary', onClick: this._toggleModal.bind(null, false)}, 'Ok')
						)
					)
				);
				
				return molgenis.ui.Modal({
					title: this.props.attr.label,
					show : true,
					onHide: this._toggleModal.bind(null, false)
				}, Table, OkBtn);
			} else {
				return null;
			}
		},
		_createValue: function(value) {
			var CellContent, attr = this.props.attr;
			
			if(value === undefined || value === null) {
				CellContent = span(null, String.fromCharCode(160)); // &nbsp;
			} else {
				switch(attr.fieldType) {
					case 'BOOL':
						CellContent = span(null, value.toString());
						break;
					case 'CATEGORICAL':
					case 'XREF':
						CellContent = a({href: '#', onClick: this._toggleModal.bind(null, true)}, span(null, value[attr.refEntity.labelAttribute]));
						break;
					case 'FILE':
						CellContent = (
							div(null,
								a({href: '#', onClick: this._toggleModal.bind(null, true)}, span(null, value[attr.refEntity.labelAttribute])),
								' ',
								a({href: value['url']},
									molgenis.ui.Icon({
										name: 'download',
										style: {cursor: 'pointer'}
									})
								)
							)
						);
						break;
					case 'CATEGORICAL_MREF':
					case 'MREF':
						CellContent = (
							span(null,
									_.flatten(_.map(value, function(item, i) {
										var Anchor = a({href: '#', onClick: this._toggleModal.bind(null, true), key: 'a' + i}, span(null, item[attr.refEntity.labelAttribute]));
										var Seperator = i < value.length - 1 ? span({key: 's' + i}, ',') : null; 
										return [Anchor, Seperator];
									}.bind(this)))
							)
						);
						break;
					case 'EMAIL':
						CellContent = a({href: 'mailto:' + value}, value);
						break;
					case 'HYPERLINK':
						CellContent = a({href: value, target: '_blank'}, value);
						break;
					case 'HTML':
					case 'SCRIPT':
					case 'TEXT':
						var maxLength = 50;
						if(value.length > maxLength) {
							CellContent = span(null, molgenis.ui.Popover({
								value: value.substring(0, maxLength - 3) + '...',
								popoverValue: value
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
		_toggleModal: function(show) {
			this.setState({
				showRef : show
			});
		}
	});
	var TableCellContentFactory = React.createFactory(TableCellContent);
	
	/**
	 * @memberOf component
	 */
	var EntityExpandBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'EntityExpandBtn',
		propTypes: {
			attrPath: React.PropTypes.array.isRequired,
			onExpand: React.PropTypes.func,
		},
		getDefaultProps: function() {
	    	return {
	    		onExpand: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'expand',
				size: 'xsmall',
				title: 'Expand entity',
				onClick : this._handleExpand
			});
		},
		_handleExpand: function() {
			this.props.onExpand({
				attrPath: this.props.attrPath
			});
		}
	});
	var EntityExpandBtnFactory = React.createFactory(EntityExpandBtn);
	
	/**
	 * @memberOf component
	 */
	var EntityCollapseBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'EntityCollapseBtn',
		propTypes: {
			attrPath: React.PropTypes.array.isRequired,
			onCollapse: React.PropTypes.func,
		},
		getDefaultProps: function() {
	    	return {
	    		onCollapse: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'collapse-up',
				size: 'xsmall',
				title: 'Collapse entity',
				onClick : this._handleCollapse
			});
		},
		_handleCollapse: function() {
			this.props.onCollapse({
				attrPath: this.props.attrPath
			});
		}
	});
	var EntityCollapseBtnFactory = React.createFactory(EntityCollapseBtn);
	
	/**
	 * @memberOf component
	 */
	var EntityCreateBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'EntityCreateBtn',
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			onCreate: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				form : false
			};
	    },
	    getDefaultProps: function() {
	    	return {
	    		onCreate: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'plus',
				title: 'Add row',
				style: 'success',
				size: 'xsmall',
				onClick : this._handleCreate
			});
		},
		renderLayer: function() {
			return this.state.form ? molgenis.ui.Form({
				entity: this.props.entity.name,
				mode: 'create',
				showHidden: true,
				modal: true,
				onSubmitSuccess: this._handleCreateConfirm,
				onSubmitCancel: this._handleCreateCancel
			}) : null;
		},
		_handleCreate: function() {
			this.setState({
				form: true
			});
		},
		_handleCreateCancel: function() {
			this.setState({
				form: false
			});
		},
		_handleCreateConfirm: function(e) {
			this.setState({
				form: false
			});
			this.props.onCreate({
				href : this.props._href
			});
		}
	});
	var EntityCreateBtnFactory = React.createFactory(EntityCreateBtn);
	
	/**
	 * @memberOf component
	 */
	var EntityEditBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'EntityEditBtn',
		propTypes: {
			name: React.PropTypes.string.isRequired,
			id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
			onEdit: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				form : false
			};
	    },
	    getDefaultProps: function() {
	    	return {
	    		onEdit: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'edit',
				title: 'Edit row',
				size: 'xsmall',
				onClick : this._handleEdit
			});
		},
		renderLayer: function() {
			return this.state.form ? molgenis.ui.Form({
				entity : this.props.name,
				entityInstance: this.props.id,
				mode: 'edit',
				showHidden: true,
				modal: true,
				onSubmitSuccess: this._handleEditConfirm,
				onSubmitCancel: this._handleEditCancel
			}) : null;
		},
		_handleEdit: function() {
			this.setState({
				form: true
			});
		},
		_handleEditCancel: function() {
			this.setState({
				form: false
			});
		},
		_handleEditConfirm: function() {
			this.setState({
				form: false
			});
			this.props.onEdit({
				name: this.props.name,
				id: this.props.id
			});
		}
	});
	var EntityEditBtnFactory = React.createFactory(EntityEditBtn);
	
	/**
	 * @memberOf component
	 */
	var EntityDeleteBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'EntityDeleteBtn',
		propTypes: {
			name: React.PropTypes.string.isRequired,
			id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
			onDelete: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				dialog : false
			};
	    },
	    getDefaultProps: function() {
	    	return {
	    		onDelete: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'trash',
				title: 'Delete row',
				style: 'danger',
				size: 'xsmall',
				onClick : this._handleDelete
			});
		},
		renderLayer: function() {
			return this.state.dialog ? molgenis.ui.Dialog({
				type: 'confirm',
				message : 'Are you sure you want to delete this row?',
				onCancel : this._handleDeleteCancel,
				onConfirm : this._handleDeleteConfirm
			}) : null;
		},
		_handleDelete: function() {
			this.setState({
				dialog : true
			});
		},
		_handleDeleteCancel: function() {
			this.setState({
				dialog : false
			});
		},
		_handleDeleteConfirm: function() {
			this.setState({
				dialog : false
			});
			api.remove(this.props.name, this.props.id).done(function() {
				this.props.onDelete({
					name : this.props.name,
					id : this.props.id
				});
			}.bind(this));
		},
	});
	var EntityDeleteBtnFactory = React.createFactory(EntityDeleteBtn);
	
	/**
	 * @memberOf component
	 */
	var EntityInspectBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'EntityInspectBtn',
		propTypes: {
			name: React.PropTypes.string.isRequired,
			id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
			onInspect: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				dialog : false
			};
	    },
	    getDefaultProps: function() {
	    	return {
	    		onInspect: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'search',
				style: 'info',
				title: 'Inspect row',
				size: 'xsmall',
				onClick : this._handleClick
			});
		},
		_handleClick: function() {
			this.props.onInspect({
				name : this.props.name,
				id : this.props.id
			});
		}
	});
	var EntityInspectBtnFactory = React.createFactory(EntityInspectBtn);
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Table: React.createFactory(Table)
	});
}(_, React, molgenis));