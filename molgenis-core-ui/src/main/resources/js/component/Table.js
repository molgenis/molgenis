/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, a = React.DOM.a, span = React.DOM.span, em = React.DOM.em;
	
	var api = new molgenis.RestClientV2();
	
	/**
	 * @memberOf component.mixin
	 */
	var AttrUtilsMixin = {
		_isRefAttr: function(attr) {
			switch(attr.fieldType) {
				case 'CATEGORICAL':
				case 'CATEGORICAL_MREF':
				case 'MREF':
				case 'XREF':
					return true;
				default:
					return false;
			}  
		},
		_isMrefAttr: function(attr) {
			switch(attr.fieldType) {
				case 'CATEGORICAL_MREF':
				case 'MREF':
					return true;
				default:
					return false;
			}  
		},
		_isXrefAttr: function(attr) {
			return attr.fieldType === 'CATEGORICAL' || attr.fieldType === 'XREF';
		},
		_isMrefAttr: function(attr) {
			return attr.fieldType === 'CATEGORICAL_MREF' || attr.fieldType === 'MREF';
		},
		_isCompoundAttr: function(attr) {
			return attr.fieldType === 'COMPOUND';
		},
		_toAttrsArray: function(attrs) {
			if( Object.prototype.toString.call( attrs ) === '[object Array]' ) {
				return attrs;
			}
			
			// object to array
			return _.map(attrs, function(attr) {
				return attr;
			});
		},
		_isExpandedAttr: function(attrPath, expands) {
			for(var i = 0, expandsAtDepth = expands; i < attrPath.length; ++i) {
				if(expandsAtDepth[attrPath[i]] === undefined) {
					return false;
				}
				expandsAtDepth = expandsAtDepth[attrPath[i]]; 
			}
			return true;
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
			attrs: React.PropTypes.arrayOf(React.PropTypes.string), // TODO can we merge props.attrs and state.expands?
			query: React.PropTypes.arrayOf(React.PropTypes.object),
			maxRows: React.PropTypes.number,
			onRowAdd: React.PropTypes.func,
			onRowEdit: React.PropTypes.func,
			onRowDelete: React.PropTypes.func,
			onRowInspect: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				data: null,
				expands: {}, // TODO can we merge props.attrs and state.expands?
				sort: null,
				start: 0
			};
		},
		getDefaultProps: function() {
			return {
				maxRows: 20,
				onRowAdd: null,
				onRowEdit: null,
				onRowDelete: null,
				onRowInspect: null
			};
		},
		componentDidMount: function() {
			this._refreshData(this.props);
		},
		componentWillReceiveProps : function(nextProps) {
			this._refreshData(nextProps);
		},
		render: function() {
			if(this.state.data === null) {
				return molgenis.ui.Spinner(); // entity not available yet
			}
			
			var attrs = this._toAttrsArray(this.state.data.meta.attributes);
			var mode = this.state.data.meta.writable ? 'edit' : 'view';
			
			var TableHeader = TableHeaderFactory({
				mode: mode,
				entity: this.state.data.meta,
				attrs : attrs,
				sort: this.state.sort,
				onSort : this._handleSort,
				expands: this.state.expands,
				onExpand : this._handleExpand,
				onCollapse : this._handleCollapse,
				onCreate: this._handleCreate,
				rowClickable: this.props.onRowInspect !== undefined && this.props.onRowInspect !== null 
			});
			

			var TableBody = TableBodyFactory({
				mode: mode,
				data: this.state.data,
				expands: this.state.expands,
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
		_refreshData: function(props) {
			var opts = {
				num : props.maxRows
			};
			if(props.attrs && props.attrs.length > 0) {
				opts.attributes = {};
				for(var i = 0; i < props.attrs.length; ++i) {
					opts.attributes[props.attrs[i]] = null;
				}
			}
			if(_.size(this.state.expands) > 0) {
				if(opts.attributes) {
					opts.attributes = _.extend(opts.attributes, this.state.expands);	
				} else {
					opts.attributes = _.extend({}, {'*': null}, this.state.expands);	
				}
			}
			if(props.query) {
				opts.q = props.query; 
			}
			if(this.state.sort) {
				opts.sort = {
					'orders' : [ {
						'property' : this.state.sort.attr.name,
						'direction' : this.state.sort.order
					} ]
				};
			}
			if(this.state.start !== 0) {
				opts.start = this.state.start; 
			}
			api.get(props.entity, opts).done(function(data) {
				this.setState({data: data});
			}.bind(this));
		},
		_handleExpand: function(e) {
			var expands = JSON.parse(JSON.stringify(this.state.expands)); // deep clone
			
			for(var i = 0, expandsAtDepth = expands; i < e.attrPath.length; ++i) {
				var attr = e.attrPath[i];
				if(!expandsAtDepth[attr]) {
					expandsAtDepth[attr] = (i < e.attrPath.length - 1) ? {} : {'*': null};
				}
				expandsAtDepth = expandsAtDepth[attr];
			}
			
			this.setState({expands: expands}, function() {
				this._refreshData(this.props);
			});
		},
		_handleCollapse: function(e) {
			var expands = _.extend({}, this.state.expands);
			
			for(var i = 0, expandsAtDepth = expands; i < e.attrPath.length; ++i) {
				var attr = e.attrPath[i];
				if(i < e.attrPath.length - 1) {
					expandsAtDepth = expandsAtDepth[attr];
				} else {
					delete expandsAtDepth[attr];
				}
			}
			
			this.setState({expands: expands}, function() {
				this._refreshData(this.props);
			});
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
			this.setState({start : 0}, function() {
				this._refreshData(this.props);
			});
		},
		_handleSort: function(e) {
			this.setState({sort : e}, function() {
				this._refreshData(this.props);
			});
		},
		_handlePageChange: function(e) {
			this.setState({start : e.start}, function() {
				this._refreshData(this.props);
			});
		}
	});
	
	/**
	 * @memberOf component
	 */
	var TableHeader = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, AttrUtilsMixin],
		displayName: 'TableHeader',
		propTypes: {
			mode: React.PropTypes.oneOf(['view', 'edit']),
			entity: React.PropTypes.object.isRequired,
			attrs: React.PropTypes.array.isRequired,
			expands: React.PropTypes.object,
			sort: React.PropTypes.object,
			onSort: React.PropTypes.func,
			onExpand: React.PropTypes.func,
			onCollapse: React.PropTypes.func,
			onCreate: React.PropTypes.func,
			rowClickable: React.PropTypes.bool
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
			if(this.props.mode === 'edit') {
				Headers.push(th({className: 'compact', key: 'edit'}, EntityCreateBtnFactory({
					entity: this.props.entity,
					onCreate: this.props.onCreate
				})));
				
				Headers.push(th({className: 'compact', key: 'delete'}));
			}
			if(this.props.rowClickable) {
				Headers.push(th({className: 'compact', key: 'report'}));
			}
			this._createHeadersRec(attrs, Headers, [], false);
			return Headers;
		},
		_createHeadersRec: function(attrs, Headers, path, expanded) {
			attrs = this._toAttrsArray(attrs);
			for(var i = 0; i < attrs.length; ++i) {
				if(attrs[i].visible === true) {
					var attr = attrs[i];
					if(this._isCompoundAttr(attr)) {
						this._createHeadersRec(attr.attributes, Headers, path, expanded);
					} else {
						var attrPath = path.concat(attr.name);
						if(this._isExpandedAttr(attrPath, this.props.expands)) {
							var EntityCollapseBtn = EntityCollapseBtnFactory({
								attrPath: attrPath,
								onCollapse: this.props.onCollapse
							});
							Headers.push(th({className: 'expanded-left compact', key: 'c' + attrPath.join()}, EntityCollapseBtn));
							this._createHeadersRec(attr.refEntity.attributes, Headers, path.concat(attr.name), true);
						}
						else {
							if(this._isRefAttr(attr)) {
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
								canCollapse: expanded,
								canExpand: this._isRefAttr(attr), 
								expanded: this._isExpandedAttr(attrPath, this.props.expands),
								onExpand: this.props.onExpand,
								key: attrPath.join()
							});
							Headers.push(TableHeaderCell);
						}
					}
				}
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
			sortOrder: React.PropTypes.oneOf(['ASC', 'DESC']),
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
				name: this.props.sortOrder === 'ASC' ? 'sort-by-alphabet' : 'sort-by-alphabet-alt'
			}) : null;

			return (
				th({className: this.props.className},
					this.props.canSort ? span({style: {cursor: 'pointer'}, onClick: this._handleSort},
						this.props.attr.label,
						SortIcon
					) : this.props.attr.label
				)
			);
		},
		_handleSort: function(e) {
			this.props.onSort({
				attr: this.props.attr,
				path: this.props.path,
				order : this.props.sortOrder === null ? 'ASC' : (this.props.sortOrder === 'ASC' ? 'DESC' : 'ASC')
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
			mode: React.PropTypes.oneOf(['view', 'edit']),
			data: React.PropTypes.object.isRequired,
			expands: React.PropTypes.object.isRequired,
			onEdit: React.PropTypes.func,
			onDelete: React.PropTypes.func,
			onRowInspect: React.PropTypes.func,
		},
		getDefaultProps: function() {
			return {
				onEdit: function() {},
				onDelete: function() {}
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
			if(this.props.mode === 'edit') {
				var EntityEditBtn = EntityEditBtnFactory({
					entity: entity,
					href : item.href,
					onEdit: this.props.onEdit
				});
				Cols.push(td({className: 'compact', key: 'edit'}, EntityEditBtn));
				
				var EntityDeleteBtn = EntityDeleteBtnFactory({
					href : item.href,
					onDelete: this.props.onDelete
				});
				Cols.push(td({className: 'compact', key: 'delete'}, EntityDeleteBtn));
			}
			if(this.props.onRowInspect) {
				var EntityReportBtn = EntityReportBtnFactory({
					href : item.href,
					onReport: this.props.onRowInspect
				});
				Cols.push(td({className: 'compact', key: 'report'}, EntityReportBtn));
			}
			this._createColsRec(item, entity, entity.attributes, Cols, [], false);
			return Cols;
		},
		_createColsRec: function(item, entity, attrs, Cols, path, expanded) {
			attrs = this._toAttrsArray(attrs);
			for(var j = 0; j < attrs.length; ++j) {
				var attr = attrs[j];
				if(attr.visible === true) {
					var attrPath = path.concat(attr.name);
					if(this._isCompoundAttr(attr)) {
						this._createColsRec(item, entity, attr.attributes, Cols, attrPath, expanded);
					} else {
						if(this._isExpandedAttr(attrPath, this.props.expands)) {
							Cols.push(td({className: 'expanded-left', key : attrPath.join()}));
							this._createColsRec(item[attr.name], attr.refEntity, attr.refEntity.attributes, Cols, attrPath, true);
						} else {
							if(this._isRefAttr(attr)) {
								Cols.push(td({key: 'e' + attrPath.join()}));
							}
							var value = _.isArray(item) ? _.map(item, function(value) { return value[attr.name];}) : item[attr.name];
							var TableCell = TableCellFactory({
								className: j === attrs.length - 1 && expanded ? 'expanded-right' : undefined, 
								entity: entity,
								attr : attr,
								value: value,
								valueHref: item.href + '/' + htmlEscape(attr.name),
								multiple: _.isArray(item),
								key : attrPath.join()
							});
							Cols.push(TableCell);
						}
					}
				}
			}
		}
	});
	var TableBodyFactory = React.createFactory(TableBody);
	
	/**
	 * @memberOf component
	 */
	var TableCell = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin, AttrUtilsMixin, molgenis.ui.mixin.ReactLayeredComponentMixin],
		displayName: 'TableCell',
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			attr: React.PropTypes.object.isRequired,
			value: React.PropTypes.any.isRequired,
			valueHref: React.PropTypes.string.isRequired,
			multiple: React.PropTypes.bool,
			className: React.PropTypes.string,
			onValueUpdate: React.PropTypes.func 
		},
		getInitialState: function() {
			return {
				showRef: false
			};
		},
		render: function() {
			var CellContent;
			if(this.props.multiple) {
				CellContent = ( 
					_.flatten(_.map(this.props.value, function(value, i) {
						return [this._createValue(value, i), i < this.props.value.length - 1 ? React.DOM.br({key: 'b' + i}) : null];	
					}.bind(this)))
				);
			} else {
				CellContent = this._createValue(this.props.value);
			}
				
			return td({className: this.props.className}, CellContent);
		},
		renderLayer: function() {
			if(this.state.showRef) {
				var refEntity = this.props.attr.refEntity;
				
				var operator, value;
				if(this._isXrefAttr(this.props.attr)) {
					operator = 'EQUALS';
					value = this.props.value[refEntity.idAttribute];
				} else {
					operator = 'IN';
					value = _.map(this.props.value.items, function(item) {
						return item[refEntity.idAttribute];
					});
				}
				
				var Table = molgenis.ui.Table({
					entity: this.props.attr.refEntity.name,
					query :{
						'q' : [ {
							'field' : refEntity.idAttribute,
							'operator' : operator,
							'value' : value
						} ]
					}
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
					case 'CATEGORICAL_MREF':
					case 'MREF':
						CellContent = _.flatten(_.map(value, function(item, i) {
							var Anchor = a({href: '#', onClick: this._toggleModal.bind(null, true), key: 'a' + i}, span(null, item[attr.refEntity.labelAttribute]));
							var Seperator = i < value.length - 1 ? span({key: 's' + i}, ',') : null; 
							return [Anchor, Seperator];
						}.bind(this)));
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
					case 'FILE':
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
	var TableCellFactory = React.createFactory(TableCell);
	
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
				entity: this.props.entity,
				mode: 'create',
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
				href : this.props.href
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
			entity: React.PropTypes.object.isRequired,
			href: React.PropTypes.string.isRequired,
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
				entity : this.props.entity,
				entityInstance: this.props.href,
				mode: 'edit',
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
				href : this.props.href
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
			href: React.PropTypes.string.isRequired,
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
			api.remove(this.props.href, {
				success: function() {
					this.props.onDelete({href: this.props.href});
				}.bind(this)
			});
		},
	});
	var EntityDeleteBtnFactory = React.createFactory(EntityDeleteBtn);
	
	/**
	 * @memberOf component
	 */
	var EntityReportBtn = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'EntityReportBtn',
		propTypes: {
			href: React.PropTypes.string.isRequired,
			onReport: React.PropTypes.func
		},
		getInitialState: function() {
			return {
				dialog : false
			};
	    },
	    getDefaultProps: function() {
	    	return {
	    		onReport: function() {}
	    	};
	    },
		render: function() {
			return molgenis.ui.Button({
				icon: 'search',
				style: 'info',
				title: 'Inspect row',
				size: 'xsmall',
				onClick : this.props.onReport.bind(null, this.props.href)
			});
		}
	});
	var EntityReportBtnFactory = React.createFactory(EntityReportBtn);
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Table: React.createFactory(Table)
	});
}(_, React, molgenis));