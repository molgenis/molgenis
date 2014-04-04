$(function() {
	$('input[type=checkbox]').on('change', function(){
		$('#model-search-form').submit();
	});
	
	$('#clear-button').on('click', function(){
		$('#search-input').val('');
		$('#model-search-form').submit();
	});
	
	if (nrItems > nrItemsPerPage) {
		$('#pager').pager({
			nrItems: nrItems,
			nrItemsPerPage: nrItemsPerPage,
			page: parseInt($('input[name=page]').val()),
			onPageChange: pageChange
		});
	}
	
	function pageChange(page){
		$('input[name=page]').val(page.page);
		$('#model-search-form').submit();
	}
})