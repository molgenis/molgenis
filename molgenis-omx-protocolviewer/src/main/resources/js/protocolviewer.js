(function($, molgenis) {
	var restApi = new molgenis.RestClient();
	var catalogContainer;
	var Catalog = molgenis.Catalog = molgenis.Catalog || {};
	var maxItems = 10000;
	
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
			'selectedItems' : selection.items ? $.map(selection.items, function(selectedItem) { return selectedItem.feature; }) : null, // FIXME catalog requires group info
			'sort' : molgenis.naturalSort,
			'onItemClick' : function(featureUri) {
				updateFeatureDetails(featureUri);
			},
			'onItemSelect' : function(featureUri, select) {
				showSpinner();
				updateShoppingCart(featureUri, select, catalogId, function() {
					updateFeatureSelection(catalogId);
					hideSpinner();
				});
			},
			'onFolderSelect' : function(protocolUri, select) {
				showSpinner();
				updateShoppingCart(protocolUri, select, catalogId, function() {
					updateFeatureSelection(catalogId);
					hideSpinner();
				});
			},
			'onInit' : function() {
				updateFeatureSelection(catalogId);
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
					feature.categories.sort(function(category1, category2) {
						return molgenis.naturalSort(category1.valueCode, category2.valueCode);
					});			
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
				num : maxItems
			}),
			contentType : 'application/json',
			async : false,
			success : function(entities) {
				if (entities.total > maxItems) { 
					molgenis.createAlert([ {
						'message' : 'Feature contains more than ' + maxItems + ' categories'
					} ], 'error');
				}
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

	var updateFeatureSelection = function(catalogId) {
		
		function updateFeatureSelectionContainer(page) {
			var nrItemsPerPage = 20;
			var start = page ? page.start : 0;
			var end = page ? page.end : nrItemsPerPage;
			
			$.ajax({
				url: molgenis.getContextUrl() + '/selection/' + catalogId + '?start=' + start + '&end=' + end,
				success : function(selection) {
					var selectionTable = $('#feature-selection-table-container');
					var selectionTablePager = $('#feature-selection-table-pager');
					
					if(selection.total === 0) {
						$('#orderdata-href-btn').addClass('disabled');
						selectionTable.html('<p>No variables selected</p>');
						selectionTablePager.empty();
					} else {
						$('#orderdata-href-btn').removeClass('disabled');
						if(page === undefined) {
							selectionTablePager.pager({
								'nrItems' : selection.total,
								'nrItemsPerPage' : nrItemsPerPage,
								'onPageChange' : updateFeatureSelectionContainer
							});	
						}
						
						var catalogItems = selection.items;
						
						// get features
						var q = {
							q : [ {
								field : 'id',
								operator : 'IN',
								value : $.map(catalogItems, function(catalogItem) {
									// TODO code duplication from jquery.catalog.js hrefToId
									var href = catalogItem.feature;
	//								var href = catalogItem.item;
									return href.substring(href.lastIndexOf('/') + 1); 
								})
							} ],
							num : maxItems
						};
						restApi.getAsync('/api/v1/observablefeature', null, q, function(features) {
							if (features.total > maxItems) { 
								molgenis.createAlert([ {
									'message' : 'Maximum number of selected items reached (' + maxItems + ')'
								} ], 'error');
							}
							// get feature protocols
							q = {
									q : [ {
										field : 'id',
										operator : 'IN',
										value : $.map(catalogItems, function(catalogItem) { // FIXME dedup
											// TODO code duplication from jquery.catalog.js hrefToId
											var href = catalogItem.path[catalogItem.path.length - 1];
	//										var href = catalogItem.parent;
											return href.substring(href.lastIndexOf('/') + 1); 
										})
									} ],
									num : maxItems
								};
							// TODO deal with multiple entity pages
							restApi.getAsync('/api/v1/protocol', null, q, function(protocols) {
								if (protocols.total > maxItems) { 
									molgenis.createAlert([ {
										'message' : 'Maximum number of protocols reached (' + maxItems + ')'
									} ], 'error');
								}
								var featureMap = {};
								$.each(features.items, function() {
									featureMap[this.href] = this;
								});
								var protocolMap = {};
								$.each(protocols.items, function() {
									protocolMap[this.href] = this;
								});
								var table = $('<table id="feature-selection-table" class="table table-striped table-condensed table-hover" />');
								$('<thead />').append('<th>Group</th><th>Variable Name</th><th>Variable Identifier</th><th>Description</th><th>Remove</th>').appendTo(table);
								$.each(catalogItems, function() {
									var feature = featureMap[this.feature];
									var protocol = protocolMap[this.path[this.path.length - 1]];
									
									var protocolName = protocol.name;
									var name = feature.name;
									var identifier = feature.identifier;
									var description = molgenis.i18n.get(feature.description);
									var row = $('<tr />').data('key', this);
									$('<td />').text(typeof protocolName !== 'undefined' ? protocolName : "").appendTo(row);
									$('<td />').text(typeof name !== 'undefined' ? name : "").appendTo(row);
									$('<td />').text(typeof identifier !== 'undefined' ? identifier : "").appendTo(row);
									$('<td />').text(typeof description !== 'undefined' ? description : "").appendTo(row);
									var deleteButton = $('<i class="icon-remove"></i>');
									deleteButton.click(function() {
										var item = $(this).closest('tr').data('key');
										catalogContainer.catalog('selectItem', {
											'feature' : item.feature,
											'path' : item.path,
											'select' : false
										});
										return false; // TODO do we need this?
									});
									$('<td class="center" />').append(deleteButton).appendTo(row);
			
									row.appendTo(table);
								});
								table.addClass('listtable selection-table');
								selectionTable.html(table);
							});
						});
					}
				},
				error : function(xhr) {
					molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				}
			});
		};
		
		// create selection table with pager
		updateFeatureSelectionContainer();
	};
	
	var updateShoppingCart = function(resourceUri, select, catalogId, callback) {
		$.ajax({
			type : 'POST',
			url : molgenis.getContextUrl() + '/cart/' + (select ? 'add' : 'remove') + '/' + catalogId,
			data : JSON.stringify({'href' : resourceUri}),
			contentType : 'application/json',
			success: function() {
				callback();
			},
			error : function(xhr) {
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				callback();
			}
		});
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