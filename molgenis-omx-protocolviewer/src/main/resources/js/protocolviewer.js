(function($, w) {
	"use strict";

	var ns = w.molgenis = w.molgenis || {};

	var restApi = new ns.RestClient();

	ns.selectDataSet = function(id) {
		// get
		getDataSet(id, function(data) {
			// update
			setFeatureDetails(null);
			updateFeatureSelection(null);
			updateProtocolView(data.protocol);
			// set selected data set
			$(document).data('datasets').selected = id;
		});
	};

	ns.getSelectedDataSet = function() {
		return $(document).data('datasets').selected;
	};

	ns.getSelectedVariables = function() {
		var tree = $('#dataset-browser').dynatree('getTree');
		var features = $.map(tree.getSelectedNodes(), function(node) {
			return node.data.isFolder ? null : {feature: node.data.key};
		});
		return features;
	};
	
	ns.clearSearch = function() {
		var root = $("#dataset-browser").dynatree("getRoot");
		// reset to initial display
		root.visit(function(node) {
			if (node.li)
				$(node.li).show();
			if (node.ul)
				$(node.ul).show();
			node.expand(false);
		}, true);
		root.expand(true);

		// display selected nodes
		var nodes = root.tree.getSelectedNodes();
		for ( var i = 0; i < nodes.length; i++) {
			var node = nodes[i];
			while (node.parent) {
				node.parent.expand(true);
				node = node.parent;
			}
		}
	};

	ns.search = function(query) {
		if (query) {
			$('#spinner').modal('show');
			var dataSets = $(document).data('datasets');
			var dataSet = dataSets[dataSets.selected];
			$.ajax({
				url : 'molgenis.do?__target=ProtocolViewer&__action=download_json_searchdataset',
				data : {
					'query' : query,
					'id' : dataSet.id
				},
				success : function(data) {
					var rootNode = $("#dataset-browser").dynatree("getRoot");
					insertInExistingNodes(data, rootNode);
					var keys = getBottomNodes(data, []);
					showNodes(rootNode, keys);
					$('#spinner').modal('hide');
				},
				error : function() {
					$('#spinner').modal('hide');
				}
			});
		}
	};

	function getDataSet(id, callback) {
		var dataSets = $(document).data('datasets');
		if (typeof dataSets === 'undefined') {
			dataSets = {};
			$(document).data('datasets', dataSets);
		}

		if (!dataSets[id]) {
			$.getJSON('molgenis.do?__target=ProtocolViewer&__action=download_json_getdataset&datasetid=' + id, function(data) {
				dataSets[id] = data;
				callback(data);
			});
		} else {
			callback(dataSets[id]);
		}
	}

	function getFeature(id, callback) {
		$.getJSON('molgenis.do?__target=ProtocolViewer&__action=download_json_getfeature&featureid=' + id, function(data) {
			callback(data);
		});
	}

	function updateProtocolView(protocol) {
		var container = $('#dataset-browser');
		if (container.children('ul').length > 0) {
			container.dynatree('destroy');
		}
		container.empty();
		if (typeof protocol === 'undefined') {
			container.append("<p>Catalog does not describe variables</p>");
			return;
		}

		// render tree and open first branch
		container.dynatree({
			checkbox : true,
			selectMode : 3,
			minExpandLevel : 2,
			debugLevel : 0,
			children : [ {
				key : protocol.id,
				title : protocol.name,
				isFolder : true,
				isLazy : true,
				children : createNodes(protocol, null)
			} ],
			onClick : function(node, event) {
				if (node.getEventTargetType(event) === "title" && !node.data.isFolder)
					getFeature(node.data.key, function(data) {
						setFeatureDetails(data);
					});
			},
			onQuerySelect : function(select, node) {
				// if it has children?
				if (node.data.isFolder) {
					if (node.hasChildren()) {
						var reRenderNode = checkExistenceOfAllSubNodes(node);
						if (reRenderNode) {
							retrieveNodeInfo(node, true, null);
						}
					} else {
						retrieveNodeInfo(node, true, null);
					}
				}
			},
			onSelect : function(select, node) {
				// update feature details
				if (select && !node.data.isFolder)
					getFeature(node.data.key, function(data) {
						setFeatureDetails(data);
					});
				else
					setFeatureDetails(null);
				// update feature selection
				updateFeatureSelection(node.tree);
			},
			onLazyRead : function(node) {
				retrieveNodeInfo(node, false, null);
			}
		});
	}

	// recursively build tree for protocol, the extra dynatree node options
	// can be passed to the function to give different features to nodes.
	function createNodes(protocol, options) {
		var branches = [];
		if (protocol.subProtocols) {
			protocol.subProtocols.sort(characteristicSort);
			$.each(protocol.subProtocols, function(i, subProtocol) {
				var subBranches = createNodes(subProtocol, options);
				var newBranch = {
					key : subProtocol.id,
					title : subProtocol.name,
					isLazy : true,
					isFolder : true,
					children : subBranches
				};
				for ( var key in options) {
					if (options.hasOwnProperty(key)) {
						newBranch[key] = options[key];
					}
				}
				branches.push(newBranch);
			});
		} else if (protocol.features) {
			protocol.features.sort(characteristicSort);
			$.each(protocol.features, function(i, feature) {
				// use first description as tooltip
				var tooltip = null;
				if (feature.i18nDescription) {
					for ( var lang in feature.i18nDescription) {
						if (feature.i18nDescription.hasOwnProperty(lang)) {
							tooltip = feature.i18nDescription[lang];
							break;
						}
					}
				}

				var newBranch = {
					key : feature.id,
					title : feature.name,
					tooltip : tooltip
				};
				for ( var option in options) {
					if (options.hasOwnProperty(option)) {
						newBranch[option] = options[option];
					}
				}
				branches.push(newBranch);
			});
		}
		return branches;
	}

	function characteristicSort(a, b) {
		return naturalSort(a.name, b.name);
	}
	
	function checkExistenceOfAllSubNodes(node) {
		var reRenderNode = false;
		var listOfChildren = node.childList;
		for ( var i = 0; i < listOfChildren.length; i++) {
			var eachChildNode = listOfChildren[i];
			if (eachChildNode.data.isFolder) {
				if (eachChildNode.hasChildren()) {
					reRenderNode = reRenderNode || checkExistenceOfAllSubNodes(eachChildNode);
				} else {
					reRenderNode = true;
					break;
				}
			}
		}
		return reRenderNode;
	}

	function retrieveNodeInfo(node, recursive, options) {
		if (recursive)
			$('#spinner').modal('show');
		$.ajax({
			url : 'molgenis.do?__target=ProtocolViewer&__action=download_json_getprotocol',
			async : false,
			data : {
				'id' : node.data.key,
				'recursive' : recursive
			},
			success : function(data) {

				if (data) {
					var branches = createNodes(data, options);
					node.setLazyNodeStatus(DTNodeStatus_Ok);
					node.removeChildren();
					node.addChild(branches);
					if (node.isSelected()) {
						node.getChildren().forEach(function(eachNode) {
							eachNode.select(true);
						});
					}
				}
				if (recursive)
					$('#spinner').modal('hide');
			},
			error : function() {
				if (recursive)
					$('#spinner').modal('hide');
			}
		});
	}

	function setFeatureDetails(feature) {
		var container = $('#feature-details').empty();
		if (feature === null) {
			container.append("<p>Select a variable to display variable details</p>");
			return;
		}

		var table = $('<table />');
		table.append('<tr><td>' + "Name:" + '</td><td>' + feature.name + '</td></tr>');
		if (feature.i18nDescription) {
			for ( var lang in feature.i18nDescription) {
				if (!feature.i18nDescription.hasOwnProperty(lang))
					continue;
				table.append('<tr><td>' + "Description (" + lang + "):" + '</td><td>' + feature.i18nDescription[lang] + '</td></tr>');
			}
		}

		table.append('<tr><td>' + "Data type:" + '</td><td>' + (feature.dataType ? feature.dataType : '') + '</td></tr>');
		if (feature.unit)
			table.append('<tr><td>' + "Unit:" + '</td><td>' + feature.unit.name + '</td></tr>');

		table.addClass('listtable feature-table');
		table.find('td:first-child').addClass('feature-table-col1');
		container.append(table);

		if (feature.categories) {
			var categoryTable = $('<table class="table table-striped table-condensed" />');
			$('<thead />').append('<th>Code</th><th>Label</th><th>Description</th>').appendTo(categoryTable);
			$.each(feature.categories, function(i, category) {
				var row = $('<tr />');
				$('<td />').text(category.code).appendTo(row);
				$('<td />').text(category.name).appendTo(row);
				$('<td />').text(category.description).appendTo(row);
				row.appendTo(categoryTable);
			});
			categoryTable.addClass('listtable');
			container.append(categoryTable);
		}
	}

	function updateFeatureSelection(tree) {
		var container = $('#feature-selection').empty();
		if (tree === null) {
			container.append("<p>No variables selected</p>");
			updateShoppingCart(null);
			return;
		}

		var nodes = tree.getSelectedNodes();
		if (nodes === null || nodes.length === 0) {
			container.append("<p>No variables selected</p>");
			updateShoppingCart(null);
			return;
		}

		var table = $('<table class="table table-striped table-condensed table-hover" />');
		$('<thead />').append('<th>Group</th><th>Variable</th><th>Description</th><th>Remove</th>').appendTo(table);
		$.each(nodes, function(i, node) {
			if (!node.data.isFolder) {
				var protocol_name = node.parent.data.title;
				var name = node.data.title;
				var description = node.data.tooltip;
				var row = $('<tr />').attr('id', node.data.key + "_row");
				$('<td />').text(typeof protocol_name !== 'undefined' ? protocol_name : "").appendTo(row);
				$('<td />').text(typeof name !== 'undefined' ? name : "").appendTo(row);
				$('<td />').text(typeof description !== 'undefined' ? description : "").appendTo(row);

				var deleteButton = $('<i class="icon-remove"></i>');
				deleteButton.click($.proxy(function() {
					tree.getNodeByKey(node.data.key).select(false);
					return false;
				}, this));
				$('<td class="center" />').append(deleteButton).appendTo(row);

				row.appendTo(table);
			}
		});
		table.addClass('listtable selection-table');
		container.append(table);
		
		var features = [];
		$.each(nodes, function(i, node) {
			if (!node.data.isFolder) {
				features.push({feature: node.data.key});
			}
		});
		updateShoppingCart(features);
	}

	function updateShoppingCart(features) {
		if (features === null) {
			$.post('/cart/empty');
		} else {
			$.ajax({
				type : 'POST',
				url : '/cart/replace',
				data : JSON.stringify({
					'features' : features
					}),
				contentType : 'application/json'
			});
		}
	}
	
	function getBottomNodes(node, hits) {
		if (node.subProtocols && node.subProtocols.length > 0) {
			$.each(node.subProtocols, function(i, subNode) {
				hits.push(getBottomNodes(subNode, hits));
			});
		} else {
			if (node.features && node.features.length > 0) {
				$.each(node.features, function(i, lastNode) {
					hits.push(lastNode.id);
				});
			}
		}
		return hits;
	}

	function insertInExistingNodes(candidateNodeData, parentNode) {
		if (!parentNode.tree.getNodeByKey(candidateNodeData.id)) {
			var options = {
				expand : false
			};
			retrieveNodeInfo(parentNode, true, options);
		} else {
			var existingNode = parentNode.tree.getNodeByKey(candidateNodeData.id);
			if (candidateNodeData.subProtocols && candidateNodeData.subProtocols.length > 0) {
				$.each(candidateNodeData.subProtocols, function(i, subProtocol) {
					insertInExistingNodes(subProtocol, existingNode);
				});
			}
		}
	}

	function showNodes(node, keys) {
		var match = $.inArray(parseInt(node.data.key, 10), keys) !== -1;
		if (node.childList) {
			node.expand(true);
			for ( var i = 0; i < node.childList.length; i++) {
				match = match | showNodes(node.childList[i], keys);
			}
		}
		if (!match) {
			if (node.li)
				$(node.li).hide();
			else if (node.ul)
				$(node.ul).hide();
		} else {
			if (node.li)
				$(node.li).show();
			else if (node.ul)
				$(node.ul).show();
		}
		return match;
	}

	// on document ready
	$(function() {
		$(document).on('molgenis-login', function(e, msg) {
			$('.alert').alert('close');
			$('.form_header').after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
			$('#orderdata-href-btn').removeClass('disabled');
			$('#ordersview-href-btn').removeClass('disabled');
			updateShoppingCart(ns.getSelectedVariables()); // session changed, update shoppingcart for already selected items
		});
		$(document).on('molgenis-order-placed', function(e, msg) {
			ns.selectDataSet(ns.getSelectedDataSet()); // reset catalogue
			$('#dataset-browser').dynatree('getRoot').select(false);
			$('.form_header').after($('<div class="alert alert-success"><button type="button" class="close" data-dismiss="alert">&times;</button><strong>Success!</strong> ' + msg + '</div>'));
		});
	});
}($, window.top));