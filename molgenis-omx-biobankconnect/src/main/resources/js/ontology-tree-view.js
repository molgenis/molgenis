(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	var TREE_LABEL = "label";
	var ROOT = "root";
	var LAST = "isLast";
	var ENTITY_TYPE = "entity_type";
	var ONTOLOGY_IRI = "ontologyIRI";
	var ONTOLOGY_LABEL = "ontologyLabel";
	var ONTOLOGY_TERM = "ontologyTerm";
	var ONTOLOGY_TERM_IRI = "ontologyTermIRI";
	var SYNONYMS = "ontologyTermSynonym";
	var ONTOLOGY_TERM_DEFINITION = "definition";
	var NODE_PATH = "nodePath";
	var PARENT_NODE_PATH = "parentNodePath";
	
	molgenis.OntologyTree = function OntologyTree(){};
	
	molgenis.OntologyTree.prototype.updateOntologyTree = function(ontologyIRI){
		var ontologyIndex = getOntologyTermByIri(ontologyIRI);
		if(ontologyIndex.items.length > 0){
			var topNode = ontologyIndex.items[0];
			topNode.attributes = removeDuplicate(getRootOntologyTerms(topNode));
			createEntityMetaTree(topNode, null);
		}
	};
	
	molgenis.OntologyTree.prototype.queryTree = function(ontologyIRI, query){
		if(query !== undefined && query !== ''){
			var ontologyIndex = getOntologyTermByIri(ontologyIRI);
			if(ontologyIndex.items.length > 0){
				var ontology = ontologyIndex.items[0];
				var ontologyTerms = searchByQuery(ontology, query);
				console.log(ontologyTerms);
				$.each(ontologyTerms, function(index, term){
					
				})
			}
		}
		function getParentNodes(ontologyTerms){
			
		}
		
		function searchByQuery(ontology, query){
			var ontologyTermResult = restApi.get('/api/v1/' + ontology[ONTOLOGY_LABEL], {'expand' : ['attributes'], 'q' : {
				'q' : [{
					'field' : SYNONYMS,
					'operator' : 'EQUALS',
					'value' : query
				}]
			}}, null);
			return ontologyTermResult.items;
		}
	};
	
	
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
				else if(value[ONTOLOGY_LABEL]){
					value[TREE_LABEL] = value[ONTOLOGY_LABEL];
				}
				uniqueNodes.push(value);
			});
		}
		return uniqueNodes;
	}
	
	function getOntologyTermByIri(ontologyIRI){
		var request = {
			'q' : [{
				'field' : ONTOLOGY_IRI,
				'operator' : 'EQUALS',
				'value' : ontologyIRI
			}]
		};
		return restApi.get("/api/v1/ontologyindex/", {'q' : request}, null);
	}
	
	function getRootOntologyTerms(ontology){
		var rootOntologyTerms = restApi.get('/api/v1/' + ontology[ONTOLOGY_LABEL], {'expand' : ['attributes'], 'q' : {
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
	
	function createEntityMetaTree(entityMetaData, attributes) {
		
		var container = $('#tree-container').css({
			'height' : '500px',
			'overflow' : 'auto'
		});
		container.tree({
			'entityMetaData' : entityMetaData,
			'selectedAttributes' : attributes,
			'onAttributesSelect' : function(selects) {
				console.log(selects);
			},
			'onAttributeClick' : function(attribute) {
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
					relatedOntologyTerms.items = removeDuplicate(relatedOntologyTerms.items);
					ontologyTermInfo(relatedOntologyTerms.items[0]);
				}
			},
			'lazyload' : function(data, createChildren){
				var href = data.node.data.attribute.href;
				var ontologyTerm = restApi.get(href, {'expand' : ['attributes']});
				data.result = createChildren(removeDuplicate(ontologyTerm.attributes.items));
			}
		});
	}
	
	function ontologyTermInfo(data){
		var table = $('<table />').addClass('table');
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
		$('#ontology-term-info').empty().append(table);
	}
}($, window.top.molgenis = window.top.molgenis || {}));