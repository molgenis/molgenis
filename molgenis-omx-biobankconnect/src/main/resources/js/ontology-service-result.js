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
			var termDiv = $('<div />').addClass('col-md-4').append(entity.term).appendTo(layoutDiv);
			var ontologyTermMatchDiv= $('<div />').addClass('col-md-8 div-expandable').appendTo(layoutDiv);
			
			//Collect all the scores from the candidate ontology term mappings
			var scoreGroup = [];
			$.each(entity.results.searchHits, function(index, hit){
				scoreGroup.push(parseFloat(hit.columnValueMap.combinedScore.toFixed(2)));
			});
			
			//Get the index at which two groups are separated statistically based on the scores
			var separateLineIndex = -1;
			if(scoreGroup.length > 2){
				var classifications = ss.jenks(scoreGroup, 2);	
				separateLineIndex = scoreGroup.indexOf(classifications[1]);
			}
			
			var parentChildMap = {};
			var parentChildData = {};
			//Re-arrange the most likely ontology term mappings based on the algorithm that considers the parent-child relations
			$.each(entity.results.searchHits, function(index, hit){
				if(separateLineIndex < index + 1) return false;
				parentChildData[hit.columnValueMap.nodePath] = [];
				parentChildMap[hit.columnValueMap.nodePath] = index;
			});
			
			$.each(entity.results.searchHits, function(index, hit){
				if((index + 1) > separateLineIndex) return false;
				var nodePath = hit.columnValueMap.nodePath;
				var parentNodePath = hit.columnValueMap.parentNodePath;
				if(parentChildData[parentNodePath]){
					parentChildData[parentNodePath].push(hit);
					delete parentChildData[nodePath]; 
					// remember the index of the candidate mappings
					if(parentChildMap[parentNodePath] > index) parentChildMap[parentNodePath] = index;
					delete parentChildMap[nodePath]; 
				}
			});
			//Create the html visualizations for the mappings
			$.each(entity.results.searchHits, function(index, hit){
				if(index >= 20) return false;
				var ontologyTermNameDiv = $('<div />').addClass('col-md-5 matchterm show-popover').css('margin-bottom', '-6px').append(hit.columnValueMap.ontologyTerm);
				var ontologyTermUrlDiv = $('<div />').addClass('col-md-5 termurl').css('margin-bottom', '-6px').append('<a href="' + hit.columnValueMap.ontologyTermIRI + '" target="_blank">' + hit.columnValueMap.ontologyTermIRI + '</a>');
				var matchScoreDiv = $('<div />').addClass('col-md-2').css('margin-bottom', '-6px').append('<center>' + hit.columnValueMap.combinedScore.toFixed(2) + '</center>');
				var newLineDiv = $('<div />').addClass('row').append(ontologyTermNameDiv).append(ontologyTermUrlDiv).append(matchScoreDiv);
				var isEqual = hit.columnValueMap.ontologyTermSynonym === hit.columnValueMap.ontologyTerm;
				var popoverOption = {
					'placement' : 'bottom',
					'trigger' : 'hover',
					'title' : 'Click to look up in ontology',
					'html' : true,
					'content' : 'Matched by <u>' + (isEqual ? 'lable' : 'synonym') + '</u> shown below : <br><br> <strong>' + hit.columnValueMap.ontologyTermSynonym + '</strong><br>'
				};
				
				var nodePath = hit.columnValueMap.nodePath;
				if(parentChildMap[nodePath] !== undefined && parentChildMap[nodePath] !== null && parentChildData[nodePath].length > 2){
					if(ontologyTermMatchDiv.children('div.row:eq(' + parentChildMap[nodePath] + ')').length > 0){
						ontologyTermMatchDiv.children('div.row:eq(' + parentChildMap[nodePath] + ')').before(newLineDiv);
					}else{
						ontologyTermMatchDiv.append(newLineDiv);
					}
					var newScore = 0;
					var childList = '';
					$.each(parentChildData[nodePath], function(index, childHit){
						childList += '<u>' + childHit.columnValueMap.ontologyTerm + '</u>, ';
						newScore = newScore + childHit.columnValueMap.combinedScore;
					});
					if((newScore / parentChildData[nodePath].length) > hit.columnValueMap.combinedScore){
						popoverOption.content = 'Order has been re-arranged for this ontology term because the subclasses, ';
						newLineDiv.find('div:last>center').html((newScore / parentChildData[nodePath].length).toFixed(2) + ' (' + hit.columnValueMap.combinedScore.toFixed(2) + ')');
						newLineDiv.find('div:first').css('color', '#82108C');
						popoverOption.content += childList + ' are also selected as candidates, therefore suggests the parent class is likely to be the match!';
						matchScoreDiv.addClass('show-popover').popover({
							'placement' : 'bottom',
							'trigger' : 'hover',
							'title' : 'Score explanation',
							'content' : 'Score is re-calculated as the average score of multiple matched child ontology terms.' 
						});
					}
				}else{					
					ontologyTermMatchDiv.append(newLineDiv);
				}
				
				ontologyTermNameDiv.popover(popoverOption).click(function(){
					ontologyTree.locateTerm(hit.columnValueMap);
				});
				
				if((index + 1) == separateLineIndex){
					var separatLine = $('<legend />').css({
						'padding-top':'15px',
						'padding-bottom':'5px',
						'border-bottom-color':'#CC0025'
					});
					$('<div />').addClass('row').append(separatLine).appendTo(ontologyTermMatchDiv);
				}
			});
			
			var divWithColor = $('<div />').addClass('col-md-12 well div-hover').append(layoutDiv);
			$('<div />').addClass('row').append(divWithColor).appendTo(container);
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
				var toggle = $('<i class="icon-plus"></icon>').css({
					'cursor':'pointer',
					'margin-right' : '-15px'
				});
				var buttonDiv = $('<div />').css('float','right').append(toggle);
				$(element).before(buttonDiv);
				toggle.click(function(){
					if($(this).hasClass('icon-plus')){
						$(element).children().show();
						$(this).removeClass('icon-plus').addClass('icon-minus');
					}else{
						$(element).children('div:gt(0)').hide();
						$(this).removeClass('icon-minus').addClass('icon-plus');
					}
				});
			}
		});
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));