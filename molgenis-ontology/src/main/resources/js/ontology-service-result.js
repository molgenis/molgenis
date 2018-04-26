(function ($, molgenis) {
    "use strict";

    var restApi = new molgenis.RestClient();
    var ontologyServiceRequest = null;
    var result_container = null;
    var reserved_identifier_field = 'identifier';
    var NO_MATCH_INFO = 'N/A';
    var itermsPerPage = 5;

    molgenis.OntologyService = function OntologySerivce(container, request) {
        if (container) {
            result_container = container;
        }
        if (request) {
            ontologyServiceRequest = request;
            ontologyServiceRequest.entityPager = {'start': 0, 'num': itermsPerPage, 'total': 5};
        }
    };

    molgenis.OntologyService.prototype.renderPage = function () {

        var items = [];
        items.push('<div class="row"><div class="col-md-offset-3 col-md-6 well">');
        items.push('<div class="row">');
        items.push('<div class="col-md-8">The total number of matched items is <strong><span id="matched-item-count"></span></strong></div>');
        items.push('<div class="col-md-4"><button id="matched-result-button" type="button" class="btn btn-primary pull-right">Show</button></div>');
        items.push('</div><br>');
        items.push('<div class="row">');
        items.push('<div class="col-md-8">The total number of unmatched items is <strong><span id="unmatched-item-count"></span></strong></div>');
        items.push('<div class="col-md-4"><button id="unmatched-result-button" type="button" class="btn btn-info pull-right">Show</button></div>');
        items.push('</div><br>');
        items.push('<div class="row">');
        items.push('<div class="col-md-12"><button id="sorta-download-button" class="btn btn-primary" type="button">Download</button></div>');
        items.push('</div>');
        items.push('</div></div>');
        result_container.empty().append(items.join(''));

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
    };

    function updatePageFunction(page) {

        $.extend(ontologyServiceRequest.entityPager, page);

        $.ajax({
            type: 'POST',
            url: molgenis.getContextUrl() + '/match/retrieve',
            async: false,
            data: JSON.stringify(ontologyServiceRequest),
            contentType: 'application/json',
            success: function (data) {
                var table_container = $('<div />').addClass('col-md-12');
                if (data.items.length > 0) {
                    var pagerDiv = $('<div />').addClass('row').appendTo(table_container);
                    var searchItems = [];
                    searchItems.push('<div class="col-md-3">');
                    searchItems.push('<div class="input-group"><span class="input-group-addon">Filter</span>');
                    searchItems.push('<input type="text" class="form-control" value="' + (ontologyServiceRequest.filterQuery ? ontologyServiceRequest.filterQuery : '') + '" />');
                    searchItems.push('<span class="input-group-btn"><button class="btn btn-default"><span class="glyphicon glyphicon-search"></span></button></span>')
                    searchItems.push('</div></div>')

                    var matchResultHeaderDiv = $('<div />').addClass('row').css({'margin-bottom': '10px'}).appendTo(table_container);
                    matchResultHeaderDiv.append(searchItems.join(''));
                    matchResultHeaderDiv.append('<div class="col-md-6"><center><strong><p style="font-size:20px;">' + (ontologyServiceRequest.matched ? 'Matched result' : 'Unmatched result') + '</p></strong></center></div>');

                    var tableItems = [];
                    tableItems.push('<div class="col-md-12"><table class="table">');
                    tableItems.push('<tr><th style="width:38%;">Input term</th><th style="width:38%;">Best candidate</th><th style="width:10%;">Score</th><th style="width:10%;">Manual Match</th>' + (ontologyServiceRequest.matched ? '<th>Remove</th>' : '') + '</tr>');
                    tableItems.push('</table></div>');
                    $('<div />').addClass('row').append(tableItems.join('')).appendTo(table_container);
                    var table = $(table_container).find('table:eq(0)')

                    $.each(data.items, function (index, entity) {
                        table.append(createRowForMatchedTerm(entity, ontologyServiceRequest.matched, page));
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

                    $(pagerDiv).pager({
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
                    table_container.append(messageItems.join(''));
                    $(table_container).find('span.glyphicon-remove:eq(0)').click(function () {
                        ontologyServiceRequest.filterQuery = '';
                        updatePageFunction(page);
                    });
                }
                //Remove the existing results
                result_container.children('div.row:gt(0)').empty();
                //Add the new results to the page
                $('<div />').addClass('row').append(table_container).appendTo(result_container);
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

    function createRowForMatchedTerm(responseData, matched, page) {
        var row = $('<tr />');
        row.append(gatherInputInfoHelper(responseData.inputTerm));
        row.append(gatherOntologyInfoHelper(responseData.inputTerm, responseData.ontologyTerm));
        $('<td />').append(responseData.matchedTerm.score ? responseData.matchedTerm.score.toFixed(2) + '%' : NO_MATCH_INFO).appendTo(row);
        if (matched) {
            $('<td />').append('<span class="glyphicon ' + (responseData.matchedTerm.validated ? 'glyphicon-ok' : 'glyphicon-remove') + '"></span>').appendTo(row);
            $('<td />').append(responseData.matchedTerm.validated ? '<button type="button" class="btn btn-default"><span class="glyphicon glyphicon-trash"</span></button>' : '').appendTo(row);
            row.find('button:eq(0)').click(function () {
                matchEntity(responseData.inputTerm.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                    var updatedMappedEntity = {};
                    $.map(responseData.matchedTerm, function (val, key) {
                        if (key !== 'identifier') updatedMappedEntity[key] = val;
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
                    restApi.update('/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + responseData.matchedTerm.identifier, updatedMappedEntity, createCallBackFunction(), true);
                });
            });
        } else {
            var button = $('<button class="btn btn-default" type="button">Match</button>').click(function () {
                matchEntity(responseData.inputTerm.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                    createTableForCandidateMappings(responseData.inputTerm, data, row, page);
                })
            });
            $('<td />').append(button).appendTo(row);
        }
        return row;
    }

    function createTableForCandidateMappings(inputEntity, data, row, page) {
        if (data.message) {
            console.log('Error fetching candidate mappings', data.message);
            throw data.message;
        }
        var container = $('<div class="row"></div>').css({'margin-bottom': '20px'});
        //Hide existing table
        row.parents('table:eq(0)').hide();
        //Add table containing candidate matches to the view
        row.parents('div:eq(0)').append(container);

        //Add a backButton for users to go back to previous summary table
        var backButton = $('<button type="button" class="btn btn-warning">Cancel</button>').css({
            'margin-bottom': '10px',
            'float': 'right'
        }).click(function () {
            container.remove();
            row.parents('table:eq(0)').show();
        });
        //Add a unknownButton for users to choose 'Unknown' for the input term
        var unknownButton = $('<button type="button" class="btn btn-danger">No match</button>').css({
            'margin-bottom': '10px',
            'margin-right': '10px',
            'float': 'right'
        }).click(function () {
            getMappingEntity(inputEntity.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                if (data.items.length > 0) {
                    var mappedEntity = data.items[0];
                    var href = '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + mappedEntity.identifier;
                    var updatedMappedEntity = {};
                    $.map(mappedEntity, function (val, key) {
                        updatedMappedEntity[key] = val;
                        if (key === 'validated') updatedMappedEntity[key] = true;
                        if (key === 'inputTerm') {
                            console.log('inputTerm', val)
                            updatedMappedEntity[key] = val.Identifier;
                        }
                        if (key === 'score') updatedMappedEntity[key] = 0;
                        if (key === 'matchTerm') updatedMappedEntity[key] = null;
                    });
                    restApi.update(href, updatedMappedEntity, createCallBackFunction(), true);
                }
            });
        });

        var hoverover = $('<div>Adjusted score ?</div>').css({'cursor': 'pointer'}).popover({
            'title': 'Explanation',
            'content': '<p style="color:black;font-weight:normal;">Adjusted scores are derived from the original scores (<strong>lexical similarity</strong>) combined with the weight of the words (<strong>inverse document frequency</strong>)</p>',
            'placement': 'auto',
            'trigger': 'hover',
            'html': true
        });

        var table = $('<table class="table"></table>');
        var header = $('<tr />').appendTo(table);
        $('<th />').append('Input term').css({'width': '30%'}).appendTo(header);
        $('<th />').append('Candidate mapping').css({'width': '40%'}).appendTo(header);
        $('<th />').append('Score').css({'width': '12%'}).appendTo(header);
        $('<th />').append(hoverover).css({'width': '12%'}).appendTo(header);
        $('<th />').append('Select').appendTo(header);

        var hintInformation;
        if (data.ontologyTerms && data.ontologyTerms.length > 0) {
            hintInformation = $('<center><p style="font-size:15px;">The candidate ontology terms are sorted based on similarity score, please select one of them by clicking <span class="glyphicon glyphicon-ok"></span> button</p></center>');
            $.each(data.ontologyTerms, function (index, ontologyTerm) {
                if (index >= 20) return;
                var row = $('<tr />').appendTo(table);
                row.append(index == 0 ? gatherInputInfoHelper(inputEntity) : '<td></td>');
                row.append(gatherOntologyInfoHelper(inputEntity, ontologyTerm));
                row.append('<td>' + ontologyTerm.Score.toFixed(2) + '%</td>');
                row.append('<td>' + ontologyTerm.Combined_Score.toFixed(2) + '%</td>');
                row.append('<td><button type="button" class="btn btn-default"><span class="glyphicon glyphicon-ok"></span></button></td>');
                row.data('ontologyTerm', ontologyTerm);
                row.find('button:eq(0)').click(function () {
                    getMappingEntity(inputEntity.Identifier, ontologyServiceRequest.sortaJobExecutionId, function (data) {
                        if (data.items.length > 0) {
                            var mappedEntity = data.items[0];
                            var href = '/api/v1/' + ontologyServiceRequest.sortaJobExecutionId + '/' + mappedEntity.identifier;
                            var updatedMappedEntity = {};
                            $.map(mappedEntity, function (val, key) {
                                if (key === 'validated') updatedMappedEntity[key] = true;
                                else if (key === 'inputTerm') updatedMappedEntity[key] = val.Identifier;
                                else if (key === 'matchTerm') updatedMappedEntity.matchTerm = row.data('ontologyTerm').ontologyTermIRI;
                                else if (key === 'score') updatedMappedEntity.score = row.data('ontologyTerm').Score;
                                else updatedMappedEntity[key] = val;
                            });
                            restApi.update(href, updatedMappedEntity, createCallBackFunction(), true);
                        }
                    });
                });
            });
        } else {
            hintInformation = $('<center><p style="font-size:15px;">There are no candidate mappings for this input term!</p></center>');
            $('<tr />').append(gatherInputInfoHelper(inputEntity)).append('<td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td><td>' + NO_MATCH_INFO + '</td>').appendTo(table);
        }
        $('<div class="col-md-12"></div>').append(hintInformation).append(backButton).append(unknownButton).append(table).appendTo(container);
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
                if (key.toLowerCase() !== reserved_identifier_field.toLowerCase()) inputTermTd.append('<div>' + key + ' : ' + val + '</div>');
            });
        }
        return inputTermTd;
    }

    function gatherOntologyInfoHelper(inputEntity, ontologyTerm) {
        var ontologyTermTd = $('<td />');
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
                ontologyTermTd.append('<div>Name : <a href="' + ontologyTerm.ontologyTermIRI + '" target="_blank">' + ontologyTerm.ontologyTermName + '</a></div>').append(synonymDiv);
            } else {
                ontologyTermTd.append('<div>Name : ' + ontologyTerm.ontologyTermName + '</div>').append(synonymDiv);
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
                    ontologyTermTd.append('<div>' + key + ' : ' + (annotationMap[key] ? annotationMap[key].join() : 'N/A') + '</div>');
                }
            });
        } else {
            ontologyTermTd.append(NO_MATCH_INFO);
        }
        return ontologyTermTd;
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