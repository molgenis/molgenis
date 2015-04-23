/* global _: false, React: false, molgenis: true */
(function(_, React, molgenis) {
	"use strict";
	
	var div = React.DOM.div, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td;
	
	var api = new molgenis.RestClient();
	
	/**
	 * @memberOf component
	 */
	var Table = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'Table',
		propTypes: {
			entity: React.PropTypes.string.isRequired,
			attrs: React.PropTypes.arrayOf(React.PropTypes.object)
		},
		getInitialState: function() {
			return {
				entity: null,
				attrs: null
			};
		},
		componentDidMount: function() {
			this._loadEntity(this.props.entity, function(entity) {
				this._loadAttrs(this.props.attrs ? this.props.attrs : entity.attributes);
			}.bind(this));
		},
		componentWillUpdate: function() {
			
		},
		render: function() {
			if(this.state.entity === null || this.state.attrs === null) {
				return molgenis.ui.Spinner(); // entity not available yet
			}
			
			return (
				div(null,
					TableDataFactory({entity: this.state.entity, attrs: this.state.attrs}),
					TableControlsFactory({})
				)
			);
		},
		_loadEntity: function(href, callback) {
			api.getAsync(href, {'expand': ['attributes']}).done(function(entity) {
				if (this.isMounted()) {
					this.setState({entity: entity});
					if(callback) {
						callback(entity);
					}
				}
			}.bind(this));			
		},
		_loadAttrs: function(attrs) {
			this.setState({
				attrs : molgenis.getAtomicAttributes(attrs, api)
			});
		}
	});
	
	/**
	 * @memberOf component
	 */
	var TableData = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'TableData',
		propTypes: {
			entity: React.PropTypes.object.isRequired,
			attrs: React.PropTypes.arrayOf(React.PropTypes.object)
		},
		getInitialState: function() {
			return {
				expands: {}
			};
		},
		render: function() {
			var headers = [];
			for(var i = 0; i < this.props.attrs.length; ++i) {
				var attr = this.props.attrs[i];
				headers.push({
					id: [this.props.entity.name],
					label: attr.label,
					canSort: true,
					canExpand: attr.refEntity !== undefined,
					headers : attr.refEntity !== undefined ? [ {
						id : [ 'attr0' ],
						label : 'attr0',
						canSort : false,
						canExpand : false
					}, {
						id : [ 'attr1' ],
						label : 'attr1',
						canSort : false,
						canExpand : true,
						headers : [ {
							id : [ 'attr10' ],
							label : 'attr10',
							canSort : false,
							canExpand : false
						}, {
							id : [ 'attr11' ],
							label : 'attr11',
							canSort : false,
							canExpand : false
						} ]
					} ] : undefined
				});
			}
			// table header
			var TableHeader = TableHeaderFactory({
				headers: headers,
				onSort: this._handleSort,
				onExpand: this._handleExpand
			});
			
			// table body
			var TableBody = TableBodyFactory({
				entity : this.props.entity
			});
			
			return (
				table({className: 'table table-striped table-condensed'},
					TableHeader,
					TableBody
				)
			);
		},
		_handleSort: function(e) {
			console.log(e);
		},
		_handleExpand: function(e) {
			console.log(e);
		}
	});
	var TableDataFactory = React.createFactory(TableData);
	
	/**
	 * @memberOf component
	 */
	var TableHeader = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'TableHeader',
		propTypes: {
			headers: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
			onSort: React.PropTypes.func,
			onExpand: React.PropTypes.func,
		},
		getDefaultProps: function() {
			return {
				onSort: function() {},
				onExpand: function() {}
			};
		},
		render: function() {
			var TableHeaderRows = [];
			var depth = 0, headers;
			while((headers = this._getHeadersAtDepth(this.props.headers, depth)) !== null) {
				var TableHeaderRow = TableHeaderRowFactory({
					headers: headers,
					onSort: this.props.onSort,
					onExpand: this.props.onExpand,
					key: '' + depth
				});
				TableHeaderRows.push(TableHeaderRow);
				++depth;
			}
			
			return (
				thead(null,
					TableHeaderRows
				)
			);
		},
		_getHeadersAtDepth: function(headers, depth) {
			if(depth === 0) {
				return headers;
			} else {
				var headersAtDepth = _.flatten(_.map(headers, function(header) {
					return header.headers ? this._getHeadersAtDepth(header.headers, depth - 1) : null;
				}.bind(this)));
				if(_.every(headersAtDepth, function(e) {return e === null;})) {
					headersAtDepth = null;
				}
				return headersAtDepth;
			}
		},
	});
	var TableHeaderFactory = React.createFactory(TableHeader);
	
	/**
	 * @memberOf component
	 */
	var TableHeaderRow = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'TableHeaderRow',
		propTypes: {
			headers: React.PropTypes.arrayOf(React.PropTypes.object).isRequired,
			onSort: React.PropTypes.func,
			onExpand: React.PropTypes.func,
		},
		render: function() {
			var TableHeaderCols = [];
			for(var i = 0; i < this.props.headers.length; ++i) {
				var header = this.props.headers[i];
				if(header === null) {
					TableHeaderCols.push(th({key: '' + i}));
				} else {
					// create header column
					var TableHeaderCol = TableHeaderColFactory({
						label : header.label,
						colSpan: this._getColSpan(header),
						canSort : header.canSort,
						canExpand: header.canExpand,
						onExpand: function(props) {
							this.onExpand(_.extend({}, props, {
								id: this.id
							}));
						}.bind({onExpand: this.props.onExpand, id: header.id}),
						onSort: function(props) {
							this.onSort(_.extend({}, props, {
								id: this.id
							}));
						}.bind({onSort: this.props.onSort, id: header.id}),
						key: '' + i
					});
					TableHeaderCols.push(TableHeaderCol);
				}
			}
			
			return (
				tr(null,
					TableHeaderCols	
				)
			);
		},
		_getColSpan: function(header) {
 			if(header.headers) {
 				var colSpan = 0;
 				for(var i = 0; i < header.headers.length; ++i) {
 					colSpan += this._getColSpan(header.headers[i]);
 				}
 				return colSpan;
 			} else {
 				return 1;
 			}
		}
	});
	var TableHeaderRowFactory = React.createFactory(TableHeaderRow);
	
	/**
	 * @memberOf component
	 */
	var TableHeaderCol = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'TableHeaderCol',
		propTypes: {
			label: React.PropTypes.string.isRequired,
			colSpan: React.PropTypes.number.isRequired,
			canSort: React.PropTypes.bool,
			canExpand: React.PropTypes.bool,
			onSort: React.PropTypes.func,
			onExpand: React.PropTypes.func
		},
		getDefaultProps: function() {
			return {
				canSort: false,
				canExpand: false,
				onSort: function() {},
				onExpand: function() {}
			};
		},
		getInitialState: function() {
			return {
				sort: null,
				expand: false
			};
		},
		render: function() {
			// sort on attribute
			var SortIcon = this.props.canSort ? molgenis.ui.Icon({
				name : this.state.sort === null ? 'sort' : (this.state.sort === 'ASC' ? 'sort-by-alphabet' : 'sort-by-alphabet-alt'),
				onClick : this._handleSortIconClick
			}) : null;
			
			// expand/collapse attribute
			var ExpandCollapseIcon = this.props.canExpand ? molgenis.ui.Icon({
				name : this.state.expand ? 'minus' : 'plus',
				onClick : this._handleExpandCollapseIconClick
			}) : null;
			
			return (
				th({colSpan: this.props.colSpan},
					this.props.label,
					SortIcon ? ' ' : '',
					SortIcon,
					ExpandCollapseIcon ? ' ' : '',
					ExpandCollapseIcon 
				)
			);
		},
		_handleSortIconClick: function() {
			var sort = this.state.sort === null ? 'ASC' : (this.state.sort === 'ASC' ? 'DESC' : 'ASC');
			this.setState({
				sort : sort
			});
			this.props.onSort({
				sort : sort
			});
		},
		_handleExpandCollapseIconClick: function() {
			var expand = !this.state.expand;
			this.setState({
				expand : expand
			});
			this.props.onExpand({
				expand : expand
			});
		}
	});
	var TableHeaderColFactory = React.createFactory(TableHeaderCol);
	
	/**
	 * @memberOf component
	 */
	var TableBody = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'TableBody',
		propTypes: {
			entity: React.PropTypes.object.isRequired
		},
		render: function() {
			return (
				tbody({})
			);
		}
	});
	var TableBodyFactory = React.createFactory(TableBody);
	
	/**
	 * @memberOf component
	 */
	var TableControls = React.createClass({
		mixins: [molgenis.ui.mixin.DeepPureRenderMixin],
		displayName: 'TableControls',
		propTypes: {
		},
		render: function() {
			return (
				div({className: 'row'},
					div({className: 'col-md-12'},
						'TODO: table controls'
					)
				)
			);
		}
	});
	var TableControlsFactory = React.createFactory(TableControls);
	
	// export component
	molgenis.ui = molgenis.ui || {};
	_.extend(molgenis.ui, {
		Table: React.createFactory(Table)
	});
}(_, React, molgenis));