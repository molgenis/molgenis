(function($, molgenis) {
	"use strict";
	var restApi = new molgenis.RestClient();
	
	$.fn.attributeMetadataTable = function(options) {
		var container = this;
		var attributeMetadata = options.attributeMetadata;
		container.html('');
		
		var table = $('<table class="table"></table>');
		container.append(table);
		
		for (var key in attributeMetadata) {
			if (key !== 'href' && key != 'attributes') {
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
	
}($, window.top.molgenis = window.top.molgenis || {}));