(function($, molgenis) {
	"use strict";
	var restApi = new molgenis.RestClient();
	var ATTRIBUTE_KEYS = ['name', 'label', 'fieldType', 'description', 'refEntity', 'nillable', 'readOnly', 'unique'];

	
	$.fn.attributeMetadataTable = function(options) {
		var container = this;
		var attributeMetadata = options.attributeMetadata;
		container.html('');
		
		var panel = $('<div class="panel"></div>');
		container.append(panel);
		panel.append('<div class="panel-heading"><h4 class="panel-title">Data item details</h4></div>');
		var panelBody = $('<div class="panel-body"></div>');
		panel.append(panelBody);
		var table = $('<table class="table"></table>');
		panelBody.append(table);
		
		for (var i = 0;i < ATTRIBUTE_KEYS.length; i++) {
			var key = ATTRIBUTE_KEYS[i];
			var value = attributeMetadata[key];
			
			if ((key !== 'refEntity') || (attributeMetadata.fieldType === 'CATEGORICAL')) {
				var tr = $('<tr></tr>');
				table.append(tr);
				
				var th = $('<th></th>');
				tr.append(th);
				th.text(key);
				
				var td = $('<td></td>');
				tr.append(td);
				
				if (key === 'refEntity') {
					(function (td) {
						restApi.getAsync(value.href, {}, function(entity){
							td.text(entity.label);
						});
					})(td);
				} else {
					td.text(value);
				}
			}
		}
		
		if (attributeMetadata.fieldType === 'CATEGORICAL') {
			var panel = $('<div class="panel"></div>');
			container.append(panel);
			
			
			restApi.getAsync(attributeMetadata.refEntity.href,  {'expand': ['attributes']}, function(refEntityMetadata){	
				panel.append('<div class="panel-heading"><h4 class="panel-title">Possible values (refEntity = ' + refEntityMetadata.label + ')</h4></div>');
				var panelBody = $('<div class="panel-body"></div>');
				panel.append(panelBody);
				var table = $('<table class="table"></table>');
				panelBody.append(table);
				
				$.each(refEntityMetadata.attributes, function() {
					table.append('<th>' + this.label + '</th>');
				});
				
				var maxRows = 3;
				restApi.getAsync(attributeMetadata.refEntity.href.replace('/meta', ''), {num: maxRows}, function(data){
					$.each(data.items, function(index, item) {
						var tr = $('<tr></tr>');
						table.append(tr);
						$.each(refEntityMetadata.attributes, function(index, attr) {
							tr.append('<td>' + item[attr.name] + '</td>');
						});
					});
					
					if (data.total > maxRows) {
						panelBody.append('<a href="/menu/main/dataexplorer?dataset=' + refEntityMetadata.name + '">And ' + (data.total-maxRows) + ' more...</a>');
					}
				});
			});
		}
		
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));