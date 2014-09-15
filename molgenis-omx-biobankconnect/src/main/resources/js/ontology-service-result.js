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
			var layoutDiv = $('<div />').addClass('row');
			var termDiv = $('<div />').addClass('col-md-3').append(entity.term).appendTo(layoutDiv);
			var ontologyTermMatchDiv= $('<div />').addClass('col-md-9 div-expandable').appendTo(layoutDiv);
			
			//Collect all the scores from the candidate ontology term mappings
			var scoreGroup = [];
			$.each(entity.results.searchHits, function(index, hit){
				var eachScore = hit.columnValueMap.combinedScore ? hit.columnValueMap.combinedScore : hit.columnValueMap.score;
				scoreGroup.push(parseFloat(eachScore.toFixed(2)));
			});
			
			//Create the html visualizations for the mappings
			$.each(entity.results.searchHits, function(index, hit){
				if(index >= 20) return false;
				var ontologyTermNameDiv = $('<div />').addClass('col-md-4 matchterm show-popover').css('margin-bottom', '-6px').append(hit.columnValueMap.ontologyTerm);
				var ontologyTermUrlDiv = $('<div />').addClass('col-md-6 termurl').css('margin-bottom', '-6px').append('<a href="' + hit.columnValueMap.ontologyTermIRI + '" target="_blank">' + hit.columnValueMap.ontologyTermIRI + '</a>');
				var matchScoreDiv = $('<div />').addClass('col-md-2').css('margin-bottom', '-6px').append('<center>' + (hit.columnValueMap.combinedScore ? hit.columnValueMap.combinedScore.toFixed(2) :  hit.columnValueMap.score.toFixed(2)) + '%</center>');
				var newLineDiv = $('<div />').addClass('row').css({
					'padding-top':'3px',
					'padding-bottom':'3px'
				}).append(ontologyTermNameDiv).append(ontologyTermUrlDiv).append(matchScoreDiv);
				var isEqual = hit.columnValueMap.ontologyTermSynonym === hit.columnValueMap.ontologyTerm;
				var popoverOption = {
					'placement' : 'bottom',
					'trigger' : 'hover',
					'title' : 'Click to look up in ontology',
					'html' : true,
					'content' : 'Matched by <u>' + (isEqual ? 'lable' : 'synonym') + '</u> shown below : <br><br> <strong>' + hit.columnValueMap.ontologyTermSynonym + '</strong><br>'
				};
				ontologyTermMatchDiv.append(newLineDiv);
				ontologyTermNameDiv.popover(popoverOption).click(function(){
					ontologyTree.locateTerm(hit.columnValueMap);
				});
				
				if(scoreGroup.length > 3){
					var classifications = ss.jenks(scoreGroup, 3);
					$.each(classifications, function(i, score){
						if(i != 0 && i != classifications.length - 1 && index == scoreGroup.lastIndexOf(score)){
							var separatLine = $('<legend />').css({
								'padding-top':'20px',
								'border-bottom-color':'#CC0025'
							});
							$('<div />').addClass('row').append(separatLine).appendTo(ontologyTermMatchDiv);
						}
					});
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
				var toggle = $('<span class="glyphicon glyphicon-plus"></span>').css({
					'cursor':'pointer',
					'float' :'right',
					'margin-right' : '-15px'
				});
//				var buttonDiv = $('<div />').css('float','right').append(toggle);
				$(element).before(toggle);
				toggle.click(function(){
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