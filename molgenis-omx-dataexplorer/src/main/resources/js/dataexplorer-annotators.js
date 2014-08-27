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
	
	var self = molgenis.dataexplorer.annotators = molgenis.dataexplorer.annotators || {};

	// module api
	self.getAnnotatorSelectBoxes = getAnnotatorSelectBoxes;
	
	var restApi = new molgenis.RestClient();
	
	function getAnnotatorSelectBoxes() {
		// reset
		var entity = getEntity();
		restApi.getAsync(entity.href, null, function(dataset) {		
			$.ajax({
				type : 'POST',
				url : '/annotators/get-available-annotators',
				data : JSON.stringify(dataset.name),
				contentType : 'application/json',
				success : function(resultMap) {
					var enabledAnnotators = [];
					var disabledAnnotators = [];
					
					for(var key in resultMap){
						if(resultMap[key]['canAnnotate'] === true){
							enabledAnnotators.push('<label class="checkbox">\n');
							enabledAnnotators.push('<input type="checkbox" class="checkbox" name="annotatorNames" value="' + key + '">' + key+ ' <a id="disabled-tooltip" class="darktooltip" data-toggle="tooltip" title="Input:\t'+resultMap[key]["inputMetadata"].toString()+'\nOutput:\t'+resultMap[key]["outputMetadata"].toString()+'"><span class="glyphicon glyphicon-remove"></span></span></a>');
							enabledAnnotators.push('</label>');
							
						}else{
							disabledAnnotators.push('<label class="checkbox">\n');
							disabledAnnotators.push('<input type="checkbox" class="checkbox" name="annotatorNames" disabled value="' + key + '">' + key + ' <a id="disabled-tooltip" class="darktooltip" data-toggle="tooltip" title="Input:\t'+resultMap[key]["inputMetadata"].toString()+'\nOutput:\t'+resultMap[key]["outputMetadata"].toString()+'"><span class="glyphicon glyphicon-info-sign"></span></a>');
							disabledAnnotators.push('</label>');
						}
					}
					
					$('#annotator-checkboxes-enabled').html(enabledAnnotators.join(""));
					$('#annotator-checkboxes-disabled').html(disabledAnnotators.join(""));
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
                        molgenis.createAlert([{'message': 'Annotation completed. <a href=/menu/main/dataexplorer?dataset='+name+'>Show result</a>'}], 'success');
                    }
                });
            }
        }); 
	});
}($, window.top.molgenis = window.top.molgenis || {}));	