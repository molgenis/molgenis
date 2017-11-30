/**
 * Data module
 *
 * Dependencies: dataexplorer.js
 *
 * @param $
 * @param molgenis
 */
(function ($, molgenis) {
    "use strict";

    molgenis.dataexplorer = molgenis.dataexplorer || {};
    var self = molgenis.dataexplorer.data = molgenis.dataexplorer.data || {};
    self.createDataTable = createDataTable;
    self.createGenomeBrowser = createGenomeBrowser;
    self.doShowGenomeBrowser = doShowGenomeBrowser;
    self.setGenomeBrowserAttributes = setGenomeBrowserAttributes;
    self.setGenomeBrowserSettings = setGenomeBrowserSettings;
    self.setGenomeBrowserTracks = setGenomeBrowserTracks;

    var restApi = new molgenis.RestClient();
    var genomeBrowser;
    var genomeTracks;

    var genomebrowserStartAttribute;
    var genomebrowserChromosomeAttribute;
    var genomeBrowserSettings = {};

    var Table, tableSort;

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function setGenomeBrowserSettings(settings) {
        genomeBrowserSettings = settings;
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function setGenomeBrowserTracks(tracks) {
        genomeTracks = tracks;
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function createDataTable() {
        $.get('/permission/sys_FreemarkerTemplate/read').done(function (canRead) {
            Table = React.render(molgenis.ui.Table({
                entity: getEntity().name,
                attrs: getAttributesTree(),
                query: getQuery(),
                onRowAdd: onDataChange,
                onRowDelete: onDataChange,
                onRowEdit: onDataChange,
                onRowInspect: onRowInspect,
                onRowClick: (doShowGenomeBrowser() && isGenomeBrowserAttributesSelected()) ? onRowClick : null,
                enableInspect: canRead,
                onSort: function (e) {
                    tableSort = {
                        'orders': [{
                            'attr': e.attr.name,
                            'direction': e.order === 'desc' ? 'DESC' : 'ASC'
                        }]
                    };
                }
            }), $('#data-table-container')[0]);
        });
    }

    function onDataChange() {
        $(document).trigger('dataChange.data');
    }

    function onRowInspect(e) {
        var entityId = e.id;
        var entityTypeId = e.name;

        $('#entityReport').load("dataexplorer/details", {entityTypeId: entityTypeId, entityId: entityId}, function () {
            $('#entityReportModal').modal("show");
        });
    }

    function onRowClick(entity) {
        var chrom = entity[genomebrowserChromosomeAttribute.name];
        var pos = entity[genomebrowserStartAttribute.name];
        if (chrom !== undefined && chrom !== "" && pos !== undefined && pos !== "") {
            genomeBrowser.setLocation(chrom, pos - 50, pos + 50);
        }
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function download() {
        $.download(molgenis.getContextUrl() + '/download', {
            // Workaround, see http://stackoverflow.com/a/9970672
            'dataRequest': JSON.stringify(createDownloadDataRequest())
        });

        $('#downloadModal').modal('hide');
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function createDownloadDataRequest() {
        var entityQuery = getQuery();

        var dataRequest = {
            entityTypeId: getEntity().name,
            attributeNames: [],
            query: {
                rules: [entityQuery.q]
            },
            colNames: $('input[name=colNames]:checked').val(),
            entityValues: $('input[name=entityValues]:checked').val(),
            downloadType: $('input[name=downloadTypes]:checked').val()
        };

        dataRequest.query.sort = tableSort;

        var colAttributes = molgenis.getAtomicAttributes(getAttributes(), restApi);

        $.each(colAttributes, function () {
            var feature = this;
            dataRequest.attributeNames.push(feature.name);
        });

        return dataRequest;
    }

    //--BEGIN genome browser--
    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function doShowGenomeBrowser() {
        // dalliance is not compatible with IE9
        return molgenis.ie9 !== true && molgenis.ie10 !== true && genomebrowserStartAttribute !== undefined && genomebrowserChromosomeAttribute !== undefined && molgenis.dataexplorer.settings["data_genome_browser"] !== false;
    }

    //used to determine if the rowclick should be available
    function isGenomeBrowserAttributesSelected() {
        var attributes = molgenis.dataexplorer.getSelectedAttributes();
        return (attributes.indexOf(genomebrowserChromosomeAttribute) != -1) && (attributes.indexOf(genomebrowserStartAttribute) != -1);
    }

    function getAttributeFromList(attributesString) {
        var result;
        var attrs = getEntity().attributes;
        var list = attributesString.split(",");
        for (var item in list) {
            result = attrs[list[item]];
            if (result !== undefined) {
                break;
            }
        }
        return result;
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function createGenomeBrowser(specificSettings) {
        var showHighlight = false;
        if (specificSettings !== null) {
            showHighlight = specificSettings.showHighlight;
        }
        var settings = $.extend(true, {}, genomeBrowserSettings, specificSettings || {});

        $('#genomebrowser').css('display', 'block');
        $('#genomebrowser').css('visibility', 'visible');

        for (var track in genomeTracks) {
            settings.sources.push(genomeTracks[track]);
        }
        genomeBrowser = new Browser(settings);
        // highlight region specified with viewStart and viewEnd
        if (showHighlight === true) {
            genomeBrowser.highlightRegion(genomeBrowser.chr, (genomeBrowser.viewStart + 9990), (genomeBrowser.viewEnd - 9990));
        }
        return genomeBrowser;
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function setDallianceFilter() {
        $.each(getAttributes(), function (key, attribute) {
            if (attribute === genomebrowserStartAttribute) {
                createFilter(attribute, Math.floor(genomeBrowser.viewStart).toString(), Math.floor(genomeBrowser.viewEnd).toString());
            } else if (attribute === genomebrowserChromosomeAttribute) {
                createFilter(attribute, undefined, undefined, genomeBrowser.chr);
            }
        });
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function setGenomeBrowserAttributes(start, chromosome) {
        genomebrowserStartAttribute = getAttributeFromList(start);
        genomebrowserChromosomeAttribute = getAttributeFromList(chromosome);
    }

    //--END genome browser--

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function getEntity() {
        return molgenis.dataexplorer.getSelectedEntityMeta();
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function getAttributes() {
        return molgenis.dataexplorer.getSelectedAttributes();
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function getAttributesTree() {
        return molgenis.dataexplorer.getSelectedAttributesTree();
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    function getQuery() {
        return molgenis.dataexplorer.getEntityQuery();
    }

    function createFilter(attribute, fromValue, toValue, values) {
        var attributeFilter = new molgenis.dataexplorer.filter.SimpleFilter(attribute, fromValue, toValue, values);
        var complexFilter = new molgenis.dataexplorer.filter.ComplexFilter(attribute);
        var complexFilterElement = new molgenis.dataexplorer.filter.ComplexFilterElement(attribute);
        complexFilterElement.simpleFilter = attributeFilter;
        complexFilterElement.operator = undefined;
        complexFilter.addComplexFilterElement(complexFilterElement);
        $(document).trigger('updateAttributeFilters', {'filters': [complexFilter]});
    }

    /**
     * @memberOf molgenis.dataexplorer.data
     */
    $(function () {
        $(document).off('.data');

        $(document).on('changeModule.data', function (e, mod) {
            if (mod === 'data') {
                molgenis.dataexplorer.data.createDataTable();
            }
        });

        $(document).on('changeAttributeSelection.data', function (e, data) {
            if (Table && Table.isMounted() && (molgenis.dataexplorer.getSelectedModule() == 'data')) {
                var tableAttrs = Table.state.attrs;
                var treeAttrs = data.attributesTree;
                for (var attr in treeAttrs) {
                    if (tableAttrs[attr] !== undefined && tableAttrs[attr] !== null) {
                        //check if the attribute was expanded (x/mrefs), if so, and still selected, copy the * to stay expanded.
                        if (tableAttrs[attr].hasOwnProperty('*') && treeAttrs.hasOwnProperty(attr)) {
                            treeAttrs[attr] = tableAttrs[attr];
                        }
                    }
                }
                Table.setProps(
                    {
                        attrs: treeAttrs,
                        onRowClick: (doShowGenomeBrowser() && isGenomeBrowserAttributesSelected()) ? onRowClick : null
                    }
                );
            }
        });

        $(document).on('updateAttributeFilters.data', function (e, data) {
            /**
             * Validation before using the setLocation of the browser
             */
            function setLocation(chr, viewStart, viewEnd) {
                var maxViewWidth = 999999999;
                if (chr) {
                    viewStart = viewStart && viewStart > 0 ? viewStart : 1;
                    viewEnd = viewEnd && viewEnd > 0 ? viewEnd : viewStart + maxViewWidth;
                    genomeBrowser.setLocation(chr, viewStart, viewEnd);
                }
            }

            if (molgenis.dataexplorer.getSelectedModule() == 'data') {
                if (molgenis.dataexplorer.settings["genomebrowser"] !== 'false') {
                    // TODO implement elegant solution for genome browser specific code
                    $.each(data.filters, function () {
                        if (this.getComplexFilterElements && this.getComplexFilterElements()[0]) {
                            if (this.attribute === genomebrowserStartAttribute) {
                                setLocation(genomeBrowser.chr,
                                    parseInt(this.getComplexFilterElements()[0].simpleFilter.fromValue),
                                    parseInt(this.getComplexFilterElements()[0].simpleFilter.toValue));
                            }
                            else if (this.attribute === genomebrowserChromosomeAttribute) {
                                setLocation(this.getComplexFilterElements()[0].simpleFilter.getValues()[0],
                                    genomeBrowser.viewStart,
                                    genomeBrowser.viewEnd);
                            }
                        }
                    });
                }
            }
        });

        $(document).on('changeQuery.data', function (e, query) {
            if (Table && Table.isMounted() && (molgenis.dataexplorer.getSelectedModule() == 'data')) {
                Table.setProps({
                    query: query
                });
            }
            // TODO what to do for genome browser
        });

        $('#download-button').click(function () {
            download();
        });

        $('#genomebrowser-filter-button').click(function () {
            setDallianceFilter();
        });
    });
}($, window.top.molgenis = window.top.molgenis || {}));