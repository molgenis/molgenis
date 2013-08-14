$(document).bind("mobileinit", function() {
	$(document).on('pagebeforeshow', '#catalogue-page', window.top.molgenis.onCataloguePageBeforeShow);
	$(document).on('pagebeforehide', '#catalogue-page', window.top.molgenis.onCataloguePageBeforeHide);
});

(function($, w) {
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var features = null;
	var loading = false;
	
	ns.onCataloguePageBeforeShow = function() {
		$(document).on('scroll', function() {
			if (!loading && (features != null) && features.nextHref && ns.elementInView('#features li:last')) {
				ns.showFeatures(features.nextHref);
			}
		});
		
		ns.showFeatures('/api/v1/observablefeature?num=' + MolgenisMobileConfig.featureCount);
	}
	
	ns.onCataloguePageBeforeHide = function() {
		features = null;
		$(document).unbind('scroll');
		$('#features').html('').listview('refresh');
	}
	
	ns.showFeatures = function(href) {
		loading = true;
		$.mobile.showPageLoadingMsg();
		
		//Sort on name
		var sortQueryRule = 'q[0].operator=SORTASC&q[0].value=name';
		features = restApi.get(href + '&' + sortQueryRule);
		
		var items = [];
		if ((features != null) && features.num > 0) {
			$.each(features.items, function() {
				items.push('<li><a href="#" data-href="' + this.href + '" >' + this.name + '</a></li>');
			});
		}
		
		$('#feature-count').html(features.total);
		$('#features').append($(items.join(''))).listview('refresh');
		$.mobile.hidePageLoadingMsg(); 
		
		loading = false;
	}
	
	ns.elementInView = function(elem) {
	    var docViewTop = $(window).scrollTop();
	    var docViewBottom = docViewTop + $(window).height();
	    var elemTop = $(elem).offset().top;
	 
	   
	    return ((elemTop <= docViewBottom) && (elemTop >= docViewTop));
	}
}($, window.top));