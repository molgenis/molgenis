(function ($, molgenis) {
    'use strict';

    var restApi = new molgenis.RestClient();
    var ontologyServiceRequest = null;
    var $resultContainer = null;
    var NO_MATCH_INFO = 'N/A';
    var itemsPerPage = 10;


    $(document).ready(function () {
        $('#back-button').click(function () {
            $('#ontology-match').attr({
                'action': molgenis.getContextUrl(),
                'method': 'GET'
            }).submit();
        });

        // Only initiate the service on the task detail page.
        if (window.sorta) {
            var ontologyService = new molgenis.OntologyService($('#match-result-container'), sorta.request);
            ontologyService.renderPage();

            $('#update-threshold-button').click(function () {
                $(this).parents('form:eq(0)').attr({
                    'action': molgenis.getContextUrl() + '/threshold/' + ontologyServiceRequest.sortaJobExecutionId,
                    'method': 'POST'
                }).submit();
            });
        }
    });


    molgenis.OntologyService = function OntologyService(container, request) {
        if (container) {
            $resultContainer = container;
        }
        if (request) {
            ontologyServiceRequest = request;
            ontologyServiceRequest.entityPager = {start: 0, num: itemsPerPage, total: 5};
        }
    };


    molgenis.OntologyService.prototype.deleteMatchingTask = function (sortaJobExecutionId, callback) {
        $.ajax({
            type: 'POST',
            url: molgenis.getContextUrl() + '/delete',
            async: false,
            data: JSON.stringify(sortaJobExecutionId),
            contentType: 'application/json',
            success: function () {
                if (callback) callback();
            }
        });
    };


    molgenis.OntologyService.prototype.renderPage = function () {
        toggleDetailView(false);

        var items = [];
        items.push('<div class="row"><div class="col-md-offset-3 col-md-6 well">');
        items.push('<div class="row">');
        items.push('<div class="col-md-8">Matched input terms: <strong><span id="matched-item-count"></span></strong></div>');
        items.push('<div class="col-md-4"><button id="matched-result-button" type="button" class="btn btn-primary pull-right">Show</button></div>');
        items.push('</div><br>');
        items.push('<div class="row">');
        items.push('<div class="col-md-8">Unmatched input terms: <strong><span id="unmatched-item-count"></span></strong></div>');
        items.push('<div class="col-md-4"><button id="unmatched-result-button" type="button" class="btn btn-info pull-right">Show</button></div>');
        items.push('</div><br>');
        items.push('<div class="row">');
        items.push('<div class="col-md-12"><button id="sorta-download-button" class="btn btn-primary" type="button">Download</button></div>');
        items.push('</div>');
        items.push('</div></div>');
        $resultContainer.empty().append(items.join(''));
        getMatchResultCount();

        $('#sorta-download-button').click(function () {
            $(this).parents('form:eq(0)').attr({
                'action': molgenis.getContextUrl() + '/match/download/' + ontologyServiceRequest.sortaJobExecutionId,
                'method': 'GET'
            }).submit();
        });

        $('#matched-result-button').click(function () {
            ontologyServiceRequest.matched = true;
            molgenis.OntologyService.prototype.renderPage();
        });

        $('#unmatched-result-button').click(function () {
            ontologyServiceRequest.matched = false;
            molgenis.OntologyService.prototype.renderPage();
        });
    };


    function createCallBackFunction() {
        return {
            'success': function () {
                // Give the aggregate counter backend some time to update.
                window.setTimeout(function() {
                    molgenis.OntologyService.prototype.renderPage();
                }, 500);
            }, 'error': function () {
                molgenis.OntologyService.prototype.renderPage();
            }
        };
    }


    function deleteUnusedRows (sortaJobExecutionId, inputRowId, usedOutputRowId) {
        var defer = $.Deferred();
        getOutputRows(inputRowId, sortaJobExecutionId, function (data) {
            $.each(data.items, function (index, outputRow) {
                if (outputRow.identifier !== usedOutputRowId) {
                    restApi.remove('/api/v1/' + sortaJobExecutionId + '/' + outputRow.identifier);
                }
            })
            defer.resolve(data);
        });

        return defer.promise();
    }


    function findOntologyTerm(ontologyTerms, matchTerm) {
        var match = null;
        $.each(ontologyTerms, function (index, ontologyTerm) {
            if (ontologyTerm.ontologyTermIRI === matchTerm) {
                match = ontologyTerm;
            }
        })

        return match;
    }


    function gatherInputInfoHelper(inputTerm) {
        var output = []
        if (inputTerm) {
            $.map(inputTerm ? inputTerm : {}, function (val, key) {
                if (key.toLowerCase() !== 'identifier') {
                    output.push(key + ': ' + val);
                }
            });
        }

        return output;
    }


    function gatherOntologyInfoHelper(inputEntity, ontologyTerm) {
        var $ontologyTermContainer = $('<td />');
        if (inputEntity && ontologyTerm) {
            var synonymDiv = $('<div>Synonym : </div>');
            var synonyms = getOntologyTermSynonyms(ontologyTerm);
            if (synonyms.length == 0) {
                synonymDiv.append(NO_MATCH_INFO);
            } else if (synonyms.length == 1) {
                synonymDiv.append(synonyms.join());
            } else {
                synonymDiv.addClass('show-popover').append('<strong>' + synonyms.length + ' synonyms, see more details</strong>').popover({
                    'content': synonyms.join('<br><br>'),
                    'placement': 'auto',
                    'trigger': 'hover',
                    'html': true
                });
            }
            //check if the ontologyTermIRI is a valid link
            if (ontologyTerm.ontologyTermIRI.startsWith('http')) {
                $ontologyTermContainer.append('<div><a href="' + ontologyTerm.ontologyTermIRI + '" target="_blank">' + ontologyTerm.ontologyTermName + '</a></div>').append(synonymDiv);
            } else {
                $ontologyTermContainer.append('<div>' + ontologyTerm.ontologyTermName + '</div>').append(synonymDiv);
            }
            var annotationMap = {};
            $.each(ontologyTerm.ontologyTermDynamicAnnotation, function (i, annotation) {
                if (!annotationMap[annotation.name]) {
                    annotationMap[annotation.name] = [];
                }
                annotationMap[annotation.name].push(annotation.value);
            });
            $.each(Object.keys(inputEntity), function (index, key) {
                if (key.toLowerCase() !== 'name' && key.toLowerCase().search('synonym') === -1 && key.toLowerCase() !== 'identifier') {
                    $ontologyTermContainer.append('<div>' + key + ' : ' + (annotationMap[key] ? annotationMap[key].join() : 'N/A') + '</div>');
                }
            });
        } else {
            $ontologyTermContainer.append(NO_MATCH_INFO);
        }
        return $ontologyTermContainer;
    }


    function generateDetailTable(inputEntity, data, row) {
        toggleDetailView(true);

        if (data.message) {
            console.log('Error fetching candidate mappings', data.message);
            throw data.message;
        }
        var $tableDetailContainer = $('<div class="table-detail-container"></div>');
        //Hide existing table
        row.parents('table:eq(0)').hide();
        //Add table containing candidate matches to the view
        row.parents('div:eq(0)').append($tableDetailContainer);

        //Add a cancelButton for users to go back to previous summary table.
        var $cancelButton = $('<button type="button" class="btn btn-default">Cancel</button>').click(function () {
            $tableDetailContainer.remove();
            row.parents('table:eq(0)').show();
            toggleDetailView(false);
        });

        var $reviewFlag = $('<div class="review-switch"><label class="switch"><input id="cb-review" type="checkbox"><span class="slider"></label><span class="switch-label">Needs Review</span></div>');

        //Add a unknownButton for users to choose 'Unknown' for the input term
        var $noMatchButton = $('<button type="button" class="btn btn-warning">No match</button>').click(function () {
            var inputRowId = inputEntity.Identifier;
            getOutputRows(inputRowId, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                if (data.items.length > 0) {
                    var outputRow = data.items[0];
                    var outputRowId = outputRow.identifier

                    var updatedMappedEntity = {};
                    $.map(outputRow, function (val, key) {
                        updatedMappedEntity[key] = val;
                        if (key === 'validated') updatedMappedEntity[key] = true;
                        if (key === 'inputTerm') updatedMappedEntity[key] = val.Identifier;
                        if (key === 'score') updatedMappedEntity[key] = 0;
                        if (key === 'matchTerm') updatedMappedEntity[key] = null;
                    });

                    updatedMappedEntity.review = $('#cb-review')[0].checked

                    deleteUnusedRows(ontologyServiceRequest.sortaJobExecutionId, inputRowId, outputRowId).then(function(data) {
                        var href = '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + outputRowId;
                        restApi.update(href, updatedMappedEntity, createCallBackFunction(), true);
                    });
                }
            });
        });


        // Add Match button for users to use selected terms
        var $matchConfirmButton = $('<button type="button" class="btn btn-primary btn-match-selected">Match selected</button>').click(function () {
            // Get selected rows from page
            var checkedRows = $('tr.term-row:has(input[type="checkbox"]:checked)');
            var selectedTerms = {};
            var storedTerms = {};

            // Create map of selected terms
            checkedRows.each(function () {
                var ontologyTerm = $(this).data('ontologyTerm');
                selectedTerms[ontologyTerm.ontologyTermIRI] = ontologyTerm
            })

            getOutputRows(inputEntity.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                var promises = []
                var inputTermId
                var needsReview = $('#cb-review')[0].checked;

                if (data.items.length > 0) {
                    inputTermId = data.items[0].inputTerm.Identifier
                    $.each(data.items, function( index, item ) {
                        storedTerms[item.matchTerm] = item;
                    });
                } else {
                    console.error('missing input term')
                    return
                }

                // Create toUpdate list (Each stored-term item that is also in the selectedTerms map)
                var toUpdate = [];
                $.each(storedTerms, function (storedKey, storedVal) {
                    if(selectedTerms[storedKey] !== undefined) {
                        toUpdate.push(storedVal)
                    }
                });

                // Create toDelete list (Each stored-term item that is not in the selectedTerms map)
                var toDelete = [];
                $.each(storedTerms, function (storedKey, storedVal) {
                    if(selectedTerms[storedKey] === undefined) {
                        toDelete.push(storedVal)
                    }
                });

                // Create toCreate list (Each selected term item that is not in the storedTerms map)
                var toCreate = [];
                $.each(selectedTerms, function (selectedKey, selectedVal) {
                    if(storedTerms[selectedKey] === undefined) {
                        toCreate.push(selectedVal)
                    }
                });

                // Do Deletes
                $.each(toDelete, function (toDeleteKey, toDeleteVal) {
                    var deleteHref = '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + toDeleteVal.identifier

                    restApi.remove(deleteHref, function () {
                        console.log('delete is done for: (id: ' + toDeleteVal.identifier + ', term: ' + toDeleteKey + ')')
                    })
                });

                // Do Updates
                $.each(toUpdate, function (toUpdateKey, toUpdateVal) {
                    var updateHref = '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + toUpdateVal.identifier

                    var updatedMappedEntity = {
                        identifier: toUpdateVal.identifier,
                        review: needsReview,
                        validated: true,
                        inputTerm: inputTermId,
                        matchTerm: toUpdateVal.matchTerm,
                        score: toUpdateVal.score,
                    };

                    var result = restApi.update(updateHref, updatedMappedEntity, function () {
                        console.log('update is done for: (id: ' + toUpdateVal.identifier + ', term: ' + toUpdateKey + ')')
                    }, false)
                    promises.push(result);
                });

                // Do Creates
                $.each(toCreate, function (toCreateKey, toCreateVal) {
                    var createMappedEntity = {
                        review: needsReview,
                        validated: true,
                        inputTerm: inputTermId,
                        matchTerm: toCreateVal.ontologyTermIRI,
                        score: toCreateVal.Score,
                    };

                    var result = $.ajax({
                        type: 'POST',
                        url: '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId,
                        async: true,
                        data: JSON.stringify(createMappedEntity),
                        contentType: 'application/json'
                    });

                    promises.push(result);
                });


                $.when.apply($, promises).done(function(data) {
                    // Give the aggregate counter backend some time to update.
                    window.setTimeout(function() {
                        molgenis.OntologyService.prototype.renderPage();
                    }, 500);
                });
            });
        });

        var $hoverover = $('<div>Adjusted score <span class="glyphicon glyphicon-info-sign"></span></div>').css({'cursor': 'pointer'}).popover({
            'title': 'Explanation',
            'content': '<p style="color:black;font-weight:normal;">Adjusted scores are derived from the original scores (<strong>lexical similarity</strong>) combined with the weight of the words (<strong>inverse document frequency</strong>)</p>',
            'placement': 'auto',
            'trigger': 'hover',
            'html': true
        });

        var $table = $('<table class="table"></table>');
        $table.on('click', 'input[type=checkbox]', function() {
            var checkedRows = $('tr.term-row:has(input[type="checkbox"]:checked)').length;
            $('.btn-match-selected').attr('disabled', checkedRows === 0);
        });

        var $tbody = $('<tbody></tbody>').appendTo($table);
        var $header = $('<tr />').appendTo($tbody);
        $('<th />').append('Candidate Ontology Term').appendTo($header);
        $('<th />').append('Score').appendTo($header);
        $('<th />').append($hoverover).appendTo($header);
        $('<th />').append('').appendTo($header);

        var $tableDetailTitle = $('<div class="col-md-6 table-detail-header-title">');
        $tableDetailTitle
            .append($('<span>Match Input Term: </span>'))
            .append('<i>' + gatherInputInfoHelper(inputEntity).join(' ') + '</i>');

        if (data.ontologyTerms && data.ontologyTerms.length > 0) {
            $.each(data.ontologyTerms, function (index, ontologyTerm) {
                if (index >= 20) return;
                var row = $('<tr class="term-row"/>').appendTo($tbody);
                row.append(gatherOntologyInfoHelper(inputEntity, ontologyTerm));
                row.append('<td>' + ontologyTerm.Score.toFixed(2) + '%</td>');
                row.append('<td>' + ontologyTerm.Combined_Score.toFixed(2) + '%</td>');
                row.append('<td class="td-checkbox"><input id="cb-' + ontologyTerm.id + '" type="checkbox"></td>');
                row.data('ontologyTerm', ontologyTerm);
            });
        } else {
            $('<tr />')
                .append($('<td>' + gatherInputInfoHelper(inputEntity).join(' ') + '</td>'))
                .append('<td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td>')
                .appendTo($table);
        }
        var $tableDetailActions = $('<div class="col-md-6 table-detail-header-actions"></div>')
            .append($cancelButton)
            .append($matchConfirmButton)
            .append($noMatchButton)
            .append($reviewFlag);


        var $tableDetailHeader = $('<div class="row table-detail-header"></div>')
            .append($tableDetailTitle)
            .append($tableDetailActions);

        $tableDetailContainer
            .append($tableDetailHeader)
            .append($table);

        // Set checkbox state based on currently selected items.
        getOutputRows(inputEntity.Identifier, ontologyServiceRequest.sortaJobExecutionId, function(outputItems) {
            $.each(outputItems.items, function (index, outputItem) {
                var ontologyTerm = findOntologyTerm(data.ontologyTerms, outputItem.matchTerm);
                $('#cb-' + ontologyTerm.id).prop('checked', true);
            });
        });
    }


    function generateListTableRow(groupedEntity, matched, page) {
        var rows = [];

        var inputTerm = groupedEntity[0].inputTerm
        var matchedTerm = groupedEntity[0].matchedTerm

        var inputRowId = groupedEntity[0].inputTerm.Identifier;
        var firstOutputRowId = groupedEntity[0].matchedTerm.identifier;

        $.each(groupedEntity, function(index, entity) {
            var row = $('<tr />');
            rows.push(row);

            if (index === 0) {
                var $nameTd = $('<td rowspan="' + groupedEntity.length + '"></td>');
                $nameTd.append(gatherInputInfoHelper(inputTerm).join('<br/>'));
                // Only matched input terms could have multiple rows for candidates.
                if (matched) {
                    $nameTd.css({'vertical-align': 'top'});
                }
                $nameTd.appendTo(row);
            }

            var $ontologyTd = gatherOntologyInfoHelper(entity.inputTerm, entity.ontologyTerm).appendTo(row);
            var $scoreTd = $('<td />').appendTo(row);
            var $matchedTd = $('<td class="td-manual-match"/>').appendTo(row);
            var $optionsTd = $('<td class="input-term-options"/>').appendTo(row);

            var score = entity.matchedTerm.score ? entity.matchedTerm.score.toFixed(2) + '%' : NO_MATCH_INFO;
            $scoreTd.append(score);

            if (index !== 0) { return }

            if (!matched) {
                var matchButton = $('<button class="btn btn-default" type="button">Match</button>').click(function () {
                    matchEntity(inputTerm.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                        generateDetailTable(inputTerm, data, row, page);
                    })
                });
                $optionsTd.append(matchButton);
                return;
            }

            $matchedTd.append('<span class="glyphicon ' + (matchedTerm.validated ? 'glyphicon-ok-sign text-success' : 'glyphicon-remove-sign text-light') + '"></span>');

            if (matchedTerm.review) {
                $matchedTd.append('<span class="glyphicon glyphicon-flag text-danger"></span>');
            }

            if (matchedTerm.validated) {
                var $trashButton = $('<button type="button" class="btn btn-danger"><i class="glyphicon glyphicon-trash"</i></button>');

                $trashButton.click(function () {
                    matchEntity(inputRowId, ontologyServiceRequest.sortaJobExecutionId, function (data) {

                        var updatedMappedEntity = {};
                        $.map(matchedTerm, function (val, key) {
                            updatedMappedEntity[key] = val;
                            if (key === 'validated') updatedMappedEntity[key] = false;
                            if (key === 'inputTerm') updatedMappedEntity[key] = val.Identifier;
                        });
                        if (data.ontologyTerms && data.ontologyTerms.length > 0) {
                            var ontologyTerm = data.ontologyTerms[0];
                            updatedMappedEntity['score'] = ontologyTerm.Score;
                            updatedMappedEntity['matchTerm'] = ontologyTerm.ontologyTermIRI;
                        } else {
                            updatedMappedEntity['score'] = 0;
                            updatedMappedEntity['matchTerm'] = null;
                        }

                        deleteUnusedRows(ontologyServiceRequest.sortaJobExecutionId, inputRowId, firstOutputRowId).then(function() {
                            restApi.update('/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + firstOutputRowId, updatedMappedEntity, createCallBackFunction(), true);
                        });
                    });
                });
                $optionsTd.append($trashButton);
            }
        });

        return rows;
    }


    function getOntologyTermSynonyms(ontologyTerm) {
        var synonyms = [];
        if (ontologyTerm.ontologyTermSynonym.length > 0) {
            $.each(ontologyTerm.ontologyTermSynonym, function (index, ontologyTermSynonymEntity) {
                if (ontologyTerm.ontologyTermName !== ontologyTermSynonymEntity.ontologyTermSynonym && $.inArray(ontologyTermSynonymEntity.ontologyTermSynonym, synonyms) === -1) {
                    synonyms.push(ontologyTermSynonymEntity.ontologyTermSynonym);
                }
            });
        }
        return synonyms;
    }



    function getOutputRows(inputTermIdentifier, sortaJobExecutionId, callback) {
        restApi.getAsync('/api/v1/' + sortaJobExecutionId, {
            'q': [{
                'field': 'inputTerm',
                'operator': 'EQUALS',
                'value': inputTermIdentifier
            }],
            'expand': ['inputTerm']
        }, function (data) {
            if (callback) callback(data);
        });
    }


    function getMatchResultCount() {
        var url = '/api/v2/'+ ontologyServiceRequest.sortaJobExecutionId + '/?q=(score=ge=' + window.sorta.threshold + ';score=le=100),validated==true&aggs=x==inputTerm';
        $.ajax({
            type: 'GET',
            url: url,
            contentType: 'application/json',
            success: function (distinctCount) {
                $.ajax({
                    type: 'GET',
                    url: molgenis.getContextUrl() + '/count/' + ontologyServiceRequest.sortaJobExecutionId,
                    contentType: 'application/json',
                    success: function (data) {
                        data.numberOfMatched = distinctCount.aggs.matrix.length
                        var page = {};
                        var totalMatched = data.numberOfMatched;
                        var totalUnMatched = data.numberOfUnmatched;
                        page.total = ontologyServiceRequest.matched ? totalMatched : totalUnMatched;

                        // Reset pager between rendering pages.
                        ontologyServiceRequest.entityPager = {start: 0, num: itemsPerPage, total: page.total};

                        updatePageFunction(page);
                        $('#matched-item-count').html(totalMatched);
                        $('#unmatched-item-count').html(totalUnMatched);
                    }
                });
            }
        });
    }


    function matchEntity(inputTermIdentifier, sortaJobExecutionId, callback) {
        $.ajax({
            type: 'POST',
            url: molgenis.getContextUrl() + '/match/entity',
            async: false,
            data: JSON.stringify({'identifier': inputTermIdentifier, 'sortaJobExecutionId': sortaJobExecutionId}),
            contentType: 'application/json',
            success: function (data) {
                if (data.message) throw data.message;
                if (callback) callback(data);
            }
        });
    }


    function orderByMatchScore(a, b) {
        if(!a.matchedTerm || !b.matchedTerm ) {
            return 0;
        } else {
            return a.matchedTerm.validated < b.matchedTerm.validated;
        }
    }


    function orderByValidated(a, b) {
        if(!a.matchedTerm || !b.matchedTerm  ) {
            return 0;
        } else {
            return (a.matchedTerm.validated === b.matchedTerm.validated) ? 0 : a.matchedTerm.validated ? -1 : 1;
        }
    }


    function toggleDetailView(enabled) {
        if (enabled) {
            $('.table-list-header').hide();
            $('.pagination').hide();
        } else {
            $('.table-list-header').show();
            $('.pagination').show();
        }
    }


    function updatePageFunction(page) {
        $.extend(ontologyServiceRequest.entityPager, page);
        $.ajax({
            type: 'POST',
            url: molgenis.getContextUrl() + '/match/retrieve',
            async: false,
            data: JSON.stringify(ontologyServiceRequest),
            contentType: 'application/json',
            success: function (data) {
                var $tableContainer = $('<div />').addClass('col-md-12');
                if (data.items.length > 0) {
                    var searchItems = [];
                    searchItems.push('<div class="col-md-3 filter-box">');
                    searchItems.push('<div class="input-group"><span class="input-group-addon">Filter</span>');
                    searchItems.push('<input type="text" class="form-control" value="' + (ontologyServiceRequest.filterQuery ? ontologyServiceRequest.filterQuery : '') + '" />');
                    searchItems.push('<span class="input-group-btn"><button class="btn btn-default"><span class="glyphicon glyphicon-search"></span></button></span>')
                    searchItems.push('</div></div>')

                    var matchResultHeaderDiv = $('<div class="table-list-header" />').addClass('row').appendTo($tableContainer);
                    matchResultHeaderDiv.append(searchItems.join(''));
                    matchResultHeaderDiv.append('<div class="col-md-9 table-list-header-title">' + (ontologyServiceRequest.matched ? 'Matched Input Terms' : 'Unmatched Input Terms') + '</div>');

                    var tableItems = [];
                    tableItems.push('<div class="col-md-12"><table class="table">');
                    tableItems.push('<tr><th>Input Term</th><th>' + (ontologyServiceRequest.matched ? 'Matched Ontology Terms' : 'Best Candidate') + '</th><th>Score</th><th>' + (ontologyServiceRequest.matched ? 'Manual match' : '') + '</th><th></th>' + '</tr>');
                    tableItems.push('</table></div>');
                    $('<div />').addClass('row').append(tableItems.join('')).appendTo($tableContainer);
                    var table = $($tableContainer).find('table:eq(0)')

                    var groupedEntities = {};
                    $.each(data.items, function (index, entity) {
                        if (!groupedEntities[entity.inputTerm.Identifier]) {
                            groupedEntities[entity.inputTerm.Identifier] = [];
                        }

                        groupedEntities[entity.inputTerm.Identifier].push(entity);
                    });

                    var groupedEntitiesList = Object.values(groupedEntities);
                    // Sort outputs within input terms
                    $.each(groupedEntitiesList, function(index, groupedEntity) {
                        groupedEntity.sort(orderByMatchScore);
                    });

                    // Sort input terms
                    groupedEntitiesList.sort(function (a, b) {
                        return orderByMatchScore(a[0], b[0]);
                    })

                    // Sort input terms by validation
                    groupedEntitiesList.sort(function (a, b) {
                        return orderByValidated(a[0], b[0]);
                    })

                    $.each(groupedEntitiesList, function(index, groupedEntity) {
                        table.append(generateListTableRow(groupedEntity, ontologyServiceRequest.matched, page));
                    });

                    var searchButton = matchResultHeaderDiv.find('button:eq(0)');
                    var searchBox = matchResultHeaderDiv.find('input:eq(0)');
                    $(searchButton).click(function() {
                        if (ontologyServiceRequest.filterQuery !== $(searchBox).val()) {
                            ontologyServiceRequest.filterQuery = $(searchBox).val();
                            updatePageFunction(page);
                        }
                        return false;
                    });

                    $(searchBox).keyup(function(e) {
                        //stroke key enter or backspace
                        if (e.keyCode === 13 || $(this).val() === '') {
                            $(searchButton).click();
                        }
                    });

                    // Only use a pager if there are pages to page.
                    if (data.total > itemsPerPage) {
                        var $pagerDiv = $('<div />').addClass('row').appendTo($tableContainer);
                        $pagerDiv.pager({
                            'page': Math.floor(data.start / data.num) + (data.start % data.num == 0 ? 0 : 1) + 1,
                            'nrItems': data.total,
                            'nrItemsPerPage': data.num,
                            'onPageChange': updatePageFunction
                        });
                    }
                } else {
                    var messageItems = [];
                    messageItems.push('<div class="col-md-offset-3 col-md-6"><p>There are no results!</p>');
                    if (ontologyServiceRequest.filterQuery) {
                        messageItems.push('<strong>Clear the query </strong>: ' + ontologyServiceRequest.filterQuery + '&nbsp;&nbsp;');
                        messageItems.push('<span class="glyphicon glyphicon-remove"></span>');
                    }
                    messageItems.push('<br><br></div>');
                    $tableContainer.append(messageItems.join(''));
                    $tableContainer.find('span.glyphicon-remove:eq(0)').click(function () {
                        ontologyServiceRequest.filterQuery = '';
                        updatePageFunction(page);
                    });
                }
                //Remove the existing results
                $resultContainer.children('div.row:gt(0)').empty();
                //Add the new results to the page
                $('<div />').addClass('row').append($tableContainer).appendTo($resultContainer);
            }
        });
    };
}($, window.top.molgenis = window.top.molgenis || {}));