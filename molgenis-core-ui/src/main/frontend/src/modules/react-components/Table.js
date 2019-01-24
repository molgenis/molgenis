import React from "react";
import RestClientV2 from "rest-client/RestClientV2";
import DeepPureRenderMixin from "./mixin/DeepPureRenderMixin";
import Spinner from "./Spinner";
import Button from "./Button";
import _ from "underscore";
import SelectBox from "./SelectBox";
import Pager from "./Pager";
import Icon from "./Icon";
import Popover from "./Popover";
import ReactLayeredComponentMixin from "./mixin/ReactLayeredComponentMixin";
import Modal from "./Modal";
import Form from "./Form";
import Dialog from "./Dialog";
import {isCompoundAttr, isMrefAttr, isRefAttr, isXrefAttr} from "rest-client/AttributeFunctions";
import "./css/Table.css";
import moment from "moment";

var div = React.DOM.div, table = React.DOM.table, thead = React.DOM.thead, tbody = React.DOM.tbody, tr = React.DOM.tr, th = React.DOM.th, td = React.DOM.td, a = React.DOM.a, span = React.DOM.span, em = React.DOM.em, br = React.DOM.br, label = React.DOM.label;

var api = new RestClientV2();

/**
 * @memberOf component.mixin
 */
var AttrUtilsMixin = {
    _isSelectedAttr: function (attr, selectedAttrs) {
        return selectedAttrs['*'] !== undefined || selectedAttrs[attr.name] !== undefined;
    },
    _isExpandedAttr: function (attr, selectedAttrs) {
        return selectedAttrs[attr.name] !== null && selectedAttrs[attr.name] !== undefined;
    },
    _canExpandAttr: function (attr, path) {
        // expanding mrefs in expanded attr not supported
        return isRefAttr(attr) && !(isMrefAttr(attr) && _.size(path) > 0);
    }
};

/**
 * @memberOf component
 */
var Table = React.createClass({
    mixins: [DeepPureRenderMixin, AttrUtilsMixin],
    displayName: 'Table',
    propTypes: {
        entity: React.PropTypes.string.isRequired,
        attrs: React.PropTypes.object,
        query: React.PropTypes.object,
        sort: React.PropTypes.object,
        maxRows: React.PropTypes.number,
        onRowAdd: React.PropTypes.func,
        onRowEdit: React.PropTypes.func,
        onRowDelete: React.PropTypes.func,
        onRowInspect: React.PropTypes.func,
        onRowClick: React.PropTypes.func,
        onExecute: React.PropTypes.func,
        enableAdd: React.PropTypes.bool,
        enableEdit: React.PropTypes.bool,
        enableDelete: React.PropTypes.bool,
        enableInspect: React.PropTypes.bool,
        enableExecute: React.PropTypes.bool,
        onSort: React.PropTypes.func,
        defaultSelectFirstRow: React.PropTypes.bool,
        selectedRow: React.PropTypes.object
    },
    getInitialState: function () {
        return {
            data: null,
            attrs: this.props.attrs,
            sort: this.props.sort,
            start: 0,
            maxRows: this.props.maxRows
        };
    },
    getDefaultProps: function () {
        return {
            attrs: {'*': null},
            maxRows: 20,
            onRowAdd: function () {
            },
            onRowEdit: function () {
            },
            onRowDelete: function () {
            },
            onRowInspect: null,
            enableAdd: true,
            enableEdit: true,
            enableDelete: true,
            enableInspect: true,
            enableExecute: false,
            onRowClick: null,
            onExecute: null,
            onSort: function () {
            },
            defaultSelectFirstRow: false,
            selectedRow: null
        };
    },
    componentDidMount: function () {
        this._refreshData(this.props, this.state);
    },
    componentWillReceiveProps: function (nextProps) {
        // reset pager on query change
        var nextState = _.extend({}, this.state, {attrs: nextProps.attrs});
        if (JSON.stringify(this.props.query) !== JSON.stringify(nextProps.query)) {
            _.extend(nextState, {start: 0});
        }
        this._refreshData(nextProps, nextState);
    },
    render: function () {
        if (this.state.data === null) {
            return Spinner(); // entity not available yet
        }

        var editable = this.state.data.meta.permissions.indexOf("UPDATE_DATA") >= 0;
        var creatable = this.state.data.meta.permissions.indexOf("ADD_DATA") >= 0;
        var deletable = this.state.data.meta.permissions.indexOf("DELETE_DATA") >= 0;

        var TableHeader = TableHeaderFactory({
            entity: this.state.data.meta,
            attrs: this.state.attrs,
            sort: this.state.sort,
            enableAdd: creatable && this.props.enableAdd === true,
            enableEdit: editable && this.props.enableEdit === true,
            enableDelete: deletable && this.props.enableDelete === true,
            enableInspect: this.props.enableInspect === true && this.props.onRowInspect !== null,
            enableExecute: this.props.enableExecute === true && this.props.onExecute != null,
            onSort: this._handleSort,
            onExpand: this._handleExpand,
            onCollapse: this._handleCollapse,
            onCreate: this._handleCreate,
            onExecute: this.props.onExecute,
            onAddClick: this.props.onAddClick
        });

        var TableBody = TableBodyFactory({
            data: this.state.data,
            attrs: this.state.attrs,
            enableEdit: editable && this.props.enableEdit === true,
            enableDelete: deletable && this.props.enableDelete === true,
            enableInspect: this.props.enableInspect === true && this.props.onRowInspect !== null,
            enableExecute: this.props.enableExecute === true && this.props.onExecute != null,
            onEdit: this._handleEdit,
            onDelete: this._handleDelete,
            onRowInspect: this.props.onRowInspect,
            onRowClick: this.props.onRowClick,
            onExecute: this.props.onExecute,
            selectedRow: this.props.selectedRow,
            onEditClick: this.props.onEditClick
        });

        var className = 'table table-striped table-condensed table-bordered molgenis-table';

        if (this.props.onRowClick !== null)
            className = className + " table-hover";

        return (
            div(null,
                div({className: 'molgenis-table-container'},
                    table({className: className},
                        TableHeader,
                        TableBody
                    )
                ),
                div({className: 'row'},
                    div({className: 'col-md-3 form-inline'},
                        div({
                                'className': 'form-group'
                            },
                            label(null, "Rows per page: " + String.fromCharCode(160)),
                            SelectBox({
                                options: [
                                    {value: 10, text: 10},
                                    {value: 20, text: 20},
                                    {value: 30, text: 30},
                                    {value: 50, text: 50},
                                    {value: 100, text: 100}
                                ],
                                value: 20,
                                onChange: this._handleRowsPerPageChange
                            })
                        )
                    ),
                    div({className: 'col-md-6'},
                        div({className: 'text-center'},
                            Pager({
                                nrItems: this.state.data.total,
                                nrItemsPerPage: this.state.maxRows,
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
    _refreshData: function (props, state) {
        var opts = {
            attrs: {'~id': null}, // always include the id attribute
            num: state.maxRows
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
                'orders': [{
                    'attr': state.sort.attr.name,
                    'direction': state.sort.order
                }]
            };
        }
        if (state.start !== 0) {
            opts.start = state.start;
        }
        api.get(props.entity, opts).done(function (data) {
            var newState = _.extend({}, state, {data: data});
            if (this.isMounted()) {
                if (this.props.onRowClick !== null && this.props.defaultSelectFirstRow === true && this.props.selectedRow === null && data && data.items && data.items.length > 0) {
                    this.props.onRowClick(data.items[0]);
                }
                this.setState(newState);
            }
        }.bind(this));
    },
    _handleExpand: function (e) {
        var attrs = JSON.parse(JSON.stringify(this.state.attrs)); // deep clone

        for (var i = 0, attrsAtDepth = attrs; i < e.attrPath.length; ++i) {
            var attr = e.attrPath[i];
            if (!attrsAtDepth[attr]) {
                attrsAtDepth[attr] = (i < e.attrPath.length - 1) ? {} : {'*': null};
            }
            attrsAtDepth = attrsAtDepth[attr];
        }

        this._refreshData(this.props, _.extend({}, this.state, {attrs: attrs}));
    },
    _handleCollapse: function (e) {
        var attrs = _.extend({}, this.state.attrs);

        for (var i = 0, attrsAtDepth = attrs; i < e.attrPath.length; ++i) {
            var attr = e.attrPath[i];
            if (i < e.attrPath.length - 1) {
                attrsAtDepth = attrsAtDepth[attr];
            } else {
                attrsAtDepth[attr] = null;
            }
        }

        this._refreshData(this.props, _.extend({}, this.state, {attrs: attrs}));
    },
    _handleCreate: function () {
        this._resetTable();
        this.props.onRowAdd();
    },
    _handleEdit: function () {
        this._resetTable();
        this.props.onRowEdit();
    },
    _handleDelete: function () {
        this._resetTable();
        this.props.onRowDelete();
    },
    _resetTable: function () {
        this._refreshData(this.props, _.extend({}, this.state, {start: 0}));
    },
    _handleSort: function (e) {
        this._refreshData(this.props, _.extend({}, this.state, {sort: e}));
        this.props.onSort(e);
    },
    _handlePageChange: function (e) {
        this._refreshData(this.props, _.extend({}, this.state, {start: e.start}));
    },
    _handleRowsPerPageChange: function (e) {
        this._refreshData(this.props, _.extend({}, this.state, {
            start: 0,
            maxRows: parseInt(e.target.value)
        }));
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
        enableInspect: React.PropTypes.bool,
        enableExecute: React.PropTypes.bool,
        onAddClick: React.PropTypes.func
    },
    render: function () {
        return thead(null,
            tr(null,
                this._createHeaders(this.props.attrs)
            )
        );
    },
    _createHeaders: function (attrs) {
        var Headers = [];
        if (this.props.enableAdd === true) {
            Headers.push(th({className: 'compact', key: 'add'}, EntityCreateBtnFactory({
                entity: this.props.entity,
                onCreate: this.props.onCreate,
                onClick: this.props.onAddClick
            })));
        }
        if (this.props.enableAdd === false && this.props.enableEdit === true) {
            Headers.push(th({className: 'compact', key: 'edit'}));
        }
        if (this.props.enableDelete === true) {
            Headers.push(th({className: 'compact', key: 'delete'}));
        }
        if (this.props.enableInspect) {
            Headers.push(th({className: 'compact', key: 'inspect'}));
        }
        if (this.props.enableExecute) {
            Headers.push(th({className: 'compact', key: 'execute'}));
        }
        this._createHeadersRec(this.props.entity.attributes, attrs, Headers, [], false);
        return Headers;
    },
    _createHeadersRec: function (attrs, selectedAttrs, Headers, path, expanded) {
        if (_.size(selectedAttrs) > 0) {
            for (var i = 0; i < attrs.length; ++i) {
                if (attrs[i].visible === true) {
                    var attr = attrs[i];
                    if (this._isSelectedAttr(attr, selectedAttrs)) {
                        if (isCompoundAttr(attr)) {
                            // for a selected compound attribute select all child attributes (= the wildcard),
                            // child attributes might be expanded so include the selected attributes as well when recursing
                            this._createHeadersRec(attr.attributes, _.extend({'*': null}, selectedAttrs), Headers, path, expanded);
                        } else {
                            var attrPath = path.concat(attr.name);
                            if (this._isExpandedAttr(attr, selectedAttrs)) {
                                var EntityCollapseBtn = EntityCollapseBtnFactory({
                                    attrPath: attrPath,
                                    onCollapse: this.props.onCollapse
                                });
                                Headers.push(th({
                                    className: 'expanded-left compact',
                                    key: 'c' + attrPath.join()
                                }, EntityCollapseBtn));
                                this._createHeadersRec(attr.refEntity.attributes, selectedAttrs[attr.name], Headers, path.concat(attr.name), true);
                            }
                            else {
                                if (this._canExpandAttr(attr, path)) {
                                    var EntityExpandBtn = EntityExpandBtnFactory({
                                        attrPath: attrPath,
                                        onExpand: this.props.onExpand
                                    });
                                    Headers.push(th({
                                        className: 'compact',
                                        key: 'e' + attrPath.join()
                                    }, EntityExpandBtn));
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
    _getSortOrder: function (attr, path) {
        var sort = this.props.sort;
        return sort && this._getAttrId(sort.attr, sort.path) === this._getAttrId(attr, path) ? sort.order : null;
    },
    _getAttrId: function (attr, path) {
        return path.concat(attr.name).join('.');
    }
});
var TableHeaderFactory = React.createFactory(TableHeader);

/**
 * @memberOf component
 */
var TableHeaderCell = React.createClass({
    mixins: [DeepPureRenderMixin, AttrUtilsMixin],
    displayName: 'TableHeaderCell',
    propTypes: {
        attr: React.PropTypes.object.isRequired,
        path: React.PropTypes.array.isRequired,
        canSort: React.PropTypes.bool,
        sortOrder: React.PropTypes.oneOf(['asc', 'desc']),
        onSort: React.PropTypes.func,
        className: React.PropTypes.string
    },
    getDefaultProps: function () {
        return {
            sortOrder: null,
            onSort: function () {
            },
        };
    },
    render: function () {
        var SortIcon = this.props.sortOrder !== null ? Icon({
            style: {marginLeft: 5},
            name: this.props.sortOrder === 'asc' ? 'sort-by-alphabet' : 'sort-by-alphabet-alt'
        }) : null;

        var Label = this.props.attr.description ? span(null, Popover({
            value: this.props.attr.label,
            popoverValue: this.props.attr.description,
            trigger: 'hover'
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
    _handleSort: function (e) {
        this.props.onSort({
            attr: this.props.attr,
            path: this.props.path,
            order: this.props.sortOrder === null ? 'asc' : (this.props.sortOrder === 'asc' ? 'desc' : 'asc')
        });
    }
});
var TableHeaderCellFactory = React.createFactory(TableHeaderCell);

/**
 * @memberOf component
 */
var TableBody = React.createClass({
    mixins: [DeepPureRenderMixin, AttrUtilsMixin],
    displayName: 'TableBody',
    propTypes: {
        data: React.PropTypes.object.isRequired,
        attrs: React.PropTypes.object.isRequired,
        enableEdit: React.PropTypes.bool,
        enableDelete: React.PropTypes.bool,
        enableInspect: React.PropTypes.bool,
        enableExecute: React.PropTypes.bool,
        onEdit: React.PropTypes.func,
        onDelete: React.PropTypes.func,
        onRowInspect: React.PropTypes.func,
        onRowClick: React.PropTypes.func,
        onExecute: React.PropTypes.func,
        selectedRow: React.PropTypes.object,
        onEditClick: React.PropTypes.func,
    },
    getDefaultProps: function () {
        return {
            onEdit: function () {
            },
            onDelete: function () {
            },
            onRowInspect: function () {
            },
            onRowClick: function () {
            },
            onExecute: function () {
            },
            selectedRow: null
        };
    },
    render: function () {
        return tbody(null,
            this._createRows(this.props.data.meta)
        );
    },
    _createRows: function (entity) {
        var Rows = [];
        for (var i = 0; i < this.props.data.items.length; ++i) {
            var item = this.props.data.items[i];

            Rows.push(tr({
                key: '' + i,
                className: this.props.selectedRow && this.props.selectedRow.id === item.id ? 'info' : '',
                onClick: this.props.onRowClick !== null ? this.props.onRowClick.bind(null, item) : null
            }, this._createCols(item, entity)));
        }
        return Rows;
    },
    _createCols: function (item, entity) {
        var Cols = [];
        if (this.props.enableEdit === true) {
            var EntityEditBtn = EntityEditBtnFactory({
                name: entity.name,
                id: item[entity.idAttribute],
                onEdit: this.props.onEdit,
                onClick: this.props.onEditClick
            });
            Cols.push(td({className: 'compact', key: 'edit'}, EntityEditBtn));
        }
        if (this.props.enableDelete === true) {
            var EntityDeleteBtn = EntityDeleteBtnFactory({
                name: entity.name,
                id: item[entity.idAttribute],
                onDelete: this.props.onDelete
            });
            Cols.push(td({className: 'compact', key: 'delete'}, EntityDeleteBtn));
        }
        if (this.props.enableInspect === true && this.props.onRowInspect !== null) {
            var EntityInspectBtn = EntityInspectBtnFactory({
                name: entity.name,
                id: item[entity.idAttribute],
                onInspect: this.props.onRowInspect
            });
            Cols.push(td({className: 'compact', key: 'inspect'}, EntityInspectBtn));
        }
        if (this.props.enableExecute === true && this.props.onExecute !== null) {
            var EntityExecuteBtn = EntityExecuteBtnFactory({
                name: entity.name,
                id: item[entity.idAttribute],
                onExecute: this.props.onExecute
            });
            Cols.push(td({className: 'compact', key: 'execute'}, EntityExecuteBtn));
        }
        this._createColsRec(item, entity, entity.attributes, this.props.attrs, Cols, [], false, undefined);
        return Cols;
    },
    _getAttributeValues: function (item, attrName) {
        var result = null;
        if (item !== undefined && item !== null) {
            if (_.isArray(item)) {
                // We have passed an mref, the array contains one item for each value of the mref,
                // this item can be undefined or null
                result = _.map(item, function (row) {
                    if (row === undefined || row === null) {
                        return undefined;
                    } else {
                        return row[attrName];
                    }
                });
            } else {
                result = item[attrName];
            }
        }
        return result;
    },
    /**
     * Recursively creates the data rows for the data table body
     *
     * @param item One entire entity row for all columns (Object)
     * @param entity Metadata of the entity (Object)
     * @param attrs All attributes selected in the filter tree (array)
     * @param selectedAttrs All attributes selected in the filter tree (object)
     * @param Cols Array of React components
     * @param path Array of strings containing the names of attribute columns being created recursively at the moment
     * @param expanded boolean for reference types that are expanded or not
     * @param behindMref True if attribute is an MREF
     */
    _createColsRec: function (item, entity, attrs, selectedAttrs, Cols, path, expanded, behindMref) {
        if (_.size(selectedAttrs) > 0) {
            for (var j = 0; j < attrs.length; ++j) {
                var attr = attrs[j];
                if (this._isSelectedAttr(attr, selectedAttrs)) {
                    if (attr.visible === true) {
                        var attrPath = path.concat(attr.name);
                        if (isCompoundAttr(attr)) {
                            var expandedSelectedAttrs = $.extend({'*': null}, selectedAttrs)
                            this._createColsRec(item, entity, attr.attributes, expandedSelectedAttrs, Cols, path, expanded, behindMref);
                        } else {
                            if (this._isExpandedAttr(attr, selectedAttrs)) {
                                behindMref |= attr.fieldType === 'MREF' || attr.fieldType === 'CATEGORICAL_MREF' || attr.fieldType === 'ONE_TO_MANY';
                                Cols.push(td({className: 'expanded-left', key: attrPath.join()}));
                                this._createColsRec(this._getAttributeValues(item, attr.name), attr.refEntity, attr.refEntity.attributes, selectedAttrs[attr.name], Cols, attrPath, true, behindMref);
                            } else {
                                if (this._canExpandAttr(attr, path)) {
                                    Cols.push(td({key: 'e' + attrPath.join()}));
                                }
                                var TableCell = TableCellFactory({
                                    className: j === attrs.length - 1 && expanded ? 'expanded-right' : undefined,
                                    entity: entity,
                                    attr: attr,
                                    value: this._getAttributeValues(item, attr.name),
                                    expanded: expanded,
                                    onEdit: this.props.onEdit,
                                    key: attrPath.join(),
                                    behindMref: behindMref
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
        onEdit: React.PropTypes.func,
        behindMref: React.PropTypes.bool
    },
    shouldComponentUpdate: function (nextProps, nextState) {
        return !_.isEqual(this.state, nextState) || !_.isEqual(this.props.entity.name, nextProps.entity.name)
            || !_.isEqual(this.props.attr.name, nextProps.attr.name) || !_.isEqual(this.props.value, nextProps.value);
    },
    render: function () {
        var CellContentBlocks;
        // If we are behind an expanded mref, the value is an array
        if (this.props.expanded && _.isArray(this.props.value) && this.props.behindMref) {
            CellContentBlocks = _.flatten(_.map(this.props.value, function (value, i) {
                if (value !== null && value !== undefined) {
                    var CellContentForValue = this._createTableCellContent(value, 'c' + i);
                    if ((i < this.props.value.length - 1) && this.props.attr.fieldType !== 'FILE') {
                        return [CellContentForValue, br({key: 'b' + i})];
                    }
                    return CellContentForValue;
                } else {
                    return br();
                }
            }.bind(this)));
        } else {
            CellContentBlocks = this.props.value !== null && this.props.value !== undefined ? [this._createTableCellContent(this.props.value)] : [];
        }

        return td({className: this.props.className}, CellContentBlocks);
    },
    _createTableCellContent: function (value, key) {
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
    mixins: [AttrUtilsMixin, ReactLayeredComponentMixin],
    displayName: 'TableCellContent',
    propTypes: {
        entity: React.PropTypes.object.isRequired,
        attr: React.PropTypes.object.isRequired,
        value: React.PropTypes.any,
        className: React.PropTypes.string,
        onEdit: React.PropTypes.func
    },
    getDefaultProps: function () {
        return {
            onEdit: function () {
            }
        };
    },
    getInitialState: function () {
        return {
            showRef: false
        };
    },
    shouldComponentUpdate: function (nextProps, nextState) {
        return !_.isEqual(this.state, nextState) || !_.isEqual(this.props.entity.name, nextProps.entity.name)
            || !_.isEqual(this.props.attr.name, nextProps.attr.name) || !_.isEqual(this.props.value, nextProps.value);
    },
    render: function () {
        return this._createValue(this.props.value);
    },
    renderLayer: function () {
        if (this.state.showRef) {
            var refEntity = this.props.attr.refEntity;

            var operator, value;
            if (isXrefAttr(this.props.attr)) {
                operator = 'EQUALS';
                value = this.props.value[refEntity.idAttribute];
            } else {
                operator = 'IN';
                value = _.map(this.props.value, function (item) {
                    return item[refEntity.idAttribute];
                });
            }

            var nestedTable = TableFactory({
                entity: this.props.attr.refEntity.name,
                query: {
                    'q': [{
                        'field': refEntity.idAttribute,
                        'operator': operator,
                        'value': value
                    }]
                },
                enableAdd: false,
                enableDelete: false,
                enableInspect: false,
                onRowEdit: function (e) {
                    this.props.onEdit(e);
                }.bind(this)
            });

            var OkBtn = (
                div({className: 'row', style: {textAlign: 'right'}},
                    div({className: 'col-md-12'},
                        Button({text: 'Ok', style: 'primary', onClick: this._toggleModal.bind(null, false)}, 'Ok')
                    )
                )
            );

            return Modal({
                title: this.props.attr.label,
                show: true,
                onHide: this._toggleModal.bind(null, false)
            }, nestedTable, OkBtn);
        } else {
            return null;
        }
    },
    _createValue: function (value) {
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
                    if (attr.expression) {
                        // computed refs refer to entities that only exist within the context of entity that refers to them
                        CellContent = span(null, value[attr.refEntity.labelAttribute]);
                    } else {
                        CellContent = a({
                            href: '#',
                            onClick: this._toggleModal.bind(null, true)
                        }, span(null, value[attr.refEntity.labelAttribute]));
                    }
                    break;
                case 'FILE':
                    CellContent = (
                        div(null,
                            a({
                                href: '#',
                                onClick: this._toggleModal.bind(null, true)
                            }, span(null, value[attr.refEntity.labelAttribute])),
                            ' ',
                            a({href: value['url']},
                                Icon({
                                    name: 'download',
                                    style: {cursor: 'pointer'}
                                })
                            )
                        )
                    );
                    break;
                case 'CATEGORICAL_MREF':
                case 'MREF':
                case 'ONE_TO_MANY':
                    CellContent = (
                        span(null,
                            _.flatten(_.map(value, function (item, i) {
                                var Element;
                                if (attr.expression) {
                                    // computed refs refer to entities that only exist within the context of entity that refers to them
                                    Element = span(null, item[attr.refEntity.labelAttribute]);
                                } else {
                                    Element = a({
                                        href: '#',
                                        onClick: this._toggleModal.bind(null, true),
                                        key: 'a' + i
                                    }, span(null, item[attr.refEntity.labelAttribute]));
                                }
                                var Seperator = i < value.length - 1 ? span({key: 's' + i}, ',') : null;
                                return [Element, Seperator];
                            }.bind(this)))
                        )
                    );
                    break;
                case 'DATE':
                    CellContent = span(null, moment(value).format('ll'));
                    break;
                case 'DATE_TIME':
                    CellContent = span(null, moment(value).format('lll'));
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
                    if (value.length > maxLength) {
                        CellContent = span(null, Popover({
                            value: value.substring(0, maxLength - 3) + '...',
                            popoverValue: value,
                            trigger: 'hover'
                        }));
                    } else {
                        CellContent = span(null, value);
                    }
                    break;
                default:
                    CellContent = span(null, value);
                    break;
            }
        }

        return CellContent;
    },
    _toggleModal: function (show) {
        this.setState({
            showRef: show
        });
    }
});
var TableCellContentFactory = React.createFactory(TableCellContent);

/**
 * @memberOf component
 */
var EntityExpandBtn = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'EntityExpandBtn',
    propTypes: {
        attrPath: React.PropTypes.array.isRequired,
        onExpand: React.PropTypes.func,
    },
    getDefaultProps: function () {
        return {
            onExpand: function () {
            }
        };
    },
    render: function () {
        return Button({
            icon: 'expand',
            size: 'xsmall',
            title: 'Expand entity',
            onClick: this._handleExpand
        });
    },
    _handleExpand: function () {
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
    mixins: [DeepPureRenderMixin],
    displayName: 'EntityCollapseBtn',
    propTypes: {
        attrPath: React.PropTypes.array.isRequired,
        onCollapse: React.PropTypes.func,
    },
    getDefaultProps: function () {
        return {
            onCollapse: function () {
            }
        };
    },
    render: function () {
        return Button({
            icon: 'collapse-up',
            size: 'xsmall',
            title: 'Collapse entity',
            onClick: this._handleCollapse
        });
    },
    _handleCollapse: function () {
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
    mixins: [DeepPureRenderMixin, ReactLayeredComponentMixin],
    displayName: 'EntityCreateBtn',
    propTypes: {
        entity: React.PropTypes.object.isRequired,
        onCreate: React.PropTypes.func,
        onClick: React.PropTypes.func
    },
    getInitialState: function () {
        return {
            form: false
        };
    },
    getDefaultProps: function () {
        return {
            onCreate: function () {
            }
        };
    },
    render: function () {
        var btnProps = {
            icon: 'plus',
            title: 'Add row',
            style: 'success',
            size: 'xsmall'
        }
        if (this.props.onClick != null) {
            // Wrap supplied click handler in closure binding props of handler context
            var clickHandler = this.props.onClick
            var tableId = this.props.entity.name
            btnProps.onClick = function () {
                clickHandler(tableId, null)
            }
        } else {
            // Use default handler if no handler was passed in
            btnProps.onClick = this._handleCreate
        }
        return Button(btnProps)
    },
    renderLayer: function () {
        return this.state.form ? Form({
            entity: this.props.entity.name,
            mode: 'create',
            showHidden: true,
            modal: true,
            onSubmitSuccess: this._handleCreateConfirm,
            onSubmitCancel: this._handleCreateCancel
        }) : null;
    },
    _handleCreate: function () {
        this.setState({
            form: true
        });
    },
    _handleCreateCancel: function () {
        this.setState({
            form: false
        });
    },
    _handleCreateConfirm: function (e) {
        this.setState({
            form: false
        });
        this.props.onCreate({
            href: this.props._href
        });
    }
});
var EntityCreateBtnFactory = React.createFactory(EntityCreateBtn);

/**
 * @memberOf component
 */
var EntityEditBtn = React.createClass({
    mixins: [DeepPureRenderMixin, ReactLayeredComponentMixin],
    displayName: 'EntityEditBtn',
    propTypes: {
        name: React.PropTypes.string.isRequired,
        id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
        onEdit: React.PropTypes.func,
        onClick: React.PropTypes.func
    },
    getInitialState: function () {
        return {
            form: false
        };
    },
    getDefaultProps: function () {
        return {
            onEdit: function () {},
            onClick: null
        };
    },
    render: function () {
        var btnProps = {
            icon: 'edit',
            title: 'Edit row',
            size: 'xsmall'
        }

        if (this.props.onClick != null) {
            // Wrap supplied click handler in closure binding props of handler context
            var clickHandler = this.props.onClick;
            var tableId = this.props.name;
            var rowId = this.props.id;
            btnProps.onClick = function () {
                clickHandler(tableId, rowId)
            }
        } else {
            // Use default handler if no handler was passed in
            btnProps.onClick = this._handleEdit;
        }

        return Button(btnProps);
    },
    renderLayer: function () {
        return this.state.form ? Form({
            entity: this.props.name,
            entityInstance: this.props.id,
            mode: 'edit',
            showHidden: true,
            modal: true,
            onSubmitSuccess: this._handleEditConfirm,
            onSubmitCancel: this._handleEditCancel
        }) : null;
    },
    _handleEdit: function () {
        this.setState({
            form: true
        });
    },
    _handleEditCancel: function () {
        this.setState({
            form: false
        });
    },
    _handleEditConfirm: function () {
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
    mixins: [DeepPureRenderMixin, ReactLayeredComponentMixin],
    displayName: 'EntityDeleteBtn',
    propTypes: {
        name: React.PropTypes.string.isRequired,
        id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
        onDelete: React.PropTypes.func
    },
    getInitialState: function () {
        return {
            dialog: false
        };
    },
    getDefaultProps: function () {
        return {
            onDelete: function () {
            }
        };
    },
    render: function () {
        return Button({
            icon: 'trash',
            title: 'Delete row',
            style: 'danger',
            size: 'xsmall',
            onClick: this._handleDelete
        });
    },
    renderLayer: function () {
        return this.state.dialog ? Dialog({
            type: 'confirm',
            message: 'Are you sure you want to delete this row?',
            onCancel: this._handleDeleteCancel,
            onConfirm: this._handleDeleteConfirm
        }) : null;
    },
    _handleDelete: function () {
        this.setState({
            dialog: true
        });
    },
    _handleDeleteCancel: function () {
        this.setState({
            dialog: false
        });
    },
    _handleDeleteConfirm: function () {
        this.setState({
            dialog: false
        });
        api.remove(this.props.name, this.props.id).done(function () {
            this.props.onDelete({
                name: this.props.name,
                id: this.props.id
            });
        }.bind(this));
    },
});
var EntityDeleteBtnFactory = React.createFactory(EntityDeleteBtn);

/**
 * @memberOf component
 */
var EntityInspectBtn = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'EntityInspectBtn',
    propTypes: {
        name: React.PropTypes.string.isRequired,
        id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
        onInspect: React.PropTypes.func
    },
    getInitialState: function () {
        return {
            dialog: false
        };
    },
    getDefaultProps: function () {
        return {
            onInspect: function () {
            }
        };
    },
    render: function () {
        return Button({
            icon: 'search',
            style: 'info',
            title: 'Inspect row',
            size: 'xsmall',
            onClick: this._handleClick
        });
    },
    _handleClick: function () {
        this.props.onInspect({
            name: this.props.name,
            id: this.props.id
        });
    }
});
var EntityInspectBtnFactory = React.createFactory(EntityInspectBtn);

/**
 * @memberOf component
 */
var EntityExecuteBtn = React.createClass({
    mixins: [DeepPureRenderMixin],
    displayName: 'EntityExecuteBtn',
    propTypes: {
        name: React.PropTypes.string.isRequired,
        id: React.PropTypes.oneOfType([React.PropTypes.string, React.PropTypes.number]).isRequired,
        onExecute: React.PropTypes.func
    },
    getDefaultProps: function () {
        return {
            onExecute: function () {
            }
        };
    },
    render: function () {
        return Button({
            icon: 'play',
            style: 'success',
            title: 'Execute now',
            size: 'xsmall',
            onClick: this._handleClick
        });
    },
    _handleClick: function () {
        this.props.onExecute({
            name: this.props.name,
            id: this.props.id
        });
    }
});
var EntityExecuteBtnFactory = React.createFactory(EntityExecuteBtn);

export {Table};
const TableFactory = React.createFactory(Table);
export default TableFactory;
