var selectedFeatureHref = null;

$(document).bind("mobileinit", function() {
	$(document).on('pageshow', '#feature-page', window.top.molgenis.onDataItemPageShow);
	$(document).on('pagehide', '#feature-page', window.top.molgenis.onDataItemPageHide);
});

(function($, w) {
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	
	ns.onDataItemPageShow = function() {
		if (selectedFeatureHref) {
			restApi.getAsync(selectedFeatureHref, null, null, function(feature) {
				$('.feature-name').html(feature.name);
				$('#feature-description').html(feature.description);
				$('#feature-datatype').html(feature.dataType);
			
				if (feature.dataType == 'categorical') {
				
					$('#categories').append('<ul data-inset="true" data-role="listview"></ul>');
					$("#categories").trigger("create");
				
					var items = [];
					items.push('<li>Categories</li>');
				
					var q = {q:[{field:'observablefeature_identifier',operator:'EQUALS',value:feature.identifier}]};
					restApi.getAsync('/api/v1/category', null, q, function(categories) {
						$.each(categories.items, function() {
							items.push('<li class="feature-detail-value">' + this.name + '</li>');
						});
						$('#categories ul').html(items.join('')).listview('refresh');
					});
				}
			});
		
		} else {
			window.location.href = '/mobile/catalogue';
		}
	}
	
	ns.onDataItemPageHide = function() {
		$('.feature-name').html('&nbsp;');
		$('#feature-description').html('');
		$('#feature-datatype').html('');
		$('#categories').html('');
	}
	
}($, window.top));
