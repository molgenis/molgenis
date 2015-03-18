$(function() {
	var expressionAndTagInfoContainer = $('#expression-tag-info-container');

	var expressionAndTagTemplate;
	expressionAndTagTemplate = Handlebars.compile($("#expression-and-tag-template").html());

	$('.tag-remove-btn').on('click', function() {
		$(this).remove();
	});
	
	$('.expression').on('click', function() {
		var expression = $(this).val();
		var btns = $(this).parent().next().children();
		var tags = []; 
		
		$.each(btns, function() {
			tags.push($(this).text());
		});

		expressionAndTagInfoContainer.empty();
		expressionAndTagInfoContainer.append(expressionAndTagTemplate({
			'expression' : expression,
			'taglist' : tags
		}));
	
		$('#tag-dropdown').select2();
	});
});