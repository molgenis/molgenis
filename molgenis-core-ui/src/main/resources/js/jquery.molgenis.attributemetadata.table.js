(function($, molgenis) {
	"use strict";
	var restApi = new molgenis.RestClient();
	var ATTRIBUTE_KEYS = [ 'name', 'label', 'fieldType', 'description', 'nillable', 'readOnly', 'unique' ];

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

		for (var i = 0; i < ATTRIBUTE_KEYS.length; i++) {
			var key = ATTRIBUTE_KEYS[i];
			var value = attributeMetadata[key] !== undefined ? attributeMetadata[key] : '';
			table.append('<tr><th>' + key + '</th><td>' + value + '</td></tr>');
		}

		var refEntity = attributeMetadata['refEntity'];
		if (refEntity && (attributeMetadata.fieldType !== 'COMPOUND')) {
			restApi.getAsync(refEntity.href, {}, function(entity) {
				table.append('<tr><th>refEntity</th><td>' + entity.label + '</td></tr>');
			});
		}

		if (attributeMetadata.fieldType === 'CATEGORICAL') {
			var panel = $('<div class="panel"></div>');
			container.append(panel);

			restApi.getAsync(attributeMetadata.refEntity.href, {
				'expand' : [ 'attributes' ]
			}, function(refEntityMetadata) {
				panel.append('<div class="panel-heading"><h4 class="panel-title">Possible values (refEntity = ' + refEntityMetadata.label + ')</h4></div>');
				var panelBody = $('<div class="panel-body"></div>');
				panel.append(panelBody);
				var table = $('<table class="table"></table>');
				panelBody.append(table);

				$.each(refEntityMetadata.attributes, function() {
					table.append('<th>' + this.label + '</th>');
				});

				var maxRows = 3;
				restApi.getAsync(attributeMetadata.refEntity.href.replace('/meta', ''), {
					num : maxRows
				}, function(data) {
					$.each(data.items, function(index, item) {
						var tr = $('<tr></tr>');
						table.append(tr);
						$.each(refEntityMetadata.attributes, function(index, attr) {
							tr.append('<td>' + item[attr.name] + '</td>');
						});
					});

					if (data.total > maxRows) {
						panelBody.append('<a href="/menu/main/dataexplorer?entity=' + refEntityMetadata.name + '">And ' + (data.total - maxRows) + ' more...</a>');
					}
				});
			});
		}

	}

}($, window.top.molgenis = window.top.molgenis || {}));