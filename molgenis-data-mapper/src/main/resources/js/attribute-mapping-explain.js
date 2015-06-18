(function($, molgenis) {
	
	function generateExplanation(explainObjects){
		var explanation = '';
		$.each(explainObjects, function(index, explainObject){
			explanation += explainObject.matchedTerm + '-->' + explainObject.queryValue + ' with ' + explainObject.score + '<br />';
		});
		return explanation;
	}
	
	function explainTargetAttribute(requestBody){
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/attributeMapping/explain',
			data : JSON.stringify(requestBody),
			contentType : 'application/json',
			success : function(data) {
				var resultDiv = $('#' + requestBody.targetAttribute + '-explained-result');
				$(resultDiv).find('select').remove();
				var select = $('<select />').appendTo(resultDiv);
				$.map(data, function(val, key){
					select.append('<option value="' + key + '">' + key + '</option>');
				});
				select.change(function(){
					$(resultDiv).find('table').remove();
					var candidateAttributes = data[$(this).val()];
					if(candidateAttributes.length > 0){
						var table = $('<table />').addClass('table').css({'margin-top':'15px'});
						table.append('<tr><th>Name</th><th>Label</th><th>Tags</th><th>Explanation</th></tr>');
						$.each(candidateAttributes, function(index, attribute){
							table.append('<tr><td>' + attribute.attributeMetaData.name + '</td><td>' + attribute.attributeMetaData.label + '</td><th></th><th>' + generateExplanation(attribute.explainedQueryStrings) + '</th></tr>');
						});
						$(resultDiv).append(table);	
					}
				}).trigger('change');
			}
		});
	}
	
	$(document).ready(function() {
		console.log( "ready!" );
		$.each($('button.explain-button'), function(index, element){
			$(element).click(function(){
				var requestBody = {'mappingProjectId': $('#mappingProjectId').val(), 'target' : $('#target').val(), 'targetAttribute' : $(this).val()};
				explainTargetAttribute(requestBody);
			});
		});
	});
}($, window.top.molgenis = window.top.molgenis || {}));