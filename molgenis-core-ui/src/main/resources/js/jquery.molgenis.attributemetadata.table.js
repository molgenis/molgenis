(function($, molgenis) {
	"use strict";
	var restApi = new molgenis.RestClient();
	
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
		
		for (var key in attributeMetadata) {
			if (key !== 'href' && key !== 'attributes') {
				if ((key !== 'refEntity') || (attributeMetadata.fieldType !== 'COMPOUND')) {
					var value = attributeMetadata[key];
				
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
		}
		
		if (attributeMetadata.fieldType === 'CATEGORICAL') {
			var panel = $('<div class="panel"></div>');
			container.append(panel);
			panel.append('<div class="panel-heading"><h4 class="panel-title">Possible values</h4></div>');
			var panelBody = $('<div class="panel-body"></div>');
			panel.append(panelBody);
			var table = $('<table class="table"></table>');
			panelBody.append(table);
			
			restApi.getAsync(attributeMetadata.refEntity.href,  {'expand': ['attributes']}, function(refEntityMetadata){	
				$.each(refEntityMetadata.attributes, function() {
					table.append('<th>' + this.label + '</th>');
				});
				
				restApi.getAsync(attributeMetadata.refEntity.href.replace('/meta', ''), {}, function(data){
					$.each(data.items, function(index, item) {
						var tr = $('<tr></tr>');
						table.append(tr);
						$.each(refEntityMetadata.attributes, function(index, attr) {
							tr.append('<td>' + item[attr.name] + '</td>');
						});
					});
				});
			});
		}
		
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));