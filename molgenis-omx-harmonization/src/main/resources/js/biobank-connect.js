(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var CONTEXT_URL = null;
	var catalogueChooser = new ns.CatalogueChooser();
	var ontologyAnnotator = new ns.OntologyAnnotator();
	
	ns.setContextURL = function(CONTEXT_URL){
		this.CONTEXT_URL = CONTEXT_URL;
	};
	
	ns.getContextURL = function() {
		return this.CONTEXT_URL;
	};
	
	ns.hrefToId = function(href){
		return href.substring(href.lastIndexOf('/') + 1); 
	};
	
	ns.getCatalogueChooser = function() {
		return catalogueChooser;
	};
	
	ns.getOntologyAnnotator = function() {
		return ontologyAnnotator;
	};
	
	ns.checkMatchingStatus = function(prefix, progressBarElement) {
		$.ajax({
			type : 'GET',
			url : prefix + '/match/status',
			contentType : 'application/json',
			success : function(response) {
				console.log(response);
				$('#control-container ul.pager li').addClass('disabled');
				if(response.isRunning){
					if(response.matchePercentage === 0){
						var width = $(progressBarElement).width();
						var parentWidth = $(progressBarElement).offsetParent().width();
						var percent = 100 * width / parentWidth + 5;
						$(progressBarElement).width(percent + '%');
					}else if(response.matchePercentage === 100){
						$(progressBarElement).width(response.matchePercentage + '%');
						setTimeout(function(){
							$(progressBarElement).empty().append('<p style="font-size:14px;padding-top:4px;">Storing result...</p>');
						}, 2000);
					}else{
						var width = $(progressBarElement).width();
						var parentWidth = $(progressBarElement).offsetParent().width();
						var percent = 100 * width / parentWidth + 5;
						if(percent < response.matchePercentage) percent = response.matchePercentage;
						$(progressBarElement).width(percent + '%').empty();
					}
					setTimeout(function(){
						ns.checkMatchingStatus(prefix, progressBarElement)
					}, 3000);
				}else {
					$(progressBarElement).empty().width('100%').parents('div:eq(0)').removeClass('active');
					$(progressBarElement).append('<p style="font-size:14px;padding-top:4px;">Finished!</p>');
					$('#control-container ul.pager li').removeClass('disabled');
				}
			},
			error : function(request, textStatus, error){
				console.log(error);
			} 
		});
	};
	
	$(document).ready(function(){
		$('#reset-button').click(function(){
			if(!$(this).hasClass('disabled')){
				$('form').attr({
					'action' : ns.getContextURL() + '/reset',
					'method' : 'GET'
				}).submit();
			}
			return false;
		});
		$('#next-button').click(function(){
			if(!$(this).hasClass('disabled')){
				$('form').attr({
					'action' : ns.getContextURL() + '/next',
					'method' : 'GET',
				}).submit();
			}
			return false;
		});
		$('#prev-button').click(function(){
			if(!$(this).hasClass('disabled')){
				$('form').attr({
					'action' : ns.getContextURL() + '/prev',
					'method' : 'GET'
				}).submit();
			}
			return false;
		});
	});
}($, window.top));