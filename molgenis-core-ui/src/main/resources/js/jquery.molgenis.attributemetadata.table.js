(function($, molgenis) {
	"use strict";
	
	$.fn.attributeMetadataTable = function(options) {
		var container = this;
		var attributeMetadata = options.attributeMetadata;
		console.log(JSON.stringify(attributeMetadata));
		
		var items = [];
		
		items.push('<table class="table">');
		items.push('<tbody>');
		
		for (var f in attributeMetadata) {
			if (f !== 'href') {
				items.push('<tr>');
				items.push('<td>' + f + '</td>');
				items.push('<td>' + attributeMetadata[f] + '</td>');
				items.push('</tr>')
			};
		}
		items.push('</tbody>');
		items.push('</table');
		
		
		container.html(items.join(''));
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));