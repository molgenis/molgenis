$(function() {
	$('input[type=checkbox]').on('change', function(){
		$('#model-search-form').submit();
	});
})