(function($, molgenis) {
	"use strict";
	
	var ontologyTree = null;
	
	molgenis.OntologySerivce = function OntologySerivce(ontologyTreeObject){
		ontologyTree = ontologyTreeObject;
	};
	
	molgenis.OntologySerivce.prototype.updatePageFunction = function(page){
		var request = {
			'start' : page.start,
			'num' : page.end - page.start
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/match/retrieve',
			async : false,
			data : JSON.stringify(request),
			contentType : 'application/json',
			success : function(data, textStatus, request) {
				createResults(data.items)
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	};
	
	function createResults(entities){
		var container = $('#match-result');
		var isHidden = container.find('.termurl:eq(0)').is(':hidden');
		container.empty();
		$.each(entities, function(rowIndex, entity){
			var inputData = entity.results.inputData ? entity.results.inputData : {};
			var layoutDiv = $('<div />').addClass('row');
			var termDiv = $('<div />').addClass('col-md-4').appendTo(layoutDiv);
			if(Object.keys(inputData).length  === 0){
				termDiv.append(entity.term);
			}else{
				$.map(inputData, function(val, key){
					termDiv.append('<div>' + key + ' : ' + val + '</div>');
				});
			}
			var ontologyTermMatchDiv= $('<div />').addClass('col-md-8 div-expandable').appendTo(layoutDiv);
			//Collect all the scores from the candidate ontology term mappings
			var scoreGroup = [];
			$.each(entity.results.searchHits, function(index, hit){
				var eachScore = hit.columnValueMap.combinedScore ? hit.columnValueMap.combinedScore : hit.columnValueMap.score;
				scoreGroup.push(parseFloat(eachScore.toFixed(2)));
			});
			
			//Create the html visualizations for the mappings
			$.each(entity.results.searchHits, function(index, hit){
				if(index >= 20) return false;
				var ontologyTermPopover = $('<div>' + hit.columnValueMap.ontologyTerm + '</div>').addClass('show-popover').css('margin-bottom', '1px');
				var ontologyTermNameDiv = $('<div />').addClass('col-md-8 matchterm').css('margin-bottom','8px').append(ontologyTermPopover)
					.append('<a href="' + hit.columnValueMap.ontologyTermIRI + '" target="_blank">' + hit.columnValueMap.ontologyTermIRI + '</a>');
				var matchScoreDiv = $('<div />').addClass('col-md-3').css('margin-bottom', '-6px').append('<center>' + (hit.columnValueMap.combinedScore ? hit.columnValueMap.combinedScore.toFixed(2) :  hit.columnValueMap.score.toFixed(2)) + '%</center>');
				var newLineDiv = $('<div />').addClass('row').css({
					'padding-top':'3px',
					'padding-bottom':'3px'
				}).append(ontologyTermNameDiv).append(matchScoreDiv);
				var isEqual = hit.columnValueMap.ontologyTermSynonym === hit.columnValueMap.ontologyTerm;
				var popoverOption = {
					'placement' : 'bottom',
					'trigger' : 'hover',
					'title' : 'Click to look up in ontology',
					'html' : true, 
					'content' : (hit.columnValueMap.maxScoreField ? 'Matched based on the input field : <strong>' + hit.columnValueMap.maxScoreField + '</strong><br><br>' : '') + 
						(hit.columnValueMap.ontologyTermSynonym !== hit.columnValueMap.ontologyTerm ? 'OntologyTerm synonym is <strong>' + hit.columnValueMap.ontologyTermSynonym + '</strong>' : '') 
				};
				ontologyTermMatchDiv.append(newLineDiv);
				ontologyTermPopover.popover(popoverOption).click(function(){
					ontologyTree.locateTerm(hit.columnValueMap);
				});
				
				if(scoreGroup.length > 3){
					var classifications = ss.jenks(scoreGroup, 3);
					$.each(classifications, function(i, score){
						if(i != 0 && i != classifications.length - 1 && index == scoreGroup.lastIndexOf(score)){
							var separatLine = $('<legend />').css('border-bottom-color' , '#CC0025');
							$('<div />').addClass('row').append(separatLine).appendTo(ontologyTermMatchDiv);
						}
					});
				}
				
				//Only add plus icon to the first matched candidate
				if(index === 0){
					$('<div />').addClass('col-md-1').append('<span class="glyphicon glyphicon-plus"></span>').appendTo(newLineDiv);
				}
			});
			$('<div />').addClass('col-md-12 well div-hover').append(layoutDiv).appendTo(container);
		});
		if(isHidden){
			container.find('.termurl').hide();
			$('.matchterm').removeClass('col-md-5').addClass('col-md-8');
		}
		initToggle();
	}
	
	function initToggle(){
		$('div.div-expandable').each(function(index, element){
			if($(element).children().length > 1){
				$(element).children('div:gt(0)').hide();
				$(element).find('.glyphicon-plus:eq(0)').click(function(){
					if($(this).hasClass('glyphicon-plus')){
						$(element).children().show();
						$(this).removeClass('glyphicon-plus').addClass('glyphicon-minus');
					}else{
						$(element).children('div:gt(0)').hide();
						$(this).removeClass('glyphicon-minus').addClass('glyphicon-plus');
					}
				});
			}
		});
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));