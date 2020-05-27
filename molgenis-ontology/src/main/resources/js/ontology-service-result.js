(function ($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();
    var ontologyServiceRequest = null;
    var $resultContainer = null;
    var reserved_identifier_field = 'identifier';
    var NO_MATCH_INFO = 'N/A';
    var itermsPerPage = 5;

    function findOntology(ontologyTerms, matchTerm) {
        var match = null;
        $.each(ontologyTerms, function (index, ontologyTerm) {
            if (ontologyTerm.ontologyTermIRI === matchTerm) {
                match = ontologyTerm;
            }
        })

        return match;
    }

    molgenis.OntologyService = function OntologyService(container, request) {
        if (container) {
            $resultContainer = container;
        }
        if (request) {
            ontologyServiceRequest = request;
            ontologyServiceRequest.entityPager = {'start': 0, 'num': itermsPerPage, 'total': 5};
        }
    };

    function deleteUnusedRows (sortaJobExecutionId, inputRowId, usedOutputRowId) {
        var defer = $.Deferred();
        getMappingEntity(inputRowId, sortaJobExecutionId, function (data) {
            $.each(data.items, function (index, outputRow) {
                if (outputRow.identifier !== usedOutputRowId) {
                    restApi.remove('/api/v1/' + sortaJobExecutionId + '/' + outputRow.identifier);
                }
            })
            defer.resolve(data);
        });

        return defer.promise();
    }

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

        function getMatchResultCount(callback) {
            $.ajax({
                type: 'GET',
                url: molgenis.getContextUrl() + '/count/' + ontologyServiceRequest.sortaJobExecutionId,
                contentType: 'application/json',
                success: function (data) {
                    if (callback !== null && typeof callback === 'function') {
                        callback(data)
                    }
                }
            });
        }

        getMatchResultCount(function (data) {
            var page = {};
            var totalMatched = data.numberOfMatched;
            var totalUnMatched = data.numberOfUnmatched;
            page.total = ontologyServiceRequest.matched ? totalMatched : totalUnMatched;
            updatePageFunction(page);
            $('#matched-item-count').html(totalMatched);
            $('#unmatched-item-count').html(totalUnMatched);
        });

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


    function uuidv4() {
        return 'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, function(c) {
            var r = Math.random() * 16 | 0, v = c == 'x' ? r : (r & 0x3 | 0x8);
            return v.toString(16);
        });
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

                    var matchResultHeaderDiv = $('<div />').addClass('row').css({'margin-bottom': '10px'}).appendTo($tableContainer);
                    matchResultHeaderDiv.append('<div class="col-md-9 table-title">' + (ontologyServiceRequest.matched ? 'Matched Input Terms' : 'Unmatched Input Terms') + '</div>');
                    matchResultHeaderDiv.append(searchItems.join(''));

                    var tableItems = [];
                    tableItems.push('<div class="col-md-12"><table class="table">');
                    tableItems.push('<tr><th style="width:38%;">Input Term</th><th style="width:38%;">' + (ontologyServiceRequest.matched ? 'Matched' : 'Unmatched') + ' Ontology Terms</th><th style="width:10%;">Score</th><th style="width:10%;">' + (ontologyServiceRequest.matched ? 'Manual Match' : '') + '</th><th>Options</th>' + '</tr>');
                    tableItems.push('</table></div>');
                    $('<div />').addClass('row').append(tableItems.join('')).appendTo($tableContainer);
                    var table = $($tableContainer).find('table:eq(0)')

                    var groupedEntities = {}
                    $.each(data.items, function (index, entity) {
                        if (!groupedEntities[entity.inputTerm.Identifier]) {
                            groupedEntities[entity.inputTerm.Identifier] = []
                        }

                        groupedEntities[entity.inputTerm.Identifier].push(entity);
                    });

                    $.each(Object.keys(groupedEntities), function(index, entityId) {
                        table.append(createRowForMatchedTerm(groupedEntities[entityId], ontologyServiceRequest.matched, page));
                    });

                    var searchButton = matchResultHeaderDiv.find('button:eq(0)');
                    var searchBox = matchResultHeaderDiv.find('input:eq(0)');
                    $(searchButton).click(function () {
                        if (ontologyServiceRequest.filterQuery !== $(searchBox).val()) {
                            ontologyServiceRequest.filterQuery = $(searchBox).val();
                            updatePageFunction(page);
                        }
                        return false;
                    });

                    $(searchBox).keyup(function (e) {
                        //stroke key enter or backspace
                        if (e.keyCode === 13 || $(this).val() === '') {
                            $(searchButton).click();
                        }
                    });

                    var $pagerDiv = $('<div />').addClass('row').appendTo($tableContainer);
                    $pagerDiv.pager({
                        'page': Math.floor(data.start / data.num) + (data.start % data.num == 0 ? 0 : 1) + 1,
                        'nrItems': data.total,
                        'nrItemsPerPage': data.num,
                        'onPageChange': updatePageFunction
                    });


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

    function createRowForMatchedTerm(groupedEntity, matched, page) {
        var rows = [];

        var inputTerm = groupedEntity[0].inputTerm
        var matchedTerm = groupedEntity[0].matchedTerm

        var inputRowId = groupedEntity[0].inputTerm.Identifier;
        var firstOutputRowId = groupedEntity[0].matchedTerm.identifier;

        $.each(groupedEntity, function(index, entity) {
            var row = $('<tr />');
            rows.push(row);

            if (index === 0) {
                var $nameTd = gatherInputInfoHelper(inputTerm);
                $nameTd.attr('rowspan', groupedEntity.length);
                $nameTd.appendTo(row);
            }

            var $ontologyTd = gatherOntologyInfoHelper(entity.inputTerm, entity.ontologyTerm).appendTo(row);
            var $scoreTd = $('<td />').appendTo(row);
            var $matchedTd = $('<td />').appendTo(row);
            var $optionsTd = $('<td />').appendTo(row);

            var score = entity.matchedTerm.score ? entity.matchedTerm.score.toFixed(2) + '%' : NO_MATCH_INFO;
            $scoreTd.append(score);

            if (index !== 0) { return }

            if (!matched) {
                var matchButton = $('<button class="btn btn-default" type="button">Match</button>').click(function () {
                    matchEntity(inputTerm.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                        createTableForCandidateMappings(inputTerm, data, row, page);
                    })
                });
                $optionsTd.append(matchButton);
                return;
            }

            $matchedTd.append('<span class="glyphicon ' + (matchedTerm.validated ? 'glyphicon-ok' : 'glyphicon-remove') + '"></span>');
            if (matchedTerm.validated) {
                var $trashButton = $('<button type="button" class="btn btn-default"><span class="glyphicon glyphicon-trash"</span></button>');
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

    function toggleDetailView(enabled) {
        if (enabled) {
            $('.filter-box').hide();
            $('.table-title').hide();
            $('.pagination').hide();
        } else {
            $('.filter-box').show();
            $('.table-title').show();
            $('.pagination').show();
        }
    }

    function createTableForCandidateMappings(inputEntity, data, row, page) {
        toggleDetailView(true);

        if (data.message) {
            console.log('Error fetching candidate mappings', data.message);
            throw data.message;
        }
        var $container = $('<div class="row"></div>').css({'margin-bottom': '20px'});
        //Hide existing table
        row.parents('table:eq(0)').hide();
        //Add table containing candidate matches to the view
        row.parents('div:eq(0)').append($container);

        //Add a cancelButton for users to go back to previous summary table.
        var $cancelButton = $('<button type="button" class="btn btn-default">Cancel</button>').css({
            'margin-bottom': '10px',
            'float': 'right'
        }).click(function () {
            $container.remove();
            row.parents('table:eq(0)').show();
            toggleDetailView(false);
        });
        //Add a unknownButton for users to choose 'Unknown' for the input term
        var $unknownButton = $('<button type="button" class="btn btn-danger">No match</button>').css({
            'margin-bottom': '10px',
            'margin-right': '10px',
            'float': 'right'
        }).click(function () {
            var inputRowId = inputEntity.Identifier;
            getMappingEntity(inputRowId, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                if (data.items.length > 0) {
                    var outputRow = data.items[0];
                    var outputRowId = outputRow.identifier

                    var updatedMappedEntity = {};
                    $.map(outputRow, function (val, key) {
                        updatedMappedEntity[key] = val;
                        if (key === 'validated') updatedMappedEntity[key] = true;
                        if (key === 'inputTerm') {
                            updatedMappedEntity[key] = val.Identifier;
                        }
                        if (key === 'score') updatedMappedEntity[key] = 0;
                        if (key === 'matchTerm') updatedMappedEntity[key] = null;
                    });

                    deleteUnusedRows(ontologyServiceRequest.sortaJobExecutionId, inputRowId, outputRowId).then(function(data) {
                        var href = '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + outputRowId;
                        restApi.update(href, updatedMappedEntity, createCallBackFunction(), true);
                    });
                }
            });
        });

        // Add Match button for users to use selected terms
        var $matchConfirmButton = $('<button type="button" class="btn btn-primary btn-match-selected">Match selected</button>').css({
            'margin-bottom': '10px',
            'margin-right': '10px',
            'float': 'right'
        }).click(function () {
            // Get selected rows from page
            var checkedRows = $('tr.term-row:has(input[type="checkbox"]:checked)');
            var selectedTerms = {};
            var storedTerms = {};

            // Create map of selected terms
            checkedRows.each(function () {
                var ontologyTerm = $(this).data('ontologyTerm');
                selectedTerms[ontologyTerm.ontologyTermIRI] = ontologyTerm
            })

            getMappingEntity(inputEntity.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                var promises = []
                var inputTermId

                if (data.items.length > 0) {
                    inputTermId = data.items[0].inputTerm.Identifier
                    $.each(data.items, function( index, item ) {
                        storedTerms[item.matchTerm] = item;
                    });
                } else {
                    console.error('missing input term')
                    return
                }

                var toUpdate = [];
                var toDelete = [];

                $.each(storedTerms, function (storedKey, storedVal) {
                    if(selectedTerms[storedKey] !== undefined) {
                        toUpdate.push(storedVal)
                    } else {
                        toDelete.push(storedVal)
                    }
                });

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
                    var updatedMappedEntity = {};

                    $.map(toUpdateVal, function (val, key) {
                        if (key === 'validated') updatedMappedEntity[key] = true;
                        else if (key === 'inputTerm') updatedMappedEntity[key] = val.Identifier;
                        else if (key === 'matchTerm') updatedMappedEntity.matchTerm = toUpdateVal.ontologyTermIRI;
                        else if (key === 'score') updatedMappedEntity.score = toUpdateVal.Score;
                        else updatedMappedEntity[key] = val;
                    });
                    var result = restApi.update(updateHref, updatedMappedEntity, function () {
                        console.log('update is done for: (id: ' + toUpdateVal.identifier + ', term: ' + toUpdateKey + ')')
                    }, false)
                    promises.push(result);
                });

                // Do Creates
                $.each(toCreate, function (toCreateKey, toCreateVal) {
                    var createMappedEntity = {
                        identifier: uuidv4(),
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
                    molgenis.OntologyService.prototype.renderPage();
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
        $('<th />').append('Input Term').css({'width': '30%'}).appendTo($header);
        $('<th />').append('Candidate Ontology Term').css({'width': '40%'}).appendTo($header);
        $('<th />').append('Score').css({'width': '12%'}).appendTo($header);
        $('<th />').append($hoverover).css({'width': '12%'}).appendTo($header);
        $('<th />').append('').appendTo($header);

        var $hintInformation;
        if (data.ontologyTerms && data.ontologyTerms.length > 0) {
            $hintInformation = $('<span class="glyphicon glyphicon-info-sign"></span> <em class="hint-info"> The candidate ontology terms are sorted based on similarity score; please select all relevant terms using the checkboxes.</em>');
            $.each(data.ontologyTerms, function (index, ontologyTerm) {
                if (index >= 20) return;
                var row = $('<tr class="term-row"/>').appendTo($tbody);
                row.append(index == 0 ? gatherInputInfoHelper(inputEntity) : '<td></td>');
                row.append(gatherOntologyInfoHelper(inputEntity, ontologyTerm));
                row.append('<td>' + ontologyTerm.Score.toFixed(2) + '%</td>');
                row.append('<td>' + ontologyTerm.Combined_Score.toFixed(2) + '%</td>');
                row.append('<td style="text-align: center;"><input id="cb-' + ontologyTerm.id + '" type="checkbox"></td>');
                row.data('ontologyTerm', ontologyTerm);
            });
        } else {
            $hintInformation = $('<center><p style="font-size:15px;">There are no candidate mappings for this input term!</p></center>');
            $('<tr />').append(gatherInputInfoHelper(inputEntity)).append('<td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td>').appendTo($table);
        }
        $('<div class="col-md-12"></div>')
        .append($cancelButton)
        .append($unknownButton)
        .append($matchConfirmButton)
        .append($table)
        .append($hintInformation)
        .appendTo($container);

        // Set checkbox state based on currently selected items.
        getMappingEntity(inputEntity.Identifier, ontologyServiceRequest.sortaJobExecutionId, function(res) {
            $.each(res.items, function (index, outMapping) {
                var ontology = findOntology(data.ontologyTerms, outMapping.matchTerm)
                if (ontology) {
                    $('#cb-' + ontology.id).prop('checked', true);
                }
            })
        });

    }

    function createCallBackFunction() {
        return {
            'success': function () {
                molgenis.OntologyService.prototype.renderPage();
            }, 'error': function () {
                molgenis.OntologyService.prototype.renderPage();
            }
        };
    }

    function getMappingEntity(inputTermIdentifier, sortaJobExecutionId, callback) {
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

    function gatherInputInfoHelper(inputTerm) {
        var inputTermTd = $('<td />');
        if (inputTerm) {
            $.map(inputTerm ? inputTerm : {}, function (val, key) {
                if (key.toLowerCase() !== reserved_identifier_field.toLowerCase()) inputTermTd.append('<div>' + key + ': ' + val + '</div>');
            });
        }
        return inputTermTd;
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
                if (key.toLowerCase() !== 'name' && key.toLowerCase().search('synonym') === -1 && key.toLowerCase() !== reserved_identifier_field.toLowerCase()) {
                    $ontologyTermContainer.append('<div>' + key + ' : ' + (annotationMap[key] ? annotationMap[key].join() : 'N/A') + '</div>');
                }
            });
        } else {
            $ontologyTermContainer.append(NO_MATCH_INFO);
        }
        return $ontologyTermContainer;
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

}($, window.top.molgenis = window.top.molgenis || {}));