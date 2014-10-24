(function($, molgenis) {
	"use strict";
	var restApi = new molgenis.RestClient();
	var ENTITY_KEYS = ['name', 'label', 'description', 'idAttribute', 'labelAttribute'];

	
	$.fn.entityMetadataTable = function(options) {
		var container = this;
		var entityMetadata = options.entityMetadata;
		container.html('');
		
		var panel = $('<div class="panel"></div>');
		container.append(panel);
		panel.append('<div class="panel-heading"><h4 class="panel-title">Data item details</h4></div>');
		
		var panelBody = $('<div class="panel-body"></div>');
		panel.append(panelBody);
		
		var table = $('<table class="table"></table>');
		panelBody.append(table);
		
		for (var i = 0;i < ENTITY_KEYS.length; i++) {
			var key = ENTITY_KEYS[i];
			var value = entityMetadata[key] !== undefined ? entityMetadata[key] : '';
			table.append('<tr><th>' + key + '</th><td>' + value + '</td></tr>');
		}	
	}
	
}($, window.top.molgenis = window.top.molgenis || {}));