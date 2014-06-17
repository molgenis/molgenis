(function($, molgenis) {
	"use strict";
	
	molgenis.OntologySerivce = function OntologySerivce(){};
	
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
		container.children('div:gt(0)').empty();
		$.each(entities, function(index, entity){
			var layoutDiv = $('<div />').addClass('row-fluid');
			var termDiv = $('<div />').addClass('span4').append(entity.term).appendTo(layoutDiv);
			var ontologyTermMatchDiv= $('<div />').addClass('span7 div-expandable').appendTo(layoutDiv);
			var scoreGroup = [];
			$.each(entity.results.searchHits, function(index, hit){
				if(index >= 20) return false;
				var ontologyTermName = $('<div />').addClass('span5').css('margin-bottom', '-6px').append(hit.columnValueMap.ontologyTerm);
				var ontologyTermUrl = $('<div />').addClass('span5').css('margin-bottom', '-6px').append('<a href="' + hit.columnValueMap.ontologyTermIRI + '" target="_blank">' + hit.columnValueMap.ontologyTermIRI + '</a>');
				var matchScore = $('<div />').addClass('span2').css('margin-bottom', '-6px').append('<center>' + hit.columnValueMap.combinedScore.toFixed(2) + '</center>');
				$('<div />').addClass('row-fluid').append(ontologyTermName).append(ontologyTermUrl).append(matchScore).appendTo(ontologyTermMatchDiv);
				scoreGroup.push(parseFloat(hit.columnValueMap.combinedScore.toFixed(2)));
			});
			if(scoreGroup.length > 2){
				var classifications = ss.jenks(scoreGroup, 2);	
				var index = scoreGroup.indexOf(classifications[1]);
				if(index !== scoreGroup.length - 1){
					var separatLine = $('<legend />').css({
						'padding-top':'15px',
						'padding-bottom':'5px',
						'border-bottom-color':'#CC0025'
					});
					ontologyTermMatchDiv.children('div.row-fluid:eq(' + (index - 1) + ')').append(separatLine);
				}
			}
			var divWithColor = $('<div />').addClass('span12 well').append(layoutDiv);
			$('<div />').addClass('row-fluid').append(divWithColor).appendTo(container);
		});
		initToggle();
	}
	
	function initToggle(){
		$('div.div-expandable').each(function(index, element){
			if($(element).children().length > 1){
				$(element).children('div:gt(0)').hide();
				var toggle = $('<i class="icon-plus"></icon>').css('cursor','pointer');
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