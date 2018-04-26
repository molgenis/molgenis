$.when($,
    window.top.molgenis = window.top.molgenis || {},
    molgenis.getPluginSettings()
).then(
    function ($, molgenis, settingsXhr) {
        "use strict";
        var self = molgenis.dataexplorer = molgenis.dataexplorer || {};

        // module api
        self.getSelectedEntityMeta = getSelectedEntityMeta;
        self.getSelectedAttributes = getSelectedAttributes;
        self.getSelectedAttributesTree = getSelectedAttributesTree;
        self.getEntityQuery = getEntityQuery;
        self.createHeader = createHeader;
        self.setGenomeAttributes = setGenomeAttributes;
        self.getSelectedModule = getSelectedModule;
        self.getRSQL = getRSQL;
        self.getSearchQuery = getSearchQuery
        self.getnToken = getnToken;

        self.moduleEvents = {
            DATAEXPLORER_URI_CHANGE: 'dataexplorer.uri.change'
        }

        var restApi = new molgenis.RestClient();
        var selectedEntityMetaData = null;
        var attributeFilters = {};
        var selectedAttributes = [];
        var selectedAttributesTree = {};
        var searchQuery = null;
        var modules = [];
        var filter = undefined;

        var posAttribute;
        var chromosomeAttribute;

        if (settingsXhr[1] !== 'success') {
            molgenis.createAlert([{message: 'An error occurred initializing the data explorer.'}], 'error');
        }

        var settings = settingsXhr[0]
        self.settings = settings;

        var stateDefault = {
            entity: null,
            query: null,
            attrs: null,
            mod: null,
            hideselect: 'false',
            filter: undefined
        };

        var state;

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getnToken() {
            return state.nToken;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getRSQL() {
            return state.filter;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getSelectedModule() {
            return state.mod;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getSelectedEntityMeta() {
            return selectedEntityMetaData;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getSelectedAttributes() {
            return selectedAttributes;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getSelectedAttributesTree() {
            return selectedAttributesTree;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function getEntityQuery() {
            // N.B. There's a translation step between the query in the state, which is also shown on screen
            // ("SEARCH 1:10050001") and the actual entity query which is used when retrieving data
            // (CHROM = 1 AND POS = 1005001)
            // So here we should return the *translated* query.
            return createEntityQuery();
        }

        function getSearchQuery () {
            return searchQuery
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function createModuleNav(modules, entity, container) {
            var items = [];
            items.push('<ul class="nav nav-tabs pull-left" style="width: 100%" role="tablist">');
            $.each(modules, function () {
                var href = molgenis.getContextUrl() + '/module/' + this.id + '?entity=' + entity;
                items.push('<li data-id="' + this.id + '"><a href="' + href + '" data-target="#tab-' + this.id + '" data-id="' + this.id + '" role="tab" data-toggle="tab"><img src="/img/' + this.icon + '"> ' + this.label + '</a></li>');
            });
            items.push('<li class="pull-right">');
            items.push('<button type="button" class="btn btn-default" id="toggleSelectors">') +
            items.push('<span id="toggleSelectorsIcon" class="glyphicon glyphicon-resize-horizontal"></span>') +
            items.push('</button></li>');
            items.push('</ul>');
            items.push('<div class="tab-content">');
            $.each(modules, function () {
                items.push('<div class="tab-pane" id="tab-' + this.id + '" data-id="' + this.id + '">Loading...</div>');
            });
            items.push('</div>');

            // add menu to container
            container.html(items.join(''));
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function createEntityMetaTree(entityMetaData, attributes) {
            var container = $('#feature-selection');
            container.tree({
                'entityMetaData': entityMetaData,
                'selectedAttributes': attributes,
                'onAttributesSelect': function () {
                    selectedAttributes = container.tree('getSelectedAttributes');
                    selectedAttributesTree = container.tree('getSelectedAttributesTree');
                    $(document).trigger('changeAttributeSelection', {
                        'attributes': selectedAttributes,
                        'attributesTree': selectedAttributesTree,
                        'totalNrAttributes': Object.keys(entityMetaData.attributes).length
                    });
                },
                'onAttributeClick': function (attribute) {
                    $(document).trigger('clickAttribute', {'attribute': attribute});
                }
            });
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function createHeader(entityMetaData) {
            $('#entity-class-name').html(entityMetaData.label);

            if (entityMetaData.description) {
                var description = $('<span data-placement="bottom"></span>');
                description.html(abbreviate(entityMetaData.description, settings['header_abbreviate']));
                description.attr('data-title', entityMetaData.description);
                $('#entity-class-description').html(description.tooltip());
            } else {
                $('#entity-class-description').html('');
            }
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function setGenomeAttributes (start, chromosome) {
          posAttribute = start
          chromosomeAttribute = chromosome
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function createEntityQuery() {
            var chromosome;
            var entityCollectionRequest = {
                q: []
            };

            // add rules for the search term to the query
            if (searchQuery) {
                if (/\S/.test(searchQuery)) {
                    var searchQueryRegex = /^\s*(?:chr)?([\d]{1,2}|X|Y|MT|XY):([\d]+)(?:-([\d]+)+)?\s*$/g;

                    if (searchQueryRegex && searchQuery.match(searchQueryRegex) && chromosomeAttribute !== undefined && posAttribute !== undefined) {
                        var match = searchQueryRegex.exec(searchQuery);

                        // only chromosome and position
                        if (match[3] === undefined) {
                            chromosome = match[1];
                            var position = match[2];

                            entityCollectionRequest.q =
                                [{
                                    operator: "NESTED",
                                    nestedRules: [{
                                      field: chromosomeAttribute,
                                        operator: "EQUALS",
                                        value: chromosome
                                    }]
                                }, {
                                    operator: "AND"
                                }, {
                                    operator: "NESTED",
                                    nestedRules: [{
                                      field: posAttribute,
                                        operator: "EQUALS",
                                        value: position
                                    }]
                                }];
                            // chromosome:startPos - endPos
                        } else if (match[3]) {

                            chromosome = match[1];
                            var startPosition = match[2];
                            var stopPosition = match[3];

                            if (parseInt(startPosition, 10) > parseInt(stopPosition, 10)) {
                                molgenis.createAlert([{message: 'The start position of the queried range is larger than the stop position. Please check the search query.'}], 'warning');
                            } else {
                                $('.alerts').empty();
                            }

                            entityCollectionRequest.q =
                                [{
                                    operator: "NESTED",
                                    nestedRules: [{
                                        operator: "NESTED",
                                        nestedRules: [{
                                          field: chromosomeAttribute,
                                            operator: "EQUALS",
                                            value: chromosome
                                        }]
                                    }]
                                }, {
                                    operator: "AND"
                                }, {
                                    operator: "NESTED",
                                    nestedRules: [{
                                      field: posAttribute,
                                        operator: "GREATER_EQUAL",
                                        value: startPosition
                                    }, {
                                        operator: "AND"
                                    }, {
                                      field: posAttribute,
                                        operator: "LESS_EQUAL",
                                        value: stopPosition
                                    }]
                                }];
                        }
                    } else {
                        entityCollectionRequest.q.push({
                            operator: 'SEARCH',
                            value: searchQuery
                        });
                    }
                }
            }

            // add rules for attribute filters to the query
            $.each(attributeFilters, function (attributeUri, filter) {
                var rule = filter.createQueryRule();
                if (rule) {
                    if (entityCollectionRequest.q.length > 0) {
                        entityCollectionRequest.q.push({
                            operator: 'AND'
                        });
                    }
                    entityCollectionRequest.q.push(rule);
                }
            });

            /**
             * Debug info:
             * Activate this code to see the query
             *
             * $("#debugFilterQuery").remove();
             * $("#tab-data").append($('<div id="debugFilterQuery"><p>QUERY : </p><p>' + JSON.stringify(entityCollectionRequest) + '</p></div>'));
             */

            return entityCollectionRequest;
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        function render() {
            // Check if the copy button should be shown or not
            $.get(molgenis.getContextUrl() + '/copy?entity=' + state.entity).done(function (data) {
                if (data === true) {
                    $("#copy-data-btn").removeClass('hidden');
                } else {
                    $("#copy-data-btn").addClass('hidden');
                }
            });

            $.get(molgenis.getContextUrl() + '/navigatorLinks?entity=' + state.entity).done(function (data) {

                if (data.length > 0) {
                    $("#entity-package-path").removeClass('hidden');

                    const hyperlinks = [];
                    data.forEach(function (element) {
                        if (element.label === "glyphicon-home") element.label = "<span class='glyphicon glyphicon-home' aria-hidden='true'></span> ";
                        var link = "<a href='" + element.href + "'>" + element.label + "</a>"
                        hyperlinks.push(link);
                    });

                    $("#entity-package-path").html("(" + hyperlinks.join(" / ") + ")");
                } else {
                    $("#entity-package-path").addClass('hidden');
                }
            });

            // get entity meta data and update header and tree
            var entityMetaDataRequest = restApi.getAsync('/api/v1/' + state.entity + '/meta', {expand: ['attributes']}, function (entityMetaData) {
                selectedEntityMetaData = entityMetaData;
                selectedAttributes = [];
                self.createHeader(entityMetaData);

                // Loop through all the attributes in the meta data
                $.each(entityMetaData.attributes, function (index, attribute) {

                    // Default expansion is false
                    // Expanded has to do with xref / mref attributes
                    attribute.expanded = false;

                    // If the state is empty or undefined, or is set to
                    // 'none', return null. All attributes will be shown
                    if (state.attrs === undefined || state.attrs === null) {
                        if (attribute.fieldType !== 'COMPOUND') selectedAttributes.push(attribute);
                    } else if (state.attrs === 'none') {
                        selectedAttributes = [];
                    } else {

                        // Loop through all the attributes mentioned in the state (url)
                        $.each(state.attrs, function (index, selectedAttrName) {
                            // If the attribute is in the state, add that attribute to the selectedAttributes
                            // For compound attributes, check the atomic attributes
                            if (attribute.name === selectedAttrName) {
                                selectedAttributes.push(attribute);
                            }
                        });
                    }
                });

                // Empties existing tree of selected attributes
                selectedAttributesTree = {};

                // For each selected attribute, check if it is expanded and fill the selectedAttributesTree map
                $.each(selectedAttributes, function (index, attribute) {
                    var key = attribute.name;
                    selectedAttributesTree[key] = attribute.expanded === true ? {'*': null} : null;
                });

                createEntityMetaTree(entityMetaData, selectedAttributes);

                if (settings['launch_wizard'] === true) {
                    self.filter.wizard.openFilterWizardModal(entityMetaData, attributeFilters);
                }
            });

            // get entity modules and load visible module
            $.get(molgenis.getContextUrl() + '/modules?entity=' + state.entity).done(function (data) {
                var container = $('#module-nav');
                modules = data.modules;
                createModuleNav(data.modules, state.entity, container);

                // select first tab
                var moduleTab;
                if (state.mod) {
                    moduleTab = $('a[data-toggle="tab"][data-target="#tab-' + state.mod + '"]', container);
                } else {

                    moduleTab = $('a[data-toggle="tab"]', container).first();
                }
                state.mod = moduleTab.data('id');

                // show tab once entity meta data is available
                $.when(entityMetaDataRequest).done(function () {
                    moduleTab.tab('show');
                });

                function hideSelectors() {
                    $('#selectors').removeClass("col-md-3").addClass("hidden");
                    $('#modules').removeClass("col-md-9").addClass("col-md-12");
                    $('#toggleSelectorsIcon').removeClass("glyphicon glyphicon-resize-horizontal").addClass("glyphicon glyphicon-resize-small");
                }

                function showSelectors() {
                    $('#selectors').addClass("col-md-3").removeClass("hidden");
                    $('#modules').removeClass("col-md-12").addClass("col-md-9");
                    $('#toggleSelectorsIcon').removeClass("glyphicon glyphicon-resize-small").addClass("glyphicon glyphicon-resize-horizontal");
                }

                $('#toggleSelectors').on('click', function () {
                    if ($('#selectors').hasClass("hidden")) {
                        showSelectors();
                    }
                    else {
                        hideSelectors();
                    }
                });
            });

            $('#observationset-search').focus();
        }

        function sendVirtualPageView (ga, location) {
          /* ga() is the google analytics library operations que function */
          if(window.hasTrackingId) {
            // default tracker
            ga('set', 'page', location);
            ga('send', 'pageview')
          }

          if(window.hasMolgenisTrackingId) {
            // molgenis tracker
            ga('molgenisTracker.set', 'page', location);
            ga('molgenisTracker.send', 'pageview')
          }
        }

        function pushState() {
            // shorten URL by removing attributes with null or undefined values
            var cleanState = {};
            for (var key in state) {
                if (state.hasOwnProperty(key)) {
                    var val = state[key];
                    if (val) {
                        cleanState[key] = val;
                    }
                }
            }

            if (state.query) {
                delete cleanState.query;
                for (var i = 0; i < state.query.q.length; ++i) {
                    var rule = state.query.q[i];
                    if (rule.field === undefined && rule.operator === 'SEARCH') {
                        cleanState.query = {q: [rule]};
                        break;
                    }
                }
            }

            // Use special encoding for the rsql
            var filter = state.filter
            delete cleanState.filter

            // update browser state
            if (filter) history.pushState(state, '', molgenis.getContextUrl() + '?' + $.param(cleanState)
                + '&filter=' + molgenis.rsql.encodeRsqlValue(filter));
            else history.pushState(state, '', molgenis.getContextUrl() + '?' + $.param(cleanState));

            $(document).trigger(self.moduleEvents.DATAEXPLORER_URI_CHANGE);
        }

        /**
         * @memberOf molgenis.dataexplorer
         */
        $(function () {

            var googleSearchField = $("#observationset-search")
            var entityTypeDropdown = $('#dataset-select');

            $(document).off(self.moduleEvents.DATAEXPLORER_URI_CHANGE)
            if(window.ga && (window.hasTrackingId || window.hasMolgenisTrackingId) ) {
                $(document).on(self.moduleEvents.DATAEXPLORER_URI_CHANGE, function () {
                    sendVirtualPageView(ga, $(location).attr('href'))
                })
            }

            // lazy load tab contents
            $(document).on('show.bs.tab', 'a[data-toggle="tab"]', function (e) {
                var target = $($(e.target).attr('data-target')), entityHref = encodeURI($(e.target).attr('href'));
                if (target.data('status') !== 'loaded') {
                    target.load(entityHref, function () {
                        target.data('status', 'loaded');
                    });
                }
            });

            $(document).on('changeQuery', function (e, query) {
                state.query = query;
                pushState();
            });

            $(document).on('changeEntity', function (e, entity) {
                // reset state
                state = {
                    entity: entity,
                    attributes: [],
                    mod: null
                };
                pushState();

                // reset
                selectedEntityMetaData = null;
                attributeFilters = {};
                selectedAttributes = [];
                searchQuery = null;

                if ($('#data-table-container').length > 0) {
                    React.unmountComponentAtNode($('#data-table-container')[0]); // must occur before mod-data is loaded
                }
                $('#feature-filters').find('p').remove();
                googleSearchField.val("");
                $('#data-table-pager').empty();

                // reset: unbind existing event handlers
                $.each(modules, function () {
                    $(document).off('.' + this.id);
                });

                render();
            });

            $(document).on('changeModule', function (e, mod) {
                state.mod = mod;
                pushState();
            });

            $(document).on('changeAttributeSelection', function (e, data) {
                if (data.attributes.length === 0) {
                    state.attrs = 'none';
                }
                else if (data.attributes.length === data.totalNrAttributes) {
                    state.attrs = null;
                }
                else {
                    state.attrs = $.map(data.attributes, function (attribute) {
                        return attribute.name;
                    });
                }
                pushState();
            });

            $(document).on('updateAttributeFilters', function (e, data) {
                var rules = []
                for (var i = 0; i < Object.keys(data.filters).length; i++) {
                    var key = Object.keys(data.filters)[i]
                    var filter = data.filters[key]
                    var rule = filter.createQueryRule()

                    if ((rule.hasOwnProperty('value') && rule.value !== undefined) ||
                        (rule.hasOwnProperty('nestedRules') && rule.nestedRules.length > 0)) {

                        if (rules.length > 0) {
                            // Add an 'AND' operator between filters for every attribute
                            rules.push({'operator': 'AND'})
                        }
                        rules.push(rule)
                    }

                    if (filter.isEmpty()) {
                        delete attributeFilters[filter.attribute.href];
                    } else {
                        attributeFilters[filter.attribute.href] = filter;
                    }
                }

                if (rules.length > 0) {
                    var queryRuleRSQL = molgenis.createRsqlQuery(rules)
                    state.filter = molgenis.dataexplorer.rsql.translateFilterRulesToRSQL(queryRuleRSQL, state.filter)
                    pushState()
                } else if (rules.length === 0) { // Filter wizard triggers updateAttributeFilters with an empty rule list, cleanup state when this happens
                  delete state.filter
                  pushState()
                }

                self.filter.createFilterQueryUserReadableList(attributeFilters);
                $(document).trigger('changeQuery', createEntityQuery());
            });

            $(document).on('removeAttributeFilter', function (e, data) {
                delete attributeFilters[data.attributeUri];
                self.filter.createFilterQueryUserReadableList(attributeFilters);

                var attribute = data.attributeUri.split('/')[5]
                state.filter = molgenis.dataexplorer.rsql.removeFilterFromRsqlState(attribute, state.filter)
                pushState()

                $(document).trigger('changeQuery', createEntityQuery());
            });

            $(document).on('clickAttribute', function (e, data) {
                var attr = data.attribute;
                if (attr.fieldType !== 'COMPOUND' && (!attr.refEntity || !attr.parent))
                    self.filter.dialog.openFilterModal(data.attribute, attributeFilters[data.attribute.href]);
            });

            var container = $("#plugin-container");


            if (entityTypeDropdown.length > 0) {
                entityTypeDropdown.select2({width: 'resolve'});
                entityTypeDropdown.change(function () {
                    $(document).trigger('changeEntity', $(this).val());
                });
            }

            googleSearchField.change(function (e) {
                searchQuery = $(this).val().trim();
                $(document).trigger('changeQuery', createEntityQuery());
            });

            // Workaround for IE not submitting on "enter"
            $('#observationset-search').keypress(function (event) {
                if (getInternetExplorerVersion() != -1) {

                    if (event.which == 13) {
                        $(document).trigger('changeQuery', createEntityQuery());
                        googleSearchField.change();
                    }
                }
            });

            $('#search-clear-button').click(function () {
                googleSearchField.val('');
                googleSearchField.change();
            });

            $('#filter-wizard-btn').click(function () {
                self.filter.wizard.openFilterWizardModal(selectedEntityMetaData, attributeFilters);
            });

            $('#module-nav').on('click', 'ul.nav > li > a', function (e) {
                $(document).trigger('changeModule', $(this).data('id'));
            });

            $(container).on('click', '.feature-filter-edit', function (e) {
                e.preventDefault();
                var filter = attributeFilters[$(this).data('href')];
                self.filter.dialog.openFilterModal(filter.attribute, filter);
            });

            $(container).on('click', '.feature-filter-remove', function (e) {
                e.preventDefault();
                $(document).trigger('removeAttributeFilter', {'attributeUri': $(this).data('href')});
            });

            $('#delete-data-btn').on('click', function () {
                bootbox.confirm("Are you sure you want to delete all data for this entity?", function (confirmed) {
                    if (confirmed) {
                        $.ajax('/api/v1/' + selectedEntityMetaData.name, {'type': 'DELETE'}).done(function () {
                            document.location.href = '/menu/main/dataexplorer?entity=' + selectedEntityMetaData.name;
                        });
                    }
                });
            });

            $('#copy-data-btn').on('click', function () {
                bootbox.prompt({
                    title: "<h4>Copy entity [" + selectedEntityMetaData.label + "]<h4/>" +
                    "<div class=\"small\">Please enter a new entity name." +
                    "<ul>" +
                    "<li>Use max 30 characters." +
                    "<li>Only letters (a-z, A-Z), digits (0-9), underscores (_) and hashes (#) are allowed.</li>" +
                    "</ul>" +
                    "<br/>By pushing the ok button you will create an new entity with copied data.</div>",
                    value: selectedEntityMetaData.label + 'Copy',
                    callback: function (result) {
                        if (result !== null) {
                            $.ajax({
                                headers: {
                                    'Accept': 'application/json',
                                    'Content-Type': 'application/json'
                                },
                                'type': 'POST',
                                'url': '/api/v2/copy/' + selectedEntityMetaData.name,
                                'data': JSON.stringify({'newEntityName': result}),
                                'dataType': 'json',
                                'success': function (newEntityName) {
                                    document.location.href = '/menu/main/dataexplorer?entity=' + newEntityName;
                                }
                            });
                        }
                    }
                });
            });

            $('#delete-data-metadata-btn').on('click', function () {
                bootbox.confirm("Are you sure you want to delete all data and metadata for this entity?", function (confirmed) {
                    if (confirmed) {
                        $.ajax('/api/v1/' + selectedEntityMetaData.name + '/meta', {'type': 'DELETE'}).done(function () {
                            document.location.href = "/menu/main/dataexplorer";
                        });
                    }
                });
            });

            function init() {
                // set entity in dropdown
                if (!state.entity) {
                    state.entity = $("#dataset-select").find("option:selected").val()
                }
                if (!state.entity) {
                    state.entity = $('#dataset-select').find('option:not(:empty)').first().val();
                }
                $('#dataset-select').select2('val', state.entity);

                // hide entity dropdown
                if (state.hideselect === 'true') {
                    $('#dataset-select-container').addClass('hidden');
                } else {
                    $('#dataset-select-container').removeClass('hidden');
                }

                if (state.query) {
                    // set query in searchbox
                    for (var i = 0; i < state.query.q.length; ++i) {
                        var rule = state.query.q[i];
                        if (rule.field === undefined && rule.operator === 'SEARCH') {
                            $('#observationset-search').val(rule.value).change();
                            break;
                        }
                    }
                }

                if (state.filter) {
                    // Create filters based on RSQL present in the URL
                    molgenis.dataexplorer.rsql.createFiltersFromRsql(state.filter, restApi, state.entity);
                }

                if (state.entity) {
                    render();
                }
            }

            // set state from url
            if (window.location.search && window.location.search.length > 0) {
                var querystring = window.location.search.substring(1); // remove '?'
                if (querystring.length > 0) {
                    state = $.deparam(querystring);
                }
            } else {
                state = stateDefault;
            }

            // handle browser back event
            window.onpopstate = function (event) {
                if (event.state !== null) {
                    state = event.state;
                    init();
                }
            };

            init();
        });
    });
