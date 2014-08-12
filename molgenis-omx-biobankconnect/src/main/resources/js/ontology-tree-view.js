(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	var standardModal = new molgenis.StandardModal();
	var TREE_LABEL = "label";
	var ROOT = "root";
	var LAST = "isLast";
	var ENTITY_TYPE = "entity_type";
	var ONTOLOGY_IRI = "ontologyIRI";
	var ONTOLOGY_NAME = "ontologyName";
	var ONTOLOGY_TERM = "ontologyTerm";
	var ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	var SYNONYMS = "ontologyTermSynonym";
	var ONTOLOGY_TERM_DEFINITION = "definition";
	var NODE_PATH = "nodePath";
	var PARENT_NODE_PATH = "parentNodePath";
	var PARENT_ONTOLOGY_TERM_IRI = "parentOntologyTermIRI";
	var treeDict = null;
	var container = null;
	
	molgenis.OntologyTree = function OntologyTree(treeContainerId){
		container = $('#' + treeContainerId);
	};
	
	molgenis.OntologyTree.prototype.updateOntologyTree = function(ontologyIRI){
		var ontologyIndex = getOntologyTermByIri(ontologyIRI);
		if(ontologyIndex && ontologyIndex.items.length > 0){
			var topNode = ontologyIndex.items[0];
			topNode.attributes = removeDuplicate(getRootOntologyTerms(topNode));
			createEntityMetaTree(topNode, null);
		}
		
		function getRootOntologyTerms(ontology){
			var rootOntologyTerms = restApi.get('/api/v1/' + ontology[ONTOLOGY_NAME], {'expand' : ['attributes'], 'q' : {
				'q' : [{
					'field' : ROOT,
					'operator' : 'EQUALS',
					'value' : true
				},{
					'operator' : 'AND'
				},{
					'field' : ONTOLOGY_IRI,
					'operator' : 'EQUALS',
					'value' : ontology[ONTOLOGY_IRI]
				},{
					'operator' : 'AND'
				},{
					'field' : LAST,
					'operator' : 'EQUALS',
					'value' : false
				}]
			}});
			return rootOntologyTerms.items;
		}
	};
	
	molgenis.OntologyTree.prototype.queryTree = function(ontologyIRI, query){
		if(query !== undefined && query !== ''){
			var molgenisTree = container.tree('getTree');
			treeDict = treeDict ? treeDict : molgenisTree.rootNode.toDict(true);
			molgenisTree.reload();
			var ontologyIndex = getOntologyTermByIri(ontologyIRI);
			if(ontologyIndex.items.length > 0){
				var ontology = ontologyIndex.items[0];
				var ontologyTerms = removeDuplicate(searchByQuery(ontology, query)).sort(function(a, b){
					return molgenis.naturalSort(b.nodePath.split('.').length, a.nodePath.split('.').length);
				});
				if(ontologyTerms.length > 0){
					$.each(ontologyTerms, function(index, ontologyTerm){
						getParentNode(molgenisTree, ontologyTerm);
					});
				}else{
					molgenis.createAlert([{'message' : 'No ontology terms are found for query "<strong>' + query + '</strong>".'}], 'error');
				}
			}
		}
		
		function searchByQuery(ontology, query){
			var ontologyTermResult = restApi.get('/api/v1/' + ontology[ONTOLOGY_NAME], {'expand' : ['attributes'], 'q' : {
				'q' : [{
					'field' : SYNONYMS,
					'operator' : 'EQUALS',
					'value' : query
				}]
			}}, null);
			return ontologyTermResult.items;
		}
	};
	
	molgenis.OntologyTree.prototype.restoreTree = function(){
		if(treeDict){
			var molgenisTree = container.tree('getTree');
			molgenisTree.rootNode.fromDict(treeDict);
			treeDict = null;
		}
	};
	
	molgenis.OntologyTree.prototype.locateTerm = function(ontologyTerm){
		var molgenisTree = container.tree('getTree');
		molgenisTree.reload();
		ontologyTerm = getOntologyTerm(ontologyTerm);
		var currentNode = getParentNode(molgenisTree, ontologyTerm, true);
		currentNode.setFocus();
		var middlePosition = container.offset().top + container.height()/2;
		var scrolledDis = Math.abs($(molgenisTree.rootNode.ul).offset().top - container.offset().top);
		if($(currentNode.li).offset().top > middlePosition){
			container.scrollTop($(currentNode.li).offset().top - middlePosition + scrolledDis);			
		}else{
			container.scrollTop(scrolledDis - middlePosition + $(currentNode.li).offset().top);
		}
	};
	
	function getParentNode(molgenisTree, ontologyTerm, showSibling){
		var currentNode = null;
		if(ontologyTerm){
			var nodeKey = ontologyTerm.href;
			//Check if the node exists in the tree already
			if(!molgenisTree.getNodeByKey(nodeKey)){
				//Recursively add parent nodes to the tree first
				var parentOntologyTerm = getParentOntologyTerm(ontologyTerm);				
				var parentNode = getParentNode(molgenisTree, getParentOntologyTerm(ontologyTerm), showSibling);
				//Add current node the tree
				if(parentNode){
					 container.tree('appendChildNodes', parentNode, showSibling ? removeDuplicate(parentOntologyTerm.attributes.items) : removeDuplicate([ontologyTerm]));
				}else{
					console.log('error parent node cannot but null!');
				}	
			}
			currentNode = molgenisTree.getNodeByKey(nodeKey);
		}
		return currentNode;
	}
	
	function removeDuplicate (listOfNodes){
		var uniqueNodes = [];
		if(listOfNodes.length > 0){
			var nodeMap = {};
			$.each(listOfNodes, function(index, eachNode){
				var name = eachNode[ONTOLOGY_TERM_IRI];
				if(nodeMap[name]){
					if(eachNode[SYNONYMS] !== eachNode[ONTOLOGY_TERM]){			
						var existingNode = nodeMap[name];
						existingNode.synonyms.push(eachNode[SYNONYMS]);
						nodeMap[name] = existingNode;
					}
				}else{
					eachNode.synonyms = [];
					if(eachNode[SYNONYMS] !== eachNode[ONTOLOGY_TERM]){
						eachNode.synonyms.push(eachNode[SYNONYMS]);
					}
					nodeMap[name] = eachNode;
				}
			});
			
			//Add tree label to the node data
			$.map(nodeMap, function(value, key){
				if(value[ONTOLOGY_TERM]){
					value[TREE_LABEL] = value[ONTOLOGY_TERM];
				}
				else if(value[ONTOLOGY_NAME]){
					value[TREE_LABEL] = value[ONTOLOGY_NAME];
				}
				uniqueNodes.push(value);
			});
		}
		
		return uniqueNodes.sort(function(a, b){
			return molgenis.naturalSort(a[TREE_LABEL], b[TREE_LABEL]); 
		});
	}
	
	function getOntologyTermByIri(ontologyIRI){
		if(ontologyIRI){
			var request = {
				'q' : [{
					'field' : ONTOLOGY_IRI,
					'operator' : 'EQUALS',
					'value' : ontologyIRI
				}]
			};
			return restApi.get("/api/v1/ontologyindex/", {'q' : request}, null);
		}
		return null;
	}
	
	function getOntologyTerm(option){
		var ontologyTerms = restApi.get('/api/v1/' + option[ONTOLOGY_NAME], {'expand' : ['attributes'], 'q' : {
			'q' : [{
				'field' : ONTOLOGY_TERM_IRI,
				'operator' : 'EQUALS',
				'value' : option[ONTOLOGY_TERM_IRI]
			},{
				'operator' : 'AND'
			},{
				'field' : NODE_PATH,
				'operator' : 'EQUALS',
				'value' : option[NODE_PATH]
			}]
		}});
		return ontologyTerms.items.length > 0 ? ontologyTerms.items[0] : null;
	}
	
	function getParentOntologyTerm(option){
		var parentOntologyTerms = restApi.get('/api/v1/' + option[ONTOLOGY_NAME], {'expand' : ['attributes'], 'q' : {
			'q' : [{
				'field' : ONTOLOGY_TERM_IRI,
				'operator' : 'EQUALS',
				'value' : option[PARENT_ONTOLOGY_TERM_IRI]
			},{
				'operator' : 'AND'
			},{
				'field' : NODE_PATH,
				'operator' : 'EQUALS',
				'value' : option[PARENT_NODE_PATH]
			}]
		}});
		return parentOntologyTerms.items.length > 0 ? parentOntologyTerms.items[0] : null;
	}
	
	function createEntityMetaTree(entityMetaData, attributes) {
		container.css({
			'height' : '500px',
			'overflow' : 'auto'
		}).tree({
			'entityMetaData' : entityMetaData,
			'selectedAttributes' : attributes,
			'onAttributesSelect' : function(selects) {
				console.log(selects);
			},
			'onAttributeClick' : function(attribute) {
				$('#ontology-term-info').empty().append(ontologyTermInfo(attribute));
			},
			'lazyload' : function(data, createChildren){
				var href = data.node.data.attribute.href;
				var ontologyTerm = restApi.get(href, {'expand' : ['attributes']});
				data.result = createChildren(removeDuplicate(ontologyTerm.attributes.items));
			}
		});
		
		container.on('hover', 'span.fancytree-title', function(event){
			var node = $.ui.fancytree.getNode(event);
			var selectorElement = node.li ? $(node.li) : $(node.ul);
			var data = gatherSynonyms(node.data.attribute);
			if(data.synonyms.length > 0){
				var listOfSynonyms = $('<div />');
				$.each(data.synonyms, function(index, synonym){
					listOfSynonyms.append('* ' + synonym + '<br>');
				});
				$(node.li).children('span:eq(0)').popover({
					'title' : 'Synonyms',
					'trigger' : 'hover',
					'placement' : 'bottom',
					'html' : true,
					'content' : listOfSynonyms
				}).popover('show');
			}
		});
	}
	
	function gatherSynonyms(attribute){
		var data = {};
		var ontologyTerm = restApi.get(attribute.href, {'expand' : ['attributes']}, null);
		var baseUrl = ontologyTerm.href.substring(0, ontologyTerm.href.lastIndexOf('/') + 1)
		var relatedOntologyTerms = restApi.get(baseUrl, {'expand' : ['attributes'], 'q' : {
			'q' : [{
				'field' : ONTOLOGY_TERM_IRI,
				'operator' : 'EQUALS',
				'value' : ontologyTerm[ONTOLOGY_TERM_IRI]
			}]
		}}, null);
		if(relatedOntologyTerms.items.length > 0){
			data = removeDuplicate(relatedOntologyTerms.items)[0];
		}
		return data;
	}
	
	function ontologyTermInfo(attribute){
		
		var table = $('<table />').addClass('table').width('500px');
		var data = gatherSynonyms(attribute);
		table.append('<tr><th>Ontology</th><td><a href="' + data[ONTOLOGY_IRI] + '" target="_blank">' + data[ONTOLOGY_IRI] + '</a></td></tr>');
		table.append('<tr><th>OntologyTerm</th><td><a href="' + data[ONTOLOGY_TERM_IRI] + '" target="_blank">' + data[ONTOLOGY_TERM_IRI] + '</a></td></tr>');
		table.append('<tr><th>Name</th><td>' + data[ONTOLOGY_TERM] + '</td></tr>');
		if(data.description){
			table.append('<tr><th>Definition</th><td>' + data[ONTOLOGY_TERM_DEFINITION] + '</td></tr>');
		}
		if(data.synonyms && data.synonyms.length > 0){
			var listOfSynonyms = $('<ul />');
			$.each(data.synonyms, function(index, synonym){
				listOfSynonyms.append('<li>' + synonym + '</li>');
			});
			var synonymContainer = $('<td />').append(listOfSynonyms);
			$('<tr />').append('<th>Synonyms</th>').append(synonymContainer).appendTo(table);
		}
		table.find('th').width('30%');
		return table;
	}
}($, window.top.molgenis = window.top.molgenis || {}));