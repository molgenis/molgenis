(function($, molgenis) {
	"use strict";
	
//	var ontologyTree = null;
	var ontologyServiceRequest = null;
	var result_container = null;
	var reserved_field = 'Identifier';
	
//	molgenis.OntologySerivce = function OntologySerivce(ontologyTreeObject){
//		ontologyTree = ontologyTreeObject;
//	};
	
	molgenis.OntologySerivce = function OntologySerivce(container, request){
		result_container = container;
		ontologyServiceRequest = request;
	};
	
	molgenis.OntologySerivce.prototype.updatePageFunction = function(page){
		ontologyServiceRequest['entityPager'] = {
			'start' : page.start,
			'num' : page.end - page.start
		};
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/match/retrieve',
			async : false,
			data : JSON.stringify(ontologyServiceRequest),
			contentType : 'application/json',
			success : function(data, textStatus, request) {
				result_container.empty();
				$.each(data.items, function(index, entity){
					var inputData = entity.inputTerm ? entity.inputTerm : {};
					var layoutDiv = $('<div />').addClass('row');
					var termDiv = $('<div />').addClass('col-md-4').appendTo(layoutDiv);
					$.map(inputData, function(val, key){
						if(key !== reserved_field) termDiv.append('<div>' + key + ' : ' + val + '</div>');
					});
					layoutDiv.append(createInfoForMatchedTerm([entity]));

					$('<div />').addClass('col-md-12 well div-hover').append(layoutDiv).appendTo(result_container);
				});
				initToggle();
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	};
	
	function createInfoForMatchedTerm(entities){
		var ontologyTermMatchDiv= $('<div />').addClass('col-md-8 div-expandable');
		//Create the html visualizations for the mappings
		$.each(entities, function(index, entity){
			if(index >= 20) return false;
			var hit = entity.ontologyTerm;
			var matchedTerm = entity.matchedTerm;
			var ontologyTermPopover = $('<div>' + hit.ontologyTerm + '</div>').addClass('show-popover').css('margin-bottom', '1px');
			var ontologyTermNameDiv = $('<div />').addClass('col-md-8 matchterm').css('margin-bottom','8px').append(ontologyTermPopover)
				.append('<a href="' + hit.ontologyTermIRI + '" target="_blank">' + hit.ontologyTermIRI + '</a>');
			var matchScoreDiv = $('<div />').addClass('col-md-3').css('margin-bottom', '-6px').append('<center>' + matchedTerm.Score.toFixed(2) + '%</center>');
			var newLineDiv = $('<div />').addClass('row').css({
				'padding-top':'3px',
				'padding-bottom':'3px'
			}).append(ontologyTermNameDiv).append(matchScoreDiv);
			var isEqual = hit.ontologyTermSynonym === hit.ontologyTerm;
			var popoverOption = {
				'placement' : 'bottom',
				'trigger' : 'hover',
				'title' : 'Click to look up in ontology',
				'html' : true, 
				'content' : (hit.maxScoreField ? 'Matched based on the input field : <strong>' + hit.maxScoreField + '</strong><br><br>' : '') +
					((hit.maxScoreField && hit[hit.maxScoreField]) ? 'OntologyTerm ' + hit.maxScoreField + ' is <strong>' + hit[hit.maxScoreField] + '</strong><br><br>' : '')+ 
					(hit.ontologyTermSynonym !== hit.ontologyTerm ? 'OntologyTerm synonym is <strong>' + hit.ontologyTermSynonym + '</strong>' : '') 
			};
			ontologyTermMatchDiv.append(newLineDiv);
			
//			if(scoreGroup.length > 3){
//				var classifications = ss.jenks(scoreGroup, 3);
//				$.each(classifications, function(i, score){
//					if(i != 0 && i != classifications.length - 1 && index == scoreGroup.lastIndexOf(score)){
//						var separatLine = $('<legend />').css('border-bottom-color' , '#CC0025');
//						$('<div />').addClass('row').append(separatLine).appendTo(ontologyTermMatchDiv);
//					}
//				});
//			}
			
			//Only add plus icon to the first matched candidate
			if(index === 0){
				$('<div />').addClass('col-md-1').append('<span class="glyphicon glyphicon-plus"></span>').appendTo(newLineDiv);
			}
		});
		return ontologyTermMatchDiv;
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