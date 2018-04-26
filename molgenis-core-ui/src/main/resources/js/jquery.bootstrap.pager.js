(function($) {
	$.fn.pager = function(options) {
		// call pager method
		if (typeof options == 'string') {
			return this.data('pager')[options]();
		}

		// create pager
		var settings = $.extend({}, $.fn.pager.defaults, options);

		// pager html
//		this.addClass("pagination pagination-centered");
		updatePager(this, settings.page, settings, false);
		
		// pager events
		this.off('click', '**');
		this.on('click', 'li.page-prev', $.proxy(function(e){
			e.preventDefault();
			e.stopPropagation();
			updatePager(this, this.data('page') - 1, settings, true);
		}, this));
		this.on('click', 'li.page', $.proxy(function(e){
			e.preventDefault();
			e.stopPropagation();
			updatePager(this, parseInt($(e.target).html(), 10), settings, true);
		}, this));
		this.on('click', 'li.page-next', $.proxy(function(e){
			e.preventDefault();
			e.stopPropagation();
			updatePager(this, this.data('page') + 1, settings, true);
		}, this));
		
		// pager plugin methods
		this.data('pager', {
			getPage : $.proxy(function(){
				return this.data('page');
			}, this)
		});
		
		return this;
	};
	
	// default pager settings
	$.fn.pager.defaults = {
		'nrItems' : 0,
		'nrItemsPerPage' : 20,
		'page' : 1,
		'onPageChange' : null
	};
	
	var updatePager = function(pager, page, settings, firePageChangeEvent) {
		var nrPages = Math.ceil(settings.nrItems / settings.nrItemsPerPage);
		if(page > 0 && page <= nrPages) {
			pager.data('page', page);
			renderPager(pager, page, nrPages);
			if(firePageChangeEvent && settings.onPageChange && typeof settings.onPageChange === 'function') {
				settings.onPageChange({
					'page' : page,
					'start': (page - 1) * settings.nrItemsPerPage,
					'end'  : Math.min(page * settings.nrItemsPerPage, settings.nrItems)
				});
			}
		}
	};
	
	var renderPager = function(pager, page, nrPages) {
		pager.empty();
		var items = [];
		items.push('<div class="text-center">');
		items.push('<ul class="pagination">');
		
		// previous page
		if(page === 1) {
			items.push('<li class="disabled"><span>&laquo;</span></li>');
		}
		else {
			items.push('<li class="page-prev"><a href="#">&laquo;</a></li>');
		}
		
		// pages
		for ( var i = 1; i <= nrPages; ++i) {
			if (i === page) {
				items.push('<li class="active"><span>' + i + '</span></li>');
			}
			else if ((i === 1) || (i === nrPages) || ((i > page - 3) && (i < page + 3)) || ((i < 7) && (page < 5)) || ((i > nrPages - 6) && (page > nrPages - 4))) {
				items.push('<li class="page"><a href="#">' + i + '</a></li>');
			}
			else if ((i === 2) || (i === nrPages - 1)) {
				items.push('<li class="disabled"><span>...</span></li>');
			}
		}
		
		// next page
		if (page === nrPages)
			items.push('<li class="disabled"><span>&raquo;</span></li>');
		else
			items.push('<li class="page-next"><a href="#">&raquo;</a></li>');
		
		items.push('</ul>');
		items.push('</div>');
		pager.html(items.join(''));
	};
}($));