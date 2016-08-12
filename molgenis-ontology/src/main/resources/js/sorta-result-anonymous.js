(function ($, molgenis) {
    "use strict";

    var NOT_AVAILABLE = 'N/A';
    var container = null;
    var THRESHOLD = 100;

    molgenis.SortaAnonymous = function SortaAnonymous(form_container) {
        container = form_container;
    };

    molgenis.SortaAnonymous.prototype.renderPage = function () {

        container.children('div.row:gt(0)').remove();

        var divContainerThreshold = $('<div />').addClass('row').css('margin-bottom', '15px').appendTo(container);
        $('<div />').addClass('col-md-offset-3 col-md-4').append('Current threshold : ' + THRESHOLD + '%').css('padding-left', '0px').appendTo(divContainerThreshold);
        var thresholdUpdateButton = $('<button />').attr('type', 'button').addClass('btn btn-default').text('Update');
        var inputGroupButton = $('<span />').addClass('input-group-btn').append(thresholdUpdateButton);
        var thresholdValue = $('<input type="text" class="form-control"/>');
        var divContainerButtonFormGroup = $('<div />').addClass('input-group pull-right').append(inputGroupButton).append(thresholdValue);
        $('<div />').addClass('col-md-2').css('padding-right', '0px').append(divContainerButtonFormGroup).appendTo(divContainerThreshold);

        var divContainerSummary = $('<div />').addClass('row').appendTo(container);
        var divContainerSummaryCenterStyleWell = $('<div />').addClass('col-md-offset-3 col-md-6 well').appendTo(divContainerSummary);

        var divContainerMatchButton = $('<div />').addClass('row').appendTo(divContainerSummaryCenterStyleWell);
        var matchButon = $('<button />').attr('type', 'button').addClass('btn btn-primary pull-right').text('Show');
        var matchSpanContainer = $('<span />');
        $('<div />').addClass('col-md-12').append('Total number of match results is ').append(matchSpanContainer).append(matchButon).appendTo(divContainerMatchButton);


        var divContainerUnMatchButton = $('<div />').addClass('row').prepend('<br>').appendTo(divContainerSummaryCenterStyleWell);
        var unmatchButon = $('<button />').attr('type', 'button').addClass('btn btn-info pull-right').text('Show');
        var unmatchSpanContainer = $('<span />');
        $('<div />').addClass('col-md-12').append('Total number of unmatch results is ').append(unmatchSpanContainer).append(unmatchButon).appendTo(divContainerUnMatchButton);

        var divContainerDownloadButton = $('<div />').addClass('row').prepend('<br>').appendTo(divContainerSummaryCenterStyleWell);
        var downloadButton = $('<button />').attr('type', 'button').addClass('btn btn-primary').text('Download');
        var backToFrontPageButton = $('<button />').attr('type', 'button').addClass('btn btn-default').html('<strong>New task</strong>&nbsp;').append(' <span class="glyphicon glyphicon-new-window"></span>');
        $('<div />').addClass('col-md-2').append(downloadButton).appendTo(divContainerDownloadButton);
        $('<div />').addClass('col-md-2').append(backToFrontPageButton).appendTo(divContainerDownloadButton);

        $(backToFrontPageButton).click(function () {
            container.attr({
                'action': molgenis.getContextUrl(),
                'method': 'GET'
            }).submit();
        });

        $(downloadButton).click(function () {
            container.attr({
                'action': molgenis.getContextUrl() + '/download/',
                'method': 'GET'
            }).submit();
        });

        $(inputGroupButton).click(function () {
            var customThreshold = Number($(thresholdValue).val());
            if (customThreshold && customThreshold !== THRESHOLD) {
                THRESHOLD = customThreshold;
                molgenis.SortaAnonymous.prototype.renderPage();
            }
        });

        $(thresholdValue).keydown(function (e) {
            if (e.keyCode === 13) {
                $(inputGroupButton).click();
            }
        });

        getMatchResults(function (matchedResults) {
            var perfectMatches = [];
            var partialMatches = [];
            $.each(matchedResults, function (index, matchedResult) {
                if (matchedResult.ontologyTerm.length > 0) {
                    var matchedScore = matchedResult.ontologyTerm[0].Combined_Score;
                    if (matchedScore && matchedScore.toFixed(2) >= THRESHOLD) {
                        perfectMatches.push(matchedResult);
                    } else {
                        partialMatches.push(matchedResult);
                    }
                } else {
                    partialMatches.push(matchedResult);
                }
            });

            matchSpanContainer.html('<strong>' + perfectMatches.length + '</strong>');

            unmatchSpanContainer.html('<strong>' + partialMatches.length + '</strong>');

            matchButon.click(function () {
                renderMatchedResultTable(perfectMatches, true);
            });

            unmatchButon.click(function () {
                renderMatchedResultTable(partialMatches, false);
            }).click();
        });


        function getMatchResults(callback) {
            $.ajax({
                type: 'GET',
                url: molgenis.getContextUrl() + '/retrieve',
                contentType: 'application/json',
                success: function (matchedResults) {
                    if (callback !== null && typeof callback === 'function') {
                        callback(matchedResults)
                    }
                }
            });
        }

        function renderMatchedResultTable(matches, isMatched) {

            if ($('#sorta-result-table')) {
                $('#sorta-result-table').remove();
            }
            var divContainerMatchResult = $('<div />').attr('id', 'sorta-result-table').addClass('row').appendTo(container);

            if (matches.length > 0) {

                var adjustedScoreHoverover = $('<div>Adjusted score ?</div>').css({'cursor': 'pointer'}).popover({
                    'title': 'Explanation',
                    'content': '<p style="color:black;font-weight:normal;">Adjusted scores are derived from the original scores (<strong>lexical similarity</strong>) combined with the weight of the words (<strong>inverse document frequency</strong>)</p>',
                    'placement': 'top',
                    'trigger': 'hover',
                    'html': true
                });
                var tableTitle = $('<p />').css('font-size', '20px').append('<strong>' + (isMatched ? 'Matched results' : 'Unmatched results') + '</strong>');
                var table = $('<table />').addClass('table');
                var tableHeader = $('<tr />').appendTo(table);
                $('<th />').append('Input term').appendTo(tableHeader);
                $('<th />').append('Ontologgy terms').appendTo(tableHeader);
                $('<th />').append('Score').appendTo(tableHeader);
                $('<th />').append(adjustedScoreHoverover).appendTo(tableHeader);
                $('<th />').append('Match').appendTo(tableHeader);
                $.each(matches, function (index, match) {
                    var row = $('<tr />').appendTo(table);
                    var firstOntologyTerm = match.ontologyTerm ? match.ontologyTerm[0] : null;
                    $('<td />').append(getInputTermInfo(match.inputTerm)).appendTo(row);
                    $('<td />').append(getOntologyTermInfo(firstOntologyTerm)).appendTo(row);
                    $('<td />').append(getMatchScore(firstOntologyTerm)).appendTo(row);
                    $('<td />').append(getMatchAdjustedScore(firstOntologyTerm)).appendTo(row);
                    $('<td />').append(firstOntologyTerm ? '<button type="button" class="btn btn-default">Match</button>' : NOT_AVAILABLE).appendTo(row);
                    row.find('button:eq(0)').click(function () {
                        var clearButton = $('<button />').attr('type', 'button').addClass('btn btn-danger pull-right').css({
                            'margin-top': '-10px',
                            'margin-bottom': '10px'
                        }).text('Clear').insertBefore(table);
                        table.find('tr:not(:first-child)').hide();
                        table.find('tr >th:last-child').hide();
                        table.append(renderCandidateMatchTable(match));
                        clearButton.click(function () {
                            table.find('tr:visible:not(:first-child)').remove();
                            table.find('tr').show();
                            table.find('th').show();
                            $(this).remove();
                        });
                    });
                });
                $('<div />').addClass('col-md-offset-1 col-md-10').append(tableTitle).append(table).appendTo(divContainerMatchResult);
            } else {
                $('<div />').addClass('col-md-offset-3 col-md-6').text('No matches are found!').appendTo(divContainerMatchResult);
            }
            return divContainerMatchResult;
        }

        function renderCandidateMatchTable(match, table) {
            var items = [];
            if (match.ontologyTerm) {
                $.each(match.ontologyTerm, function (index, candidateMatch) {
                    if (index >= 10) return;
                    var candidateMatchRow = $('<tr />');
                    $('<td />').append(index == 0 ? getInputTermInfo(match.inputTerm) : '').appendTo(candidateMatchRow);
                    $('<td />').append(getOntologyTermInfo(candidateMatch)).appendTo(candidateMatchRow);
                    $('<td />').append(getMatchScore(candidateMatch)).appendTo(candidateMatchRow);
                    $('<td />').append(getMatchAdjustedScore(candidateMatch)).appendTo(candidateMatchRow);
                    items.push(candidateMatchRow);
                });
            }
            return items;
        }

        function getInputTermInfo(inputTerm) {
            var inputTermDiv = $('<div />');
            $.map(inputTerm, function (val, key) {
                inputTermDiv.append(key + ' : ' + val).append('<br>');
            });
            return inputTermDiv;
        }

        function getOntologyTermInfo(ontologyTerm) {
            var inputTermDiv = $('<div />');
            if (ontologyTerm) {
                var divContainerOTName = getOntologyTermName(ontologyTerm);
                var divContainerOTSynonym = getOntologyTermSynonyms(ontologyTerm);
                var divContainerOTAnnotation = getOntologyTermAnnotations(ontologyTerm);
                inputTermDiv.append(divContainerOTName).append(divContainerOTSynonym).append(divContainerOTAnnotation);
            } else {
                inputTermDiv.append(NOT_AVAILABLE);
            }
            return inputTermDiv;
        }

        function getOntologyTermName(ontologyTerm) {
            return $('<div />').append('Name : ').append('<a href="' + ontologyTerm.ontologyTermIRI + '" target="_blank">' + ontologyTerm.ontologyTermName + '</a>');
        }

        function getOntologyTermAnnotations(ontologyTerm) {
            var divContainerOTAnnotations = [];
            if (ontologyTerm.ontologyTermDynamicAnnotation.length > 0) {
                var annotationMap = {};
                $.each(ontologyTerm.ontologyTermDynamicAnnotation, function (index, annotation) {
                    if (!annotationMap[annotation.name]) {
                        annotationMap[annotation.name] = [];
                    }
                    annotationMap[annotation.name].push(annotation.value);
                });
                $.map(annotationMap, function (val, key) {
                    divContainerOTAnnotations.push($('<div />').append(key + ' : ' + val.join(', ')));
                });
            }
            return divContainerOTAnnotations;
        }

        function getOntologyTermSynonyms(ontologyTerm) {
            var divContainerOTSynonym = $('<div>Synonym : </div>');
            if (ontologyTerm) {
                var synonyms = [];
                $.each(ontologyTerm.ontologyTermSynonym, function (index, ontologyTermSynonym) {
                    synonyms.push(ontologyTermSynonym.ontologyTermSynonym);
                });
                if (synonyms.length == 1) {
                    divContainerOTSynonym.append(synonyms.join());
                } else {
                    divContainerOTSynonym.addClass('show-popover').append('<strong>' + synonyms.length + ' synonyms, see more details</strong>').popover({
                        'content': synonyms.join('<br><br>'),
                        'placement': 'auto',
                        'trigger': 'hover',
                        'html': true
                    });
                }
            } else {
                divContainerOTSynonym.append(NOT_AVAILABLE);
            }
            return divContainerOTSynonym;
        }

        function getMatchScore(ontologyTerm) {
            return ontologyTerm ? $('<div />').append(ontologyTerm.Score.toFixed(2) + '%') : NOT_AVAILABLE;
        }

        function getMatchAdjustedScore(ontologyTerm) {
            return ontologyTerm ? $('<div />').append(ontologyTerm.Combined_Score.toFixed(2) + '%') : NOT_AVAILABLE;
        }
    };
}($, window.top.molgenis = window.top.molgenis || {}));