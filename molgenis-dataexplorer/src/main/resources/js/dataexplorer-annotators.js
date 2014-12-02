/**
 * Annotators module
 * 
 * Dependencies: dataexplorer.js
 *  
 * @param $
 * @param molgenis
 */
(function($, molgenis) {
	"use strict";
	
	molgenis.dataexplorer = molgenis.dataexplorer || {};
	
	var annotatorTemplate;
	var self = molgenis.dataexplorer.annotators = molgenis.dataexplorer.annotators || {};

	// module api
	self.getAnnotatorSelectBoxes = getAnnotatorSelectBoxes;
	
	var restApi = new molgenis.RestClient();
	
	function getAnnotatorSelectBoxes() {
		// reset
		var entity = getEntity();
		var enabledAnnotatorContainer = $('#enabled-annotator-selection-container');
		var disabledAnnotatorContainer = $('#disabled-annotator-selection-container');
		
		restApi.getAsync(entity.href, null, function(dataset) {		
			$.ajax({
				type : 'POST',
				url : '/annotators/get-available-annotators',
				data : JSON.stringify(dataset.name),
				contentType : 'application/json',
				success : function(resultMap) {
					for(var key in resultMap){
						var enabled = resultMap[key]['canAnnotate'].toString();
						var inputMetaData = resultMap[key]["inputMetadata"].toString();
						var outputMetaData = resultMap[key]["outputMetadata"].toString();
						
						if(enabled === 'true') {
							enabledAnnotatorContainer.append(annotatorTemplate({
								'enabled' : enabled,
								'annotatorName' : key, 
								'inputMetaData' : inputMetaData, 
								'outputMetaData' : outputMetaData
							}));
						} else {
							disabledAnnotatorContainer.append(annotatorTemplate({
								'enabled' : enabled,
								'annotatorName' : key, 
								'inputMetaData' : inputMetaData, 
								'outputMetaData' : outputMetaData
							}));
						}
					}
					
					$('#selected-dataset-name').html(dataset.name);
					$('#dataset-identifier').val(dataset.name);
                    $('.darktooltip').tooltip({placement: 'right'});
				}
			});
		});
	};
	
	/**
	 * Returns the selected attributes from the data explorer
	 * 
	 * @memberOf molgenis.dataexplorer.annotators
	 */
	function getAttributes() {
		var attributes = molgenis.dataexplorer.getSelectedAttributes();
		return molgenis.getAtomicAttributes(attributes, restApi);
	}
	
	/**
	 * Returns the selected entity from the data explorer
	 * 
	 * @memberOf molgenis.dataexplorer.annotators
	 */
	function getEntity() {
		return molgenis.dataexplorer.getSelectedEntityMeta();
	}
	
	/**
	 * Returns the selected entity query from the data explorer
	 * 
	 * @memberOf molgenis.dataexplorer.annotators
	 */
	function getEntityQuery() {
		return molgenis.dataexplorer.getEntityQuery().q;
	}
	
	// on document ready
	$(function() {
		$("#disabled-tooltip").tooltip();

        var submitBtn = $('#annotate-dataset-button');
        var form = $('#annotate-dataset-form');
        
        annotatorTemplate = Handlebars.compile($("#annotator-template").html());
        
        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });    
        
        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (form.valid()) {
                $.ajax({
                    type: 'POST',
                    url: '/annotators/annotate-data/',
                    data: form.serialize(),
                    contentType: 'application/x-www-form-urlencoded',
                    success: function (name) {
                        molgenis.createAlert([{'message': 'Annotation completed. <a href=/menu/main/dataexplorer?entity='+name+'>Show result</a>'}], 'success');
                    }
                });
            }
        });
	});
}($, window.top.molgenis = window.top.molgenis || {}));	