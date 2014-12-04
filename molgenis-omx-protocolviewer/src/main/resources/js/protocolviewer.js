(function($, molgenis) {
	var restApi = new molgenis.RestClient();
	var Catalog = molgenis.Catalog = molgenis.Catalog || {};
	var maxItems = 10000;
	
	Catalog.getEnableSelection = function() {
		return typeof Catalog.enableSelection !== 'undefined' ? Catalog.enableSelection : false; 
	};

	Catalog.setEnableSelection = function(enableSelection) {
		Catalog.enableSelection = enableSelection;
	};
	
	Catalog.getProtocol = function(featureUri) {
		return restApi.get(featureUri);
	};

	var updateCatalog = function(catalogId, selection) {
		$('#catalog-container').catalog({
			'selection' : Catalog.getEnableSelection(),
			'protocolId' : catalogId,
			'selectedItems' : selection.items ? $.map(selection.items, function(selectedItem) { return selectedItem.protocol.toLowerCase(); }) : null, // FIXME catalog requires group info
			'sort' : function(a,b) {return molgenis.naturalSort(a.title, b.title);},
			'onItemClick' : function(featureUri) {
				updateFeatureDetails(featureUri);
			},
			'onFolderSelect' : function(protocolUri, select) {
				updateShoppingCart(protocolUri, select, catalogId);
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

				table.append('<tr><td>' + "Name:" + '</td><td>' + feature.Name + '</td></tr>');
				table.append('<tr><td>' + "Identifier:" + '</td><td>' + feature.Identifier + '</td></tr>');
				$.each(molgenis.i18n.getAll(feature.description), function(key, val) {
					table.append('<tr><td>' + "Description (" + key + "):" + '</td><td>' + val + '</td></tr>');
				});
				table.append('<tr><td>' + "Data type:" + '</td><td>' + (feature.dataType ? feature.dataType : '') + '</td></tr>');
				if (feature.unit)
					table.append('<tr><td>' + "Unit:" + '</td><td>' + (feature.unit.Name ? feature.unit.Name : '') + '</td></tr>');

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
						$('<td />').text(category.Name).appendTo(row);
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
		var data = restApi.get(id);
		$.ajax({
			type : 'POST',
			url : '/api/v1/category?_method=GET',
			data : JSON.stringify({
				q : [ {
					"field" : "observableFeature",
					"operator" : "EQUALS",
					"value" : data.Identifier
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
			}
		});
		callback(data);
	};

	var updateFeatureSelection = function(catalogId, newPageObject) {
		var nrItemsPerPage = 20;
		var pager;
		
		if(newPageObject){
			pager = newPageObject;
		}
		else {
			pager = {page:1, start:0, end:nrItemsPerPage};
		}

		$.ajax({
			url: molgenis.getContextUrl() + '/selection/' + catalogId + '?start=' + pager.start + '&end=' + pager.end,
			success : function(selection) {
				var selectionTable = $('#feature-selection-table-container');
				var selectionTablePager = $('#feature-selection-table-pager');
				
				if(selection.total === 0) {
					$('#orderdata-href-btn').addClass('disabled');
					selectionTable.html('<p>No variables selected</p>');
					selectionTablePager.empty();
				} else {
					$('#orderdata-href-btn').removeClass('disabled');
					if(!newPageObject) {
						selectionTablePager.pager({
							'nrItems' : selection.total,
							'nrItemsPerPage' : nrItemsPerPage,
							'onPageChange' : function(pageObject){
								updateFeatureSelection(catalogId, pageObject);
							}
						});
					}
					
					var catalogItems = selection.items;
					
					// get protocols
					var q = {
						q : [ {
							field : 'id',
							operator : 'IN',
							value : $.map(catalogItems, function(catalogItem) {
								return restApi.getPrimaryKeyFromHref(catalogItem.protocol); 
							})
						} ],
						num : maxItems
					};
					restApi.getAsync('/api/v1/protocol', {'q': q}, function(protocols) {
						if (protocols.total > maxItems) { 
							molgenis.createAlert([ {
								'message' : 'Maximum number of selected items reached (' + maxItems + ')'
							} ], 'error');
						}
						
						var protocolMap = {};
						$.each(protocols.items, function() {
							protocolMap[this.href.toLowerCase()] = this;
						});
						var table = $('<table id="feature-selection-table" class="table table-striped table-condensed table-hover" />');
						$('<thead />').append('<th>Variable Name</th><th>Variable Identifier</th><th>Description</th><th>Remove</th>').appendTo(table);
						$.each(catalogItems, function() {								
							var protocol = protocolMap[this.path[this.path.length - 1]];
							var protocolName = protocol.Name;
							var protocolIdentifier = protocol.Identifier;
							var description = molgenis.i18n.get(protocol.description);
							var row = $('<tr />').data('key', this);
							$('<td />').text(typeof protocolName !== 'undefined' ? protocolName : "").appendTo(row);
							$('<td />').text(typeof protocolIdentifier !== 'undefined' ? protocolIdentifier : "").appendTo(row);
							$('<td />').text(typeof description !== 'undefined' ? description : "").appendTo(row);
							var deleteButton = $('<i class="icon-remove"></i>');
							
							deleteButton.click(function() {
								var item = $(this).closest('tr').data('key');
								$('#catalog-container').catalog('selectItem', {
									'feature' : item.protocol,
									'path' : $.map(item.path, function(pathPart){return pathPart.toLowerCase()}),
									'select' : false
								});
								return false;
							});
							$('<td class="center" />').append(deleteButton).appendTo(row);
	
							row.appendTo(table);
						});
						table.addClass('listtable selection-table');
						selectionTable.html(table);
					});
				}
			}
		});
	};
	
	var updateShoppingCart = function(protocolUri, select, catalogId) {
		var protocolId = restApi.getPrimaryKeyFromHref(protocolUri);
		
        $('#catalog-tree').fancytree('disable');
		$.ajax({
			type : 'POST',
			async : false,
			url : molgenis.getContextUrl() + '/cart/' + (select ? 'add' : 'remove') + '/' + catalogId,
			data : JSON.stringify({'protocolId' : protocolId}),
			contentType : 'application/json',
			success: function() {
				updateFeatureSelection(catalogId);
			},
			error : function(xhr) {
				molgenis.createAlert(JSON.parse(xhr.responseText).errors);
				updateFeatureSelection(catalogId);
			},
			complete : function() {
				$('#catalog-tree').fancytree('enable');
			}
		});
	};

	$(function() {
		$('#catalog-select').change(function() {
			var catalogId = $(this).val();
			$.ajax({
				type : 'GET',
				url : molgenis.getContextUrl() + '/selection/' + catalogId,
				success : function(selection) {
					updateCatalog(catalogId, selection);
				}
			});
		});

		$('#download-xls-button').click(function() {
			window.location = molgenis.getContextUrl() + '/download/' + $('#catalog-select').val();
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

		$('#catalog-select').change();
	});
}($, window.top.molgenis = window.top.molgenis || {}));