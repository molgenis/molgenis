(function($, w) {
	"use strict";
	
	var ns = w.molgenis = w.molgenis || {};

	var resultsTable = null;
	var allFeatureNames = [];
	var currentPage = 1;
	var counter = 1;
	var restApi = new ns.RestClient();
	var searchApi = new ns.SearchClient();
	var protocolUri = null;
	var selectedProtocol = null;
	
		
	// fill dataset select
	ns.fillProtocolSelect = function(callback) {
		restApi.getAsync('/api/v1/protocol', null, null, function(protocols) {
			var items = [];
			// TODO deal with multiple entity pages
			$.each(protocols.items, function(key, val) {
				items.push('<option value="' + val.href + '">' + val.name + '</option>');
			});
			
			$('#protocol-select').html(items.join(''));
			$('#protocol-select').change(function() {
				protocolUri = $(this).val();
				ns.onProtocolSelectionChange();
			});
			callback();
		});
	};

	ns.onProtocolSelectionChange = function() {
		allFeatureNames = [];
		restApi.getAsync(protocolUri,["features"],null,function(protocol){
			var features = protocol.features;
			selectedProtocol = protocol;
			$('#protocol_data-table').empty();
			var items = [];
			items.push('<tr>');
			$.each(features.items, function(key, feature) {	
				allFeatureNames.push(feature.name);
			items.push('<td>'+feature.name+'</td>');		
			});
			var value = "";
			//Make first row
			items.push('</tr><tr>');
			$.each(features.items, function(key, feature) {
				
				var dataType = feature.dataType;
				var idFeature = feature.name+"_"+counter;
				if(dataType == "string" || dataType == "categorical"|| dataType == "email"|| dataType == "html"|| dataType == "hyperlink") {
					items.push('<td><input type="text" style="margin:0px;width:75px" id="'+idFeature+'"/></td>');
				}  else if (dataType == 'bool') {
					items.push('<td><input type="checkbox"/></td>');
					
				} else if (dataType == 'date') {
					items.push('<td><input type="date"/></td>');
				
				} else if (dataType == 'datetime') {
					items.push('<td><input type="datetime"/></td>');
					
				} 
				else{
					items.push('<td><input type="text" style="margin:0px;width:75px" id="'+idFeature+'"></td>');
				}
				});
			$('#protocol_data-table').append(items+'</tr>');
		});
	};
	
	ns.htmlEscape = function (text) {
		return $('<div/>').text(text).html(); 
	};
	
	ns.addRow = function(){
		counter++;	
		var items = [];
		items.push('<tr id="'+counter+'">');
		restApi.getAsync(protocolUri+'/features',null,null,function(features){
		$.each(features.items, function(key, feature) {
			var dataType = feature.dataType;
			var idFeature = feature.name+"_"+counter;
			if(dataType == "string" || dataType == "categorical" || dataType == "email" || dataType == "html" || dataType == "hyperlink") {
				items.push('<td><input type="text" " style="margin:0px;width:75px" id="'+idFeature+'"/></td>');
			}  else if (dataType == 'bool') {
				items.push('<td><input type="checkbox" id="'+idFeature+'"/></td>');
				
			} else if (dataType == 'date') {
				items.push('<td><input type="date" id="'+idFeature+'"/></td>');
			
			} else if (dataType == 'datetime') {
				items.push('<td><input type="datetime" id="'+idFeature+'"/></td>');
				
			} else{
				items.push('<td><input type="text" style="margin:0px;width:75px" id="'+idFeature+'"/></td>');
			}
			
			});
		$('#protocol_data-table').append(items+'</tr>');
		});		
	}

	ns.save = function(){
		var listOfValues= [];
		for(var i = 1; i <= counter;i++){
			var map = {}; 
			$.each(allFeatureNames, function(key, feature) {
				var value = $('#'+feature+'_'+i).val();
				
				//Doesn't not have to be a hash map, any key/value map is fine
				map[feature] = value;
				
			});
			
			listOfValues.push(map);
		}
	
		$.ajax({
			type : 'POST',
			url : '/plugin/protocolmanager/save',
			data : JSON.stringify({
				'protocolManagerRequest' : listOfValues,
				'protocolIdentifier' :selectedProtocol.identifier
				}),
		 	success: function () {
            	$(document).trigger('savedRowsSuccessfully', 'Successfully added new rows');
            	ns.onProtocolSelectionChange();	
			 },
			contentType : 'application/json'
		});	
	}
	
	
	// on document ready
	$(function() {
		resultsTable = new ns.ResultsTable();
		
		$('#add-button').click(function() {
			ns.addRow();
		});
		
		$('#save-button').click(function() {
			ns.save();
		});
		
		$(document).on('savedRowsSuccessfully', function(e, msg) {
			$('#modals').after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
			
			
		});
		
	});
}($, window.top));