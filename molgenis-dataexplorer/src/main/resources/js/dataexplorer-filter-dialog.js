/**
 * Attribute filter modal
 * 
 * Dependencies: dataexplorer.js
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.dataexplorer = molgenis.dataexplorer || {};
	molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};
	var self = molgenis.dataexplorer.filter.dialog = molgenis.dataexplorer.filter.dialog || {};
	
	var filter;
	
	self.openFilterModal = function(attribute, query) {
		var modal = createFilterModal();
		var title = attribute.label;
		var description = attribute.description ? attribute.description : 'No description available';
		
		$('.filter-title', modal).html(title);
		$('.filter-description', modal).html(description);

		molgenis.filters.create(attribute, { // FIXME replace with react call
			query: query, onQueryChange: function(event) {
				filter = event;
			}
		}, $('.form-horizontal', modal));
		modal.modal('show');
	};
	
	function createFilterModal() {
		var modal = $('#filter-modal');
        var filterTemplate = Handlebars.compile($("#filter-modal-template").html());
        modal = $(filterTemplate({}));
		createFilterModalControls(modal);
		return modal;
	}
	
	function createFilterModalControls(modal) {
		$('.filter-apply-btn', modal).unbind('click');
		$('.filter-apply-btn', modal).click(function() {
			$(document).trigger('updateAttributeFilters', {filters: [{attr: filter.attr, query: filter.query}]}); // FIXME check if changed
		});
		
		$(modal).unbind('shown.bs.modal');
		modal.on('shown.bs.modal', function () {
			$('form input:visible:first', modal).focus();
		});
		
		$(modal).unbind('keypress');
		modal.keypress(function(e) {
		    if(e.which == 13) {
		    	e.preventDefault();
		    	$('.filter-apply-btn', modal).click();
		    }
		});
	}
}($, window.top.molgenis = window.top.molgenis || {}));