(function($, molgenis) {
	var restApi = new molgenis.RestClient();
	
	$(function() {	
		restApi.getAsync('/api/v1/attributeclass/meta', {'expand': ['attributes']}, function(entityMetaData) {
			selectedEntityMetaData = entityMetaData;
			$('#data-table-container').table({
				entityMetaData: entityMetaData,
				attributes: $.map(entityMetaData.attributes, function(attribute) {
					//Don't show compound and entityClass attributes
					return attribute.fieldType !== 'COMPOUND' ? (attribute.name != 'entityClass' ? attribute : null) : null;
				}),
				query: {q:[{field: 'entityClass', operator: 'EQUALS', value: entityClassIdentifier}]}
			});
		});
	});

}($, window.top.molgenis = window.top.molgenis || {}));
