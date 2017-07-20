(function ($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();
    var detailsPackageName;
    var countTemplate;
    var modelTemplate;

    var nrResultsPerPage = 3;
    var query;
    var pageIndex = 0;

    /**
     * @memberOf molgenis.model-registry
     */
    function createPackageTree(selectedPackage) {
        if (selectedPackage.name) {
            $('#attribute-selection').fancytree({
                source: {
                    url: molgenis.getContextUrl() + "/getTreeData?package=" + selectedPackage.name
                },
                'click': function (event, data) {
                    if (data.targetType === 'title' || data.targetType === 'icon') {
                        switch (data.node.data.type) {
                            case 'package' :
                                $('#package-doc-container').scrollTo('#package-' + data.node.key);
                                // needed by uml viewer
                                detailsPackageName = data.node.key;
                                break;
                            case 'entity' :
                                $('#package-doc-container').scrollTo('#entity-' + data.node.key);
                                break;
                            case 'attribute' :
                                var parent = data.node.parent;
                                while (parent.extraClasses != 'entity') {
                                    //Compound attr
                                    parent = parent.parent;
                                }
                                $('#package-doc-container').scrollTo('#attribute-' + parent.key + data.node.key);
                                break;
                            default:
                                throw 'Unknown type';
                        }
                    }
                }
            });
        }
    }

    /**
     * @memberOf molgenis.model-registry
     */
    function renderSearchResults(searchResults, container) {
        container.empty();
        for (var i = 0; i < searchResults.packages.length; ++i) {
            container.append(modelTemplate({
                'package': searchResults.packages[i],
                'entities': searchResults.packages[i].entitiesInPackage,
                'tags': searchResults.packages[i].tags
            }));
        }
        container.append(countTemplate({'count': searchResults.total}));
        $('.select2').select2({width: 300});
    }

    $(function () {
        var searchResultsContainer = $('#package-search-results');

        $('form[name=search-form]').submit(function (e) {
            e.preventDefault();

            var q = $('#package-search').val();

            if (q != query) {
                //New search reset pageIndex
                pageIndex = 0;
                query = q;
            }

            $.ajax({
                type: $(this).attr('method'),
                url: $(this).attr('action'),
                data: JSON.stringify({
                    query: q,
                    offset: pageIndex * nrResultsPerPage,
                    num: nrResultsPerPage
                }),
                contentType: 'application/json',
                success: function (data) {
                    renderSearchResultsBySucces(data);
                }
            });
        });

        function renderSearchResultsBySucces(data) {
            renderSearchResults(data, searchResultsContainer);

            if (data.total > nrResultsPerPage) {
                $('#package-search-results-pager').show();

                $('#package-search-results-pager').pager({
                    'nrItems': data.total,
                    'nrItemsPerPage': nrResultsPerPage,
                    'page': pageIndex + 1,
                    'onPageChange': function (pager) {
                        pageIndex = pager.page - 1;
                        $('form[name=search-form]').submit();
                    }
                });
            } else {
                $('#package-search-results-pager').hide();
            }
        }

        $(document).on('click', '#search-clear-button', function () {
            $('#package-search').val('');
            $('form[name=search-form]').submit();
        });

        function showPackageDetails(id) {
            detailsPackageName = id;

            $('#standards-registry-details').load(molgenis.getContextUrl() + '/details?package=' + id, function () {
                $.get(molgenis.getContextUrl() + '/getPackage?package=' + id, function (selectedPackage) {
                    createPackageTree(selectedPackage);
                });

                $('#standards-registry-search').removeClass('show').addClass('hidden');
                $('#standards-registry-details').removeClass('hidden').addClass('show');
            });
        }

        $(document).on('click', '.details-btn', function () {
            var id = $(this).closest('.package').data('id');
            showPackageDetails(id);
            return false;
        });

        $(document).on('click', '#search-results-back-btn', function () {
            $('#standards-registry-search').removeClass('hidden').addClass('show');
            $('#standards-registry-details').removeClass('show').addClass('hidden');
        });

        $(document).on('click', '.dataexplorer-btn', function () {
            var selectedEntity = $(this).parent().siblings('select.entity-select-dropdown').val();
            if (selectedEntity) {
                // FIXME do not hardcode URL
                window.location.href = '/menu/main/dataexplorer?entity=' + selectedEntity;
            }
        });



        $(document).on('click', '#print-btn', function () {
            $('#package-doc-container').css('height', '100%').css('width', '21cm').css("overflow", "hidden");
            $('#model-registry-uml-holder').css("overflow", "hidden");
            window.print();
            $('#package-doc-container').css('height', '600px').css("overflow-x", "hidden").css("overflow-y", "auto").css('width', '923px');
            $('#model-registry-uml-holder').css("overflow", "auto");
        });

        $(document).on('click', '#uml-tab', function () {
            showSpinner();
            setTimeout(function () {
                $('#model-registry-uml').load(molgenis.getContextUrl() + '/uml?package=' + detailsPackageName);
                hideSpinner();
            }, 500);

        });

        countTemplate = Handlebars.compile($("#count-template").html());
        modelTemplate = Handlebars.compile($("#model-template").html());

        if (window.location.search) {
            var query = window.location.search.substring(1);
            var pairs, keyValuePair = [];
            if (query !== undefined) {
                pairs = query.split('&');
                jQuery.each(pairs, function (indexInArray, value) {
                    var keyValuePair = value.split('=');
                    if ('package' === keyValuePair[0]) {
                        showPackageDetails(keyValuePair[1]);
                    }
                });
            }
        }

        var data = $("#package-search-results[data-package-search-results]").attr("data-package-search-results");
        if (data) {
            // Initial setting when data-results is set.
            renderSearchResultsBySucces(JSON.parse(data));
            $("#package-search-results[data-package-search-results]").removeAttr("data-package-search-results");
        } else {
            // initially search for all models
            $('form[name=search-form]').submit();
        }
    });

}($, window.top.molgenis = window.top.molgenis || {}));
