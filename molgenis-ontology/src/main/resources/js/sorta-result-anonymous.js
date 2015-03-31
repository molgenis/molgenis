(function($, molgenis) {
	"use strict";
	
	var container = null;
	var threshold = 100;
	
	molgenis.SortaAnonymous = function SortaAnonymous(form_container){
		container = form_container;
	};
	
	molgenis.SortaAnonymous.prototype.renderPage = function(){
		
		
		var divContainerThreshold = $('<div />').addClass('row').css('margin-bottom','15px').appendTo(container);
		$('<div />').addClass('col-md-offset-3 col-md-4').append('Current threshold : ' + threshold + '%').css('padding-left','0px').appendTo(divContainerThreshold);
		var thresholdUpdateButton = $('<button />').attr('type','button').addClass('btn btn-default').text('Update');
		var inputGroupButton = $('<span />').addClass('input-group-btn').append(thresholdUpdateButton);		
		var thresholdValue = $('<input type="text" class="form-control"/>');
		var divContainerButtonFormGroup = $('<div />').addClass('input-group pull-right').append(inputGroupButton).append(thresholdValue);
		$('<div />').addClass('col-md-2').css('padding-right','0px').append(divContainerButtonFormGroup).appendTo(divContainerThreshold);
		
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
		
		
		getMatchResults(function(matchedResults){
			var perfectMatches = [];
			var partialMatches = [];
			$.each(matchedResults, function(index, matchedResult){
				if(matchedResult.ontologyTerm.length > 0){
					if(matchedResult.ontologyTerm[0].Combined_Score === 100){
						perfectMatches.push(matchedResult);
					}else{
						partialMatches.push(matchedResult);
					}
				}
			});
			matchSpanContainer.html('<strong>' + perfectMatches.length + '</strong>');
			unmatchSpanContainer.html('<strong>' + partialMatches.length + '</strong>');
			
			matchButon.click(function(){
				renderMatchedResultTable(perfectMatches, true);
			}).click();
			
			unmatchButon.click(function(){
				renderMatchedResultTable(partialMatches, false);
			});
		});

		
		function getMatchResults(callback){
			$.ajax({
				type : 'GET',
				url : molgenis.getContextUrl() + '/retrieve',
				contentType : 'application/json',
				success : function(matchedResults) {
					if(callback !== null && typeof callback === 'function'){
						callback(matchedResults)
					}
				}
			});
		}
		
		function renderMatchedResultTable(matches, isMatched){
			if($('#sorta-result-table')){			
				$('#sorta-result-table').remove();
			}
			var divContainerMatchResult =  $('<div />').attr('id', 'sorta-result-table').addClass('row').appendTo(container);
			var tableTitle = $('<p />').css('font-size','20px').append('<strong>' + (isMatched ? 'Matched results' : 'Unmatched results') + '</strong>');
			var table = $('<table />').addClass('table');
			var tableHeader = $('<tr />').appendTo(table);
			$('<th />').append('Input term').appendTo(tableHeader);
			$('<th />').append('Ontologgy terms').appendTo(tableHeader);
			$('<th />').append('Score').appendTo(tableHeader);
			$('<th />').append('Adjusted score').appendTo(tableHeader);
			$('<th />').append('Match').appendTo(tableHeader);
			$.each(matches, function(index, match){
				var row = $('<tr />').appendTo(table);
				var firstOntologyTerm = match.ontologyTerm[0];
				$('<td />').append(getInputTermInfo(match.inputTerm)).appendTo(row);
				$('<td />').append(getOntologyTermInfo(firstOntologyTerm)).appendTo(row);
				$('<td />').append(getMatchScore(firstOntologyTerm)).appendTo(row);
				$('<td />').append(getMatchAdjustedScore(firstOntologyTerm)).appendTo(row);
				$('<td />').append('<button type="button" class="btn btn-default">Match</button>').appendTo(row);
				row.find('button:eq(0)').click(function(){
					var clearButton = $('<button />').attr('type','button').addClass('btn btn-danger pull-right').css({'margin-top':'-10px','margin-bottom':'10px'}).text('Clear').insertBefore(table);
					table.find('tr:not(:first-child)').hide();
					table.find('tr >th:last-child').hide();
					table.append(renderCandidateMatchTable(match));
					clearButton.click(function(){
						table.find('tr:visible:not(:first-child)').remove();
						table.find('tr').show();
						table.find('th').show();
						$(this).remove();
					});
				});
			});
			$('<div />').addClass('col-md-offset-2 col-md-8').append(tableTitle).append(table).appendTo(divContainerMatchResult);
			return divContainerMatchResult;
		}
		
		function renderCandidateMatchTable(match, table){
			var items = [];
			$.each(match.ontologyTerm, function(index, candidateMatch){
				if(index >= 10) return;
				var candidateMatchRow = $('<tr />');
				$('<td />').append(index == 0 ? getInputTermInfo(match.inputTerm) : '').appendTo(candidateMatchRow);
				$('<td />').append(getOntologyTermInfo(candidateMatch)).appendTo(candidateMatchRow);
				$('<td />').append(getMatchScore(candidateMatch)).appendTo(candidateMatchRow);
				$('<td />').append(getMatchAdjustedScore(candidateMatch)).appendTo(candidateMatchRow);
				items.push(candidateMatchRow);
			});
			return items;
		}
		
		function getInputTermInfo(inputTerm){
			var inputTermDiv = $('<div />');
			$.map(inputTerm, function(val, key){
				inputTermDiv.append(key + ' : ' + val).append('<br>');
			});
			return inputTermDiv;
		}
		
		function getOntologyTermInfo(ontologyTerm){
			var inputTermDiv = $('<div />');
			if(ontologyTerm){
				var divContainerOTName = getOntologyTermName(ontologyTerm);
				var divContainerOTSynonym = getOntologyTermSynonyms(ontologyTerm);
				inputTermDiv.append(divContainerOTName).append(divContainerOTSynonym);
			}
			return inputTermDiv;
		}
		
		function getOntologyTermName(ontologyTerm){
			return $('<div />').append('Name : ').append('<a href="' + ontologyTerm.ontologyTermIri + '">' + ontologyTerm.ontologyTermName + '</a>');
		}
		
		function getOntologyTermSynonyms(ontologyTerm){
			var divContainerOTSynonym = $('<div>Synonym : </div>');
			if(ontologyTerm){
				var synonyms = [];
				$.each(ontologyTerm.ontologyTermSynonym, function(index, ontologyTermSynonym){
					synonyms.push(ontologyTermSynonym.ontologyTermSynonym);
				});
				if(synonyms.length == 1){
					divContainerOTSynonym.append(synonyms.join());		
				}else{
					divContainerOTSynonym.addClass('show-popover').append('<strong>' + synonyms.length + ' synonyms, see more details</strong>').popover({
						'content' : synonyms.join('<br><br>'),
						'placement' : 'auto',
						'trigger': 'hover',
						'html' : true
					});
				}
			}else{
				divContainerOTSynonym.append('N/A');
			}
			return divContainerOTSynonym;
		}
		
		function getMatchScore(ontologyTerm){
			return ontologyTerm ? $('<div />').append(ontologyTerm.Score.toFixed(2) + '%') : '';
		}
		
		function getMatchAdjustedScore(ontologyTerm){
			return ontologyTerm ? $('<div />').append(ontologyTerm.Combined_Score.toFixed(2) + '%') : '';
		}
	};
}($, window.top.molgenis = window.top.molgenis || {}));