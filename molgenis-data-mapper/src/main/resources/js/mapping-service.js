(function($, molgenis) {	
	"use strict";
	var restApi = new molgenis.RestClient();
	
	// One function for getting new attribute meta data because multiple elements in 
	// the user interface allow for the selection of different entities
	function getNewAttributeMetaData(newEntityName, callback) {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/get-new-attributes',
			data : JSON.stringify(newEntityName),
			contentType : 'application/json',
			success : function(attributeMetaDataIteratable) {
				if(callback !== null && typeof callback === 'function') callback(attributeMetaDataIteratable);
			}
		});
	}
	
	$(function() {
		$('#submit-new-mapping-project-btn').click(function() {
			$('#create-new-mapping-project-form .submit').click();
		});
		
		$('#target-entity-select').change(function() {
			var selectedTargetName = $('#target-entity').val();
			var attributeMetaDataIteratable = getNewAttributeMetaData(newSourceName, function(attributeMetaDataIteratable){
				// TODO Update the first colomn of the #target-mapping-table
			});
		});
		
		$('#submit-new-source-column-btn').click(function() {
			//var targetMappingTable = $('#target-mapping-table')
			var currentTarget = $('#target-entity').val();
			var newSourceName = $('#new-source-entity').val();
			$('#create-new-source-column-modal').modal('toggle');
			
			var data = ["target" : newSourceName, 'source' : ]
			
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/mappingattribute',
				data : JSON.stringify(currentTargetnewEntityName),
				contentType : 'application/json',
				success : function(attributeMetaDataIteratable) {
					if(callback !== null && typeof callback === 'function') callback(attributeMetaDataIteratable);
				}
			});
			
//			var attributeMetaDataIteratable = getNewAttributeMetaData(newSourceName, function(attributeMetaDataIteratable){
//				targetMappingTable.find('tr').each(function(){
//					var trow = $(this);
//					if(trow.index() === 0){
//						trow.append('<th>' + newSourceName + '</th>');
//		            }else{
//		            	trow.append('<td>' + attributeMetaDataIteratable[trow.index].name + '</td>');
//		             }
//		         });
//			});	
		});
	});
		
}($, window.top.molgenis = window.top.molgenis || {}));