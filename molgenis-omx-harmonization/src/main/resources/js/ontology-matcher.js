(function($, w) {
	
	"use strict";
	var ns = w.molgenis = w.molgenis || {};
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var CONTEXT_URL = null;
	
	ns.setContextURL = function(CONTEXT_URL){
		this.CONTEXT_URL = CONTEXT_URL;
	};
	
	ns.getContextURL = function() {
		return this.CONTEXT_URL;
	};
	
	ns.disableSecondStep = function() {		
		$('#add-target-dataset').attr('disabled', 'disabled');
		$('#targetDataSets').attr('disabled', 'disabled');
		$('#start-match').attr('disabled', 'disabled');
		$('#div-select-target-catalogue').css('opacity', 0.8);
	};
	
	ns.enableSecondStep = function() {		
		$('#add-target-dataset').removeAttr('disabled');
		$('#targetDataSets').removeAttr('disabled');
		$('#div-select-target-catalogue').css('opacity', 1.0);
	};
	
	ns.changeSourceDataSet = function() {
		var sourceDataSet = $('#sourceDataSet option:selected');
		if(sourceDataSet !== null){
			$('#source-catalogue').empty().append(sourceDataSet.text());
			ns.copySelectOptions();
			ns.enableSecondStep();
		}
	};
	
	ns.copySelectOptions = function() {
		$('#targetDataSets').empty();
		$('#target-catalogue').removeData('selectedOptions');
		ns.disableSecondStep();
		$('#sourceDataSet option').each(function(){
			if(!$(this).attr('selected')){
				var option = $('<option />').attr('value', $(this).val()).text($(this).text());
				$('#targetDataSets').append(option);
			}
		});
	};
	
	ns.addTargetDataSet = function() {
		var targetDataSet = $('#targetDataSets option:selected');
		if(targetDataSet !== null){
			var targetDataSetId = $(targetDataSet).val();
			var selectedOptions = $('#target-catalogue').data('selectedOptions') === undefined ? [] : $('#target-catalogue').data('selectedOptions');
			if($.inArray(targetDataSetId, selectedOptions) === -1){
				selectedOptions.push(targetDataSetId);
				$('#target-catalogue').data('selectedOptions', selectedOptions);
			}
			var displayText = '';
			$.each(selectedOptions, function(index, targetDataSetId){
				var dataSet = restApi.get('/api/v1/dataset/' + targetDataSetId);
				displayText += dataSet.name + ' , ';
			});
			$('#target-catalogue').empty().append(displayText.substring(0, displayText.length - 2));
			$('#start-match').removeAttr('disabled');
		}
	};
	
	ns.selectCatalogue = function(action){
		var selectedOptions = $('#target-catalogue').data('selectedOptions');
		var selectedSourceDataSetId = $('#sourceDataSet').val();
		var selectedDataSets = [];
		if(selectedOptions !== undefined && selectedOptions !== null){
			$.each(selectedOptions, function(index, dataSetId){
				selectedDataSets.push(dataSetId);
			});
		}
		var request = {
			'sourceDataSetId' : selectedSourceDataSetId,
			'selectedDataSetIds' : selectedDataSets
		}
		$.ajax({
			type : 'POST',
			url : ns.getContextURL() + '/' + action,
			data : JSON.stringify(request),
			contentType : 'application/json',
			async : false,
			success : function(response) {
				ns.showMessageDialog(response.message);
				if(response.isRunning){ 
					$('#start-match').attr('disabled', 'disabled');
					$('#confirm-match').hide();
				} else {
					$('#confirm-match').show();
					$('#start-match').attr('disabled', 'disabled');
				}
			},
			error : function(status) {
				alert('error');
			}
		});	
	};
	
	ns.getExistingMappings = function(dataSetId){
		var mappedDataSets = restApi.get('/api/v1/dataset', null, {
			q : [{
				field : 'identifier',
				operator : 'LIKE',
				value : dataSetId + '-'
			}]
		})
		if(mappedDataSets.items.length > 0){
			var existingMappings = '';
			$.each(mappedDataSets.items, function(index, dataSet){
				var identifier = dataSet.identifier;
				var mappedDataSetId = identifier.split('-')[1];
				var mappedDataSet = restApi.get('/api/v1/dataset/' + mappedDataSetId);
				existingMappings += mappedDataSet.name + ' , ';
			});
			existingMappings = existingMappings.substring(0, existingMappings.length - 2);
			
			if($('.list-existing-mappings').length > 0) {
				$('.list-existing-mappings').remove();
			}
			var newRow = $('<dt class="list-existing-mappings">&nbsp;&nbsp;Existing mappings :</dt>').css('margin-top', 30).after('<dd class="list-existing-mappings">' + existingMappings + '</dd>');
			$('#div-existing-mapping dl:eq(0)').append(newRow);
		}
	};
	
	ns.showMessageDialog = function(message){
		$('#alert-message').hide().empty();
		var content = '<button type="button" class="close" data-dismiss="alert">&times;</button>';
		content += '<p><strong>Message : </strong> ' + message + '</p>';
		$('#alert-message').append(content).addClass('alert alert-info').show();
		w.setTimeout(function(){
			$('#alert-message').fadeOut().empty();
		}, 10000);
		$(document).scrollTop(0);	
	};
	
	$(document).ready(function(){
		
		ns.disableSecondStep();
		
		$('#confirm-match').click(function(){
			ns.selectCatalogue('match');
		}).hide();
		
		$('#select-source-dataset').click(function(){
			if($('#sourceDataSet').val() !== ''){
				ns.changeSourceDataSet();
				ns.getExistingMappings($('#sourceDataSet').val());
			}
			return false;
		});
		
		$('#add-target-dataset').click(function(){
			ns.addTargetDataSet();
			return false;
		});
		
		$('#sourceDataSet').change(function(){
			$('#ontologymatcher-form').attr({
				'action' : ns.getContextURL(),
				'method' : 'GET'
			}).submit();
		});
		
		$('#reset-selection').click(function(){
			$('#ontologymatcher-form').attr({
				'action' : ns.getContextURL(),
				'method' : 'GET'
			}).submit();
		});
		
		$('#start-match').click(function(){
			ns.selectCatalogue('check');
		});
	});
	
}($, window.top));