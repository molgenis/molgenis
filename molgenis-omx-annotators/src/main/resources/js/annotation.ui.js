(function($, molgenis) {
	"use strict";
	
	var selectedDataSet = null;
	
	var restApi = new molgenis.RestClient();
	
	molgenis.onDataSetSelectionChange = function(dataSetUri) {
		// reset
		restApi.getAsync(dataSetUri, null, function(dataSet) {
			selectedDataSet = dataSet;
			
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/change-selected-dataset',
				data : JSON.stringify(selectedDataSet.Identifier),
				contentType : 'application/json',
				success : function(resultMap) {
					var enabledAnnotators = [];
					var disabledAnnotators = [];
					
					for(var key in resultMap){
						if(resultMap[key]['canAnnotate'] === true){
							enabledAnnotators.push('<label class="checkbox">\n');
							enabledAnnotators.push('<input type="checkbox" class="checkbox" name="annotatorNames" value="' + key + '">' + key+ ' <a id="disabled-tooltip" class="darktooltip" data-toggle="tooltip" title="Input:\t'+resultMap[key]["inputMetadata"].toString()+'\nOutput:\t'+resultMap[key]["outputMetadata"].toString()+'"><span class="icon icon-info-sign"></span></a>');
							enabledAnnotators.push('</label>');
							
						}else{
							disabledAnnotators.push('<label class="checkbox">\n');
							disabledAnnotators.push('<input type="checkbox" class="checkbox" name="annotatorNames" disabled value="' + key + '">' + key + ' <a id="disabled-tooltip" class="darktooltip" data-toggle="tooltip" title="Input:\t'+resultMap[key]["inputMetadata"].toString()+'\nOutput:\t'+resultMap[key]["outputMetadata"].toString()+'"><span class="icon icon-info-sign"></span></a>');
							disabledAnnotators.push('</label>');
						}
					}
					
					$('#annotator-checkboxes-enabled').html(enabledAnnotators.join(""));
					$('#annotator-checkboxes-disabled').html(disabledAnnotators.join(""));
					$('#selected-dataset-name').html(selectedDataSet.Name);
					$('#dataset-identifier').val(selectedDataSet.Identifier);
                    $('.darktooltip').tooltip({placement: 'right'});
				}
			});
		});
	};

	// on document ready
	$(function() {
		$('#dataset-select').chosen();
		$('#dataset-select').change(function() {
			if($("#dataset-select").val() != null){
				
				restApi.getAsync($('#dataset-select').val(), null, function(dataSet) {
					selectedDataSet = dataSet;
				});
			
				molgenis.onDataSetSelectionChange($(this).val());
			}	
		});
		
		// fire event handler
		$('#dataset-select').change();
			
		$("#disabled-tooltip").tooltip();
		$("#remove-button-selected-phenotype").tooltip();
		
		$("#rootwizard").bootstrapWizard({'tabClass': 'nav nav-tabs'});
		$("#phenotypeSelect").chosen();	
		
		// disable the filtering tabs
		$("#rootwizard").bootstrapWizard('disable', 1);
		$(".tab2").click(function(){return false;});
		
		$("#rootwizard").bootstrapWizard('disable', 2);
		$(".tab3").click(function(){return false;});

         var submitBtn = $('#execute-button');

        var form = $('#execute-annotation-app');

        form.submit(function (e) {
            e.preventDefault();
            e.stopPropagation();
            if (form.valid()) {
                $.ajax({
                    type: 'POST',
                    url: molgenis.getContextUrl() + '/execute-annotation-app/',
                    data: form.serialize(),
                    contentType: 'application/x-www-form-urlencoded',
                    success: function (name) {
                        molgenis.createAlert([{'message': 'Annotation completed. <a href="http://localhost:8080/menu/main/dataexplorer?dataset='+name+'">Show result</a>'}], 'success');
                    }
                });
            }
        });
        submitBtn.click(function (e) {
            e.preventDefault();
            e.stopPropagation();
            form.submit();
        });

	});
	
}($, window.top.molgenis = window.top.molgenis || {}));	