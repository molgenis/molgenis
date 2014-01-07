$(document).bind("mobileinit", function() {
	$(document).on('pageshow', '#catalogue-page', window.top.molgenis.onCataloguePageShow);
});

$(function() {
	$("#search-features").on('change', window.top.molgenis.onSearchFieldChange);
});

(function($, molgenis) {
	"use strict";
	
	var ns = molgenis;
	var restApi = new ns.RestClient();
	var loading = false;
	var nextHref = null;
	var prevHref = null;
	
	ns.onSearchFieldChange = function(event) {
		var searchText = $(this).val();
		
		$('#search-features').blur();//Hide keyboard
		var q = 'q[1].operator=LIKE&q[1].field=name&q[1].value=' + encodeURIComponent(searchText);
		ns.showFeatures('/api/v1/observablefeature?num=' + MolgenisMobileConfig.featureCount + '&' + q);
	}
	
	ns.onCataloguePageShow = function(event, ui) {
		ns.isUserAuthenticated({
			authenticated: function() {
				if (!ui.prevPage || ui.prevPage.attr('id') != 'feature-page') {
					ns.clearSearch();
				}
				
				if ($("#features").children().length == 0) {
					ns.showFeatures('/api/v1/observablefeature?num=' + MolgenisMobileConfig.featureCount);
				}
			},
			unAuthenticated: function() {
				window.location.href = '/mobile/catalogue';
			}
		});
	}
	
	ns.showFeatures = function(href) {
		loading = true;
		
		//Sort on name
		var sortQueryRule = 'q[0].operator=SORTASC&q[0].value=name';
		restApi.getAsync(href + '&' + sortQueryRule, null, null, function(features) {
			var items = [];
		
			nextHref = features.nextHref;
			prevHref = features.prevHref;
		
			if ((features != null) && features.num > 0) {
				if (prevHref) {
					items.push('<li data-theme="b"><a href="#" id="prev">Previous items...</a></li>');
				}
			
				$.each(features.items, function() {
					items.push('<li><a href="#feature-page" data-href="' + this.href + '" class="feature">' + this.name + '</a></li>');
				});
			
				if (nextHref) {
					items.push('<li data-theme="b"><a href="#" id="next" >Next items...</a></li>');
				}
			}
		
			$('#feature-count').html(features.total);
			$('#features').html(items.join(''));
		
			$('#prev').on('click', function() {
				ns.showFeatures(prevHref);
			});
		
			$('a.feature').on('click',  function() { 
				selectedFeatureHref = $(this).attr('data-href');
				$.mobile.changePage('#feature-page', {transition: 'slide'});
			});
		
			$('#next').on('click', function() {
				ns.showFeatures(nextHref);
			});
		
			$('#features').listview('refresh');
		
			loading = false;
		});
	}
	
	ns.clearSearch = function() {
		$("#search-features").val('');
		$('#features').empty();
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));