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
	var self = molgenis.dataexplorer.filter = molgenis.dataexplorer.filter || {};
	
	self.openFilterModal = function(attribute, attributeFilter) {
		var modal = createFilterModal();
		
		var title = attribute.name;
		var description = attribute.description ? attribute.description : 'No description available';
		var controls = molgenis.dataexplorer.createFilterControls(attribute, attributeFilter);
		
		$('.filter-title', modal).html(title);
		$('.filter-description', modal).html(description);
		$('.filter-controls', modal).html(controls);
		
		modal.modal('show');
	};
	
	function createFilterModal() {		
		var modal = $('#filter-modal');
		if(!modal.length) {
			var items = [];
			items.push('<div class="modal hide" id="filter-modal" tabindex="-1">');
			items.push('<div class="modal-header">');
			items.push('<button type="button" class="close" data-dismiss="modal" aria-hidden="true">&times;</button>');
			items.push('<h3 class="filter-title"></h3>');
			items.push('</div>');
			items.push('<div class="modal-body">');
			items.push('<legend>Description</legend>');
			items.push('<p class="filter-description"></p>');
			items.push('<legend>Filter</legend>');
			items.push('<form class="form-horizontal filter-controls"></form>');
			items.push('</div>');
			items.push('<div class="modal-footer">');
			items.push('<a href="#" class="btn" data-dismiss="modal">Cancel</a>');
			items.push('<a href="#" class="btn btn-primary filter-apply-btn" data-dismiss="modal">Apply</a>');
			items.push('</div>');
			items.push('</div>');
			
			modal = $(items.join(''));
			
			// workaround for "Uncaught RangeError: Maximum call stack size exceeded"
			// http://stackoverflow.com/a/19190216
			var enforceModalFocusFn = $.fn.modal.Constructor.prototype.enforceFocus;
			$.fn.modal.Constructor.prototype.enforceFocus = function() {};
			modal.on('hidden', function() {
			    $.fn.modal.Constructor.prototype.enforceFocus = enforceModalFocusFn;
			});
			
			modal.modal({'show': false});
			
			createFilterModalControls(modal);
		}
		return modal;
	}
	
	function createFilterModalControls(modal) {
		$('.filter-apply-btn', modal).click(function() {
			var filters = molgenis.dataexplorer.createFilters($('form', modal));
			if (filters.length > 0) {
				$(document).trigger('updateAttributeFilters', {
					'filters' : filters
				});
			}
		});
		
		modal.on('shown', function () {
			$('form input:visible:first', modal).focus();
		});
		
		modal.keypress(function(e) {
		    if(e.which == 13) {
		    	e.preventDefault();
		    	$('.filter-apply-btn', modal).click();
		    }
		});
	}
}($, window.top.molgenis = window.top.molgenis || {}));