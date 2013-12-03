(function($, molgenis) {
	var restApi = new molgenis.RestClient();
	var catalogContainer;
	var Catalog = molgenis.Catalog = molgenis.Catalog || {};

	Catalog.getEnableSelection = function() {
		return typeof Catalog.enableSelection !== 'undefined' ? Catalog.enableSelection : false; 
	};
	
	Catalog.setEnableSelection = function(enableSelection) {
		Catalog.enableSelection = enableSelection;
	};
	
	Catalog.getFeature = function(featureUri) {
		return restApi.get(featureUri);
	};
	
	var onCatalogChange = function(catalogId) {
		$.ajax({
			type : 'GET',
			url : molgenis.getContextUrl() + '/selection/' + catalogId,
			success : function(selection) {
				updateCatalog(catalogId, selection);
				updateFeatureDetails(null);
				updateFeatureSelection(selection);
			},
			error : function(xhr) {
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
			}
		});
	};

	var updateCatalog = function(catalogId, selection) {
		catalogContainer.catalog({
			'selection' : Catalog.getEnableSelection(),
			'protocolId' : catalogId,
			'selectedItems' : selection ? selection : null,
			'sort' : molgenis.naturalSort,
			'onItemClick' : function(featureUri) {
				updateFeatureDetails(featureUri);
			},
			'onItemSelect' : function(featureUri, select) {
				var catalogItems = catalogContainer.catalog('getSelectedItems'); //FIXME returns the selected items of the search tree when searching
				updateShoppingCart(catalogItems, catalogId);
				updateFeatureSelection(catalogItems);
			},
			'onFolderSelect' : function(protocolUri, select) {
				var catalogItems = catalogContainer.catalog('getSelectedItems'); //FIXME returns the selected items of the search tree when searching
				updateShoppingCart(catalogItems, catalogId);
				updateFeatureSelection(catalogItems);
			}
		});
	};

	var updateFeatureDetails = function(featureUri) {
		var detailsContainer = $('#feature-details');
		if (featureUri === null) {
			detailsContainer.html("<p>Select a variable to display variable details</p>");
		} else {
			getFeature(featureUri, function(feature) {
				var table = $('<table />');

				table.append('<tr><td>' + "Name:" + '</td><td>' + feature.name + '</td></tr>');
				table.append('<tr><td>' + "Identifier:" + '</td><td>' + feature.identifier + '</td></tr>');
				$.each(molgenis.i18n.getAll(feature.description), function(key, val) {
					table.append('<tr><td>' + "Description (" + key + "):" + '</td><td>' + val + '</td></tr>');
				});

				table.append('<tr><td>' + "Data type:" + '</td><td>' + (feature.dataType ? feature.dataType : '') + '</td></tr>');
				if (feature.unit)
					table.append('<tr><td>' + "Unit:" + '</td><td>' + (feature.unit.name ? feature.unit.name : '') + '</td></tr>');

				table.addClass('listtable feature-table');
				table.find('td:first-child').addClass('feature-table-col1');
				detailsContainer.html(table);

				if (feature.categories && feature.categories.length > 0) {
					var categoryTable = $('<table class="table table-striped table-condensed" />');
					$('<thead />').append('<th>Code</th><th>Label</th><th>Description</th>').appendTo(categoryTable);
					$.each(feature.categories, function(i, category) {
						var row = $('<tr />');
						$('<td />').text(category.valueCode).appendTo(row);
						$('<td />').text(category.name).appendTo(row);
						$('<td />').text(category.description).appendTo(row);
						row.appendTo(categoryTable);
					});
					categoryTable.addClass('listtable');
					detailsContainer.append(categoryTable);
				}
			});
		}
	};

	var getFeature = function(id, callback) {
		var data = restApi.get(id); // TODO async instead of sync
		$.ajax({
			type : 'POST',
			url : '/api/v1/category?_method=GET',
			data : JSON.stringify({
				q : [ {
					"field" : "observableFeature_Identifier",
					"operator" : "EQUALS",
					"value" : data.identifier
				} ],
				num : 100
			}),
			contentType : 'application/json',
			async : false,
			success : function(entities) {
				var categories = [];
				$.each(entities.items, function() {
					categories.push($(this)[0]);
				});
				data["categories"] = categories;
			},
			error : function(xhr) {
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
			}
		});
		callback(data);
	};

	var updateFeatureSelection = function(features) {
		var selectionContainer = $('#feature-selection');
		if (features && features.length > 0) {
			var table = $('<table class="table table-striped table-condensed table-hover" />');
			$('<thead />').append('<th>Group</th><th>Variable Name</th><th>Variable Identifier</th><th>Description</th><th>Remove</th>').appendTo(table);
			$.each(features, function(i, feature) {
				var feature = restApi.get(feature);
				var protocolName = "FIXME";
				var name = feature.name;
				var identifier = feature.identifier;
				var description = molgenis.i18n.get(feature.description);
				var row = $('<tr />').data('key', feature.href);
				$('<td />').text(typeof protocolName !== 'undefined' ? protocolName : "").appendTo(row);
				$('<td />').text(typeof name !== 'undefined' ? name : "").appendTo(row);
				$('<td />').text(typeof identifier !== 'undefined' ? identifier : "").appendTo(row);
				$('<td />').text(typeof description !== 'undefined' ? description : "").appendTo(row);
				var deleteButton = $('<i class="icon-remove"></i>');
				deleteButton.click(function() {
					var featureUri = $(this).closest('tr').data('key');
					catalogContainer.catalog('selectItem', {
						'feature' : featureUri,
						'select' : false
					});
					return false; // TODO do we need this?
				});
				$('<td class="center" />').append(deleteButton).appendTo(row);

				row.appendTo(table);
			});
			table.addClass('listtable selection-table');
			selectionContainer.html(table);
		} else {
			selectionContainer.html('<p>No variables selected</p>');
		}
	};

	var updateShoppingCart = function(catalogItems, catalogId) {
		if (catalogItems === null) {
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/cart/empty',
				error : function(xhr) {
					molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				}
			});
		} else {
			$.ajax({
				type : 'POST',
				url : molgenis.getContextUrl() + '/cart/replace/' + catalogId,
				data : JSON.stringify({
					'features' : $.map(catalogItems, function(catalogItem) {
						return {
							'feature' : catalogItem.substring(catalogItem.lastIndexOf('/') + 1)
						// TODO href --> id mapping not elegant
						};
					})
				}),
				contentType : 'application/json',
				error : function(xhr) {
					molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				}
			});
		}
	};

	$(function() {
		catalogContainer = $('#catalog-container');
		var catalogSelect = $('#catalog-select');

		catalogSelect.change(function() {
			onCatalogChange($(this).val());
		});

		$('#download-xls-button').click(function() {
			window.location = molgenis.getContextUrl() + '/download/' + catalogSelect.val();
		});

		// prevent user form submission by pressing enter
		$(window).keydown(function(e) {
			if (e.keyCode === 13 || e.which === '13') {
				e.preventDefault();
				return false;
			}
		});

		$(document).on('molgenis-login', function(e, msg) {
			molgenis.createAlert([ {
				'message' : msg
			} ], 'success');
			$('#orderdata-href-btn').removeClass('disabled');
			$('#ordersview-href-btn').removeClass('disabled');
			$('#catalog-select').change(); // reset catalog
		});

		$(document).on('click', '#orderdata-href-btn', function() {
			$('#orderdata-modal-container').data("catalog-id", $('#catalog-select').val());
		});

		$(document).on('molgenis-order-placed', function(e, msg) {
			$('#catalog-select').change(); // reset catalog
			molgenis.createAlert([ {
				'message' : msg
			} ], 'success');
		});

		catalogSelect.change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));