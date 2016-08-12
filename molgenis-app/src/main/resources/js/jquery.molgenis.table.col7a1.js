(function ($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();

    /**
     * @memberOf molgenis.table
     */
    function createTable(settings) {
        // create elements
        var items = [];
        items.push('<div class="row">');
        items.push('<div class="col-md-12">');
        items.push('<div class="molgenis-table-container" style="min-height: 0%">');
        /* workaround for IE9 bug https://github.com/molgenis/molgenis/issues/2755 */
        if (settings.rowClickable) {
            items.push('<table class="table table-striped table-condensed molgenis-table table-hover"><thead></thead><tbody></tbody></table>');
        } else {
            items.push('<table class="table table-striped table-condensed molgenis-table"><thead><th></th></thead><tbody></tbody></table>');
        }
        items.push('</div>');
        items.push('</div>');
        items.push('</div>');
        items.push('<div class="row">');
        items.push('<div class="col-md-3"><div class="molgenis-table-controls">');
        if (settings.editable) {
            items.push('<a class="btn btn-default btn-primary edit-table-btn" href="#" data-toggle="button" title="Edit"><span class="glyphicon glyphicon-edit"></span></a>');
            items.push('<a class="btn btn-default btn-success add-row-btn" style="display: none" href="#" data-toggle="button" title="Add row"><span class="glyphicon glyphicon-plus"></span></a>');
        }

        items.push('</div></div>');
        items.push('<div class="col-md-6"><div class="molgenis-table-pager"></div></div>');
        items.push('<div class="col-md-3"><div class="molgenis-table-info pull-right"></div></div>');
        items.push('</div>');
        settings.container.html(items.join(''));

        // add data to elements
        getTableMetaData(settings, function (attributes, refEntitiesMeta) {
            var visibleAttributes = [];
            for (var i = 0; i < attributes.length; ++i) {
                if (attributes[i].visible) {
                    visibleAttributes.push(attributes[i]);
                }
            }

            settings.colAttributes = visibleAttributes;
            settings.refEntitiesMeta = refEntitiesMeta;

            getTableData(settings, function (data) {
                createTableHeader(settings);
                createTableBody(data, settings);
                createTablePager(data, settings);
                createTableFooter(data, settings);
            });
        });
    }

    /**
     * @memberOf molgenis.table
     */
    function getTableMetaData(settings, callback) {
        if (settings.attributes && settings.attributes.length > 0) {
            var colAttributes = molgenis.getAtomicAttributes(settings.attributes, restApi);
            // get meta data for referenced entities
            var refEntitiesMeta = {};
            $.each(colAttributes, function (i, attribute) {
                if (attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'CATEGORICAL_MREF') {
                    refEntitiesMeta[attribute.refEntity.href] = null;
                }
            });

            var dfds = [];
            $.each(refEntitiesMeta, function (entityHref) {
                dfds.push($.Deferred(function (dfd) {
                    restApi.getAsync(entityHref, {'expand': ['attributes']}, function (entityMeta) {
                        refEntitiesMeta[entityHref] = entityMeta;
                        dfd.resolve();
                    });
                }).promise());
            });

            // build table after all meta data for referenced entities was loaded
            $.when.apply($, dfds).done(function () {
                // inject referenced entities meta data in attributes
                $.each(colAttributes, function (i, attribute) {
                    if (attribute.fieldType === 'XREF' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'CATEGORICAL_MREF') {
                        attribute.refEntity = refEntitiesMeta[attribute.refEntity.href];
                    }
                });
                callback(colAttributes, refEntitiesMeta);
            });
        } else {
            callback([], {});
        }
    }

    /**
     * @memberOf molgenis.table
     */
    function getTableData(settings, callback) {
        var attributeNames = $.map(settings.colAttributes, function (attribute) {
            if (attribute.visible) {
                return attribute.name;
            }
        });
        var expandAttributeNames = $.map(settings.colAttributes, function (attribute) {
            if (attribute.expression) {
                if (attribute.visible) {
                    return attribute.name;
                }
            }
            if (attribute.fieldType === 'XREF' || attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'MREF' || attribute.fieldType === 'CATEGORICAL_MREF') {
                // partially expand reference entities (only request label attribute)
                var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
                if (attribute.visible) {
                    return attribute.name + '[' + refEntity.labelAttribute + ']';
                }
            }
            return null;
        });

        // TODO do not construct uri from other uri
        var entityCollectionUri = settings.entityMetaData.href.replace("/meta", "");
        if (settings.query) {
            var q = $.extend({}, settings.query, {
                'start': settings.start,
                'num': settings.maxRows,
                'sort': settings.sort
            });
            restApi.getAsync(entityCollectionUri, {
                'attributes': attributeNames,
                'expand': expandAttributeNames,
                'q': q
            }, function (data) {
                settings.data = data;
                callback(data);
            });
        } else {
            // don't query but use the predefined value
            settings.data = settings.value;
            callback(settings.value);
        }
    }

    /**
     * @memberOf molgenis.table
     */
    function createTableHeader(settings) {
        var container = $('.molgenis-table thead', settings.container);

        var items = [];
        if (settings.editenabled) {
            items.push($('<th>'));
        }

        $.each(settings.colAttributes, function (i, attribute) {
            var header;
            if (attribute.visible) {
                if (!endsWith(attribute.name, " 2")) {
                    if (endsWith(attribute.name, " 1")) {
                        header = $('<th>' + attribute.name.replace(" 1", "") + '</th>');
                    }
                    else {
                        if (settings.sort && settings.sort.orders[0].property === attribute.name) {
                            if (settings.sort.orders[0].direction === 'ASC') {
                                header = $('<th>' + attribute.label + '<span data-attribute="' + attribute.name
                                    + '" class="ui-icon ui-icon-triangle-1-s down"></span></th>');
                            } else {
                                header = $('<th>' + attribute.label + '<span data-attribute="' + attribute.name
                                    + '" class="ui-icon ui-icon-triangle-1-n up"></span></th>');
                            }
                        } else {
                            header = $('<th>' + attribute.label + '<span data-attribute="' + attribute.name
                                + '" class="ui-icon ui-icon-triangle-2-n-s updown"></span></th>');
                        }
                    }
                    header.data('attr', attribute);
                    items.push(header);
                }
            }
        });
        container.html(items);
    }

    /**
     * @memberOf molgenis.table
     */
    function createTableBody(data, settings) {
        var container = $('.molgenis-table tbody', settings.container);
        var items = [];
        var tabindex = 1;
        for (var i = 0; i < data.items.length; ++i) {
            var entity = data.items[i];
            var row = $('<tr>').data('entity', entity).data('id', entity.href);
            $.each(settings.colAttributes, function (i, attribute) {
                if (!endsWith(attribute.name, " 2")) {
                    var cell = $('<td style="vertical-align: middle;">').data('id', entity.href + '/' + encodeURIComponent(attribute.name));
                    renderCell(cell, entity, attribute, settings);
                    if (settings.editenabled) {
                        cell.attr('tabindex', tabindex++);
                    }
                    row.append(cell);
                }
            });
            items.push(row);
        }
        container.html(items);

        $('.show-popover').popover({trigger: 'hover', placement: 'bottom', container: 'body'});
    }

    /**
     * @memberOf molgenis.table.cell
     */
    function renderCell(cell, entity, attribute, settings) {
        renderViewCell(cell, entity, attribute, settings);
    }

    /**
     * @memberOf molgenis.table.cell
     */
    function renderViewCell(cell, entity, attribute, settings) {
        cell.empty();

        if (!endsWith(attribute.name, " 2")) {
            var rawValue = entity[attribute.name];
            renderSingleViewCell(cell, rawValue, attribute, settings);
            if (endsWith(attribute.name, " 1")) {
                cell.append("<br>");
                var rawValue2 = entity[attribute.name.replace(" 1", " 2")];
                renderSingleViewCell(cell, rawValue2, attribute, settings);
            }
        }
    }

    function renderSingleViewCell(cell, rawValue, attribute, settings) {
        switch (attribute.fieldType) {
            case 'XREF':
            case 'MREF':
            case 'CATEGORICAL':
            case 'CATEGORICAL_MREF':
                if (rawValue) {
                    var refEntity = settings.refEntitiesMeta[attribute.refEntity.href];
                    var refAttribute = refEntity.labelAttribute;
                    var refValue = refEntity.attributes[refAttribute];

                    if (refValue) {
                        var refAttributeType = refValue.fieldType;
                        if (refAttributeType === 'XREF' || refAttributeType === 'MREF' || refAttributeType === 'CATEGORICAL' || refAttributeType === 'CATEGORICAL_MREF' || refAttributeType === 'COMPOUND') {
                            throw 'unsupported field type ' + refAttributeType;
                        }

                        switch (attribute.fieldType) {
                            case 'CATEGORICAL':
                            case 'XREF':
                                var $cellValue = $('<a href="#">').append(formatTableCellValue(rawValue[refAttribute], refAttributeType));
                                $cellValue.click(function (event) {
                                    openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
                                    event.stopPropagation();
                                });
                                cell.append($cellValue);
                                break;
                            case 'CATEGORICAL_MREF':
                            case 'MREF':
                                $.each(rawValue.items, function (i, rawValue) {
                                    var $cellValuePart = $('<a href="#">').append(formatTableCellValue(rawValue[refAttribute], refAttributeType));
                                    $cellValuePart.click(function (event) {
                                        openRefAttributeModal(attribute, refEntity, refAttribute, rawValue);
                                        event.stopPropagation();
                                    });
                                    if (i > 0) {
                                        cell.append(',');
                                    }
                                    cell.append($cellValuePart);
                                });
                                break;
                            default:
                                throw 'unexpected field type ' + attribute.fieldType;
                        }
                    }
                }
                break;
            case 'BOOL':
                cell.append(formatTableCellValue(rawValue, attribute.fieldType, undefined, attribute.nillable));
                break;
            default :
                cell.append(formatTableCellValue(rawValue, attribute.fieldType));
                break;
        }
    }

    /**
     * @memberOf molgenis.table
     */
    function openRefAttributeModal(attribute, refEntity, refAttribute, refValue) {
        // create modal structure
        var modal = $('#table-ref-modal');
        if (!modal.length) {
            var items = [];
            items.push('<div class="modal" id="table-ref-modal" tabindex="-1" aria-labelledby="table-ref-modal-label" aria-hidden="true">');
            items.push('<div class="modal-dialog">');
            items.push('<div class="modal-content">');
            items.push('<div class="modal-header">');
            items.push('<button type="button" class="close" data-dismiss="modal"><span aria-hidden="true">&times;</span><span class="sr-only">Close</span></button>');
            items.push('<h4 class="modal-title ref-title" id="table-ref-modal-label">Sign up</h4>');
            items.push('</div>');
            items.push('<div class="modal-body">');
            items.push('<legend class="ref-description-header"></legend>');
            items.push('<p class="ref-description"></p>');
            items.push('<legend>Data</legend>');
            items.push('<div class="ref-table"></div>');
            items.push('</div>');
            items.push('<div class="modal-footer">');
            items.push('<a href="#" class="btn btn-primary filter-apply-btn" data-dismiss="modal">Ok</a>');
            items.push('</div>');
            items.push('</div>');
            modal = $(items.join(''));
        }

        // inject modal data
        var refAttributes = molgenis.getAtomicAttributes(refEntity.attributes, restApi);
        var val = restApi.get(refValue.href)[refEntity.idAttribute];

        var refQuery = {
            'q': [{
                'field': refEntity.idAttribute,
                'operator': 'EQUALS',
                'value': val
            }]
        };

        $('.ref-title', modal).html(attribute.label || attribute.name);
        $('.ref-description-header', modal).html((refEntity.label || refEntity.name) + ' description');
        $('.ref-description', modal).html(refEntity.description || 'No description available');
        if (attribute.expression) {
            // computed attribute, don't query but show the computed value
            $('.ref-table', modal).table({
                'entityMetaData': refEntity,
                'attributes': refAttributes,
                'value': {items: [refValue], total: 1}
            });
        } else {
            $('.ref-table', modal).table({'entityMetaData': refEntity, 'attributes': refAttributes, 'query': refQuery});
        }

        // show modal
        modal.modal({'show': true});
    }

    /**
     * @memberOf molgenis.table
     */
    function createTablePager(data, settings) {
        var container = $('.molgenis-table-pager', settings.container);

        if (data.total > settings.maxRows) {
            container.pager({
                'nrItems': data.total,
                'nrItemsPerPage': settings.maxRows,
                'onPageChange': function (page) {
                    settings.start = page.start;
                    getTableData(settings, function (data) {
                        createTableBody(data, settings);
                    });
                }
            });
            container.show();
        } else {
            container.hide();
        }
    }

    function refresh(settings) {
        getTableData(settings, function (data) {
            createTableBody(data, settings);
            createTablePager(data, settings);
            createTableFooter(data, settings);
        });
    }

    /**
     * @memberOf molgenis.table
     */
    function createTableFooter(data, settings) {
        var container = $('.molgenis-table-info', settings.container);
        container.html(data.total + ' item' + (data.total !== 1 ? 's' : '') + ' found');
    }

    $.fn.table = function (options) {
        var container = this;

        // call plugin method
        if (typeof options == 'string') {
            var args = Array.prototype.slice.call(arguments, 1);
            if (args.length === 0)
                return container.data('table')[options]();
            else if (args.length === 1)
                return container.data('table')[options](args[0]);
        }

        // create tree container
        var settings = $.extend({}, $.fn.table.defaults, options, {'container': container});

        // store tree settings
        container.off();
        container.empty();
        container.data('settings', settings);

        // plugin methods
        container.data('table', {
            'setAttributes': function (attributes) {
                settings.attributes = attributes;

                // add data to elements
                getTableMetaData(settings, function (attributes, refEntitiesMeta) {
                    settings.colAttributes = attributes;
                    settings.refEntitiesMeta = refEntitiesMeta;

                    getTableData(settings, function (data) {
                        createTableHeader(settings);
                        createTableBody(data, settings);
                    });
                });
            },
            'setQuery': function (query) {
                settings.query = query;
                settings.start = 0;
                refresh(settings);
            },
            'getQuery': function () {
                return settings.query;
            },
            'getSort': function () {
                return settings.sort;
            }
        });

        createTable(settings, function () {
            if (settings.onInit) {
                settings.onInit();
            }
        });

        // sort column ascending/descending
        $(container).on('click', 'thead th .ui-icon', function (e) {
            e.preventDefault();

            var attributeName = $(this).data('attribute');
            if (settings.sort) {
                var order = settings.sort.orders[0];
                order.property = attributeName;
                order.direction = order.direction === 'ASC' ? 'DESC' : 'ASC';
            } else {
                settings.sort = {
                    orders: [{
                        property: attributeName,
                        direction: 'ASC'
                    }]
                };
            }

            var classUp = 'ui-icon-triangle-1-n up', classDown = 'ui-icon-triangle-1-s down', classUpDown = 'ui-icon-triangle-2-n-s updown';
            $('thead th .ui-icon', container).not(this).removeClass(classUp + ' ' + classDown).addClass(classUpDown);
            if (settings.sort.orders[0].direction === 'ASC') {
                $(this).removeClass(classUpDown + ' ' + classUp).addClass(classDown);
            } else {
                $(this).removeClass(classUpDown + ' ' + classDown).addClass(classUp);
            }

            getTableData(settings, function (data) {
                createTableBody(data, settings);
            });
        });


        $(container).on('click', '.molgenis-table.table-hover tbody:not(.editable) tr', function (e) {
            // Issue #1400 ask for IdAttribute directly
            var entityData = $(this).data('entity').href.split('/');
            var entityId = decodeURIComponent(entityData.pop());
            var entityName = decodeURIComponent(entityData.pop());

            $('#entityReport').load("dataexplorer/details", {entityName: entityName, entityId: entityId}, function () {
                $('#entityReportModal').modal("show");

                // Button event handler when a button is placed inside an entity report ftl
                $(".modal-body button", "#entityReport").on('click', function () {
                    $.download($(this).data('href'), {entityName: entityName, entityId: entityId}, "GET");
                });
            });
        });

        return this;
    };

    // default tree settings
    $.fn.table.defaults = {
        'entityMetaData': null,
        'maxRows': 20,
        'attributes': null,
        'query': null,
        'editable': false,
        'rowClickable': false,
        'onDataChange': function () {
        }
    };

    function endsWith(str, suffix) {
        return str.indexOf(suffix, str.length - suffix.length) !== -1;
    }

    var container = $('#feature-selection');
    var fancy = $('.molgenis-tree', container).fancytree("getTree");
    $(fancy.getNodeByKey("/api/v1/Patients/meta/cDNA%20change%202").li.getElementsByClassName("fancytree-checkbox"))[0].outerHTML = "<span>&nbsp&nbsp&nbsp&nbsp&nbsp</span>";
    $(fancy.getNodeByKey("/api/v1/Patients/meta/Protein%20change%202").li.getElementsByClassName("fancytree-checkbox"))[0].outerHTML = "<span>&nbsp&nbsp&nbsp&nbsp&nbsp</span>";
    $(fancy.getNodeByKey("/api/v1/Patients/meta/Exon%2FIntron%202").li.getElementsByClassName("fancytree-checkbox"))[0].outerHTML = "<span>&nbsp&nbsp&nbsp&nbsp&nbsp</span>";
    $(fancy.getNodeByKey("/api/v1/Patients/meta/Consequence%202").li.getElementsByClassName("fancytree-checkbox"))[0].outerHTML = "<span>&nbsp&nbsp&nbsp&nbsp&nbsp</span>";
}($, window.top.molgenis = window.top.molgenis || {}));