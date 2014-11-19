(function($, molgenis) {
	"use strict";
	
	var ontologyServiceRequest = null;
	var result_container = null;
	var reserved_field = 'Identifier';
	
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
				if(data.items.length > 0){
					var slimDiv = $('<div style="width:96%;margin-left:2%;"></div>').appendTo(result_container);
					slimDiv.append('<p style="font-size:20px;margin-top:-20px;"><strong>' + (ontologyServiceRequest.matched ? 'Matched result' : 'Unmatched result') + '</strong></p>');
					var table = $('<table align="center"></table>').addClass('table').appendTo(slimDiv);
					$('<tr />').append('<th style="width:40%;">Input term</th><th style="width:40%;">Matched term</th><th style="width:10%;">Score</th><th>Validate</th>').appendTo(table);
					$.each(data.items, function(index, entity){
						table.append(createRowForMatchedTerm(entity, ontologyServiceRequest.matched));
					});
				}else{
					result_container.append('<center>There are not results!</center>');
				}
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	};
	
	function createRowForMatchedTerm(entity, validated){
		var row = $('<tr />');
		var inputTermTd = $('<td />').appendTo(row);
		$.map(entity.inputTerm ? entity.inputTerm : {}, function(val, key){
			if(key !== reserved_field) inputTermTd.append('<div>' + key + ' : ' + val + '</div>');
		});
		
		$('<td />').append('<div>' + entity.ontologyTerm.ontologyTerm + '</div><div><a href="' + entity.ontologyTerm.ontologyTermIRI + '" target="_blank">' + entity.ontologyTerm.ontologyTermIRI + '</a></div>').appendTo(row);
		$('<td />').append(entity.matchedTerm.Score.toFixed(2) + '%').appendTo(row);
		if(validated){
			$('<td />').append('<span class="glyphicon glyphicon-ok"></span>').appendTo(row);
		}else{
			var button = $('<button class="btn btn-default" type="button">Validate</button>');
			$('<td />').append(button).appendTo(row);
			button.click(function(){
				console.log("Validate button is clicked!");
			});
		}
		
		
//		var isEqual = hit.ontologyTermSynonym === hit.ontologyTerm;
//		var popoverOption = {
//			'placement' : 'bottom',
//			'trigger' : 'hover',
//			'title' : 'Click to look up in ontology',
//			'html' : true, 
//			'content' : (hit.maxScoreField ? 'Matched based on the input field : <strong>' + hit.maxScoreField + '</strong><br><br>' : '') +
//				((hit.maxScoreField && hit[hit.maxScoreField]) ? 'OntologyTerm ' + hit.maxScoreField + ' is <strong>' + hit[hit.maxScoreField] + '</strong><br><br>' : '')+ 
//				(hit.ontologyTermSynonym !== hit.ontologyTerm ? 'OntologyTerm synonym is <strong>' + hit.ontologyTermSynonym + '</strong>' : '') 
//		};
		
//			if(scoreGroup.length > 3){
//				var classifications = ss.jenks(scoreGroup, 3);
//				$.each(classifications, function(i, score){
//					if(i != 0 && i != classifications.length - 1 && index == scoreGroup.lastIndexOf(score)){
//						var separatLine = $('<legend />').css('border-bottom-color' , '#CC0025');
//						$('<div />').addClass('row').append(separatLine).appendTo(ontologyTermMatchDiv);
//					}
//				});
//			}
			
		return row;
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