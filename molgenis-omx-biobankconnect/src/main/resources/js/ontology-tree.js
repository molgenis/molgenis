(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	molgenis.OntologyTree = function OntologyTree(){};
	
	molgenis.OntologyTree.prototype.updateOntologyTree = function(ontologyUrl){
		var request = {
			'q' : [{
				'field' : 'name',
				'operator' : 'EQUALS',
				'value' : ontologyUrl
			}]
		};
		var ontologyIndex = restApi.get("/api/v1/ontologyindex/", {'expand' : ['attributes'], 'q' : request}, null);
		if(ontologyIndex.items.length > 0){
			var topNode = ontologyIndex.items[0];
			topNode.attributes = removeDuplicate(getRootOntologyTerms(topNode.label));
			createEntityMetaTree(topNode, null);
		}
	};
	
	function getRootOntologyTerms(ontologyName){
		var rootOntologyTerms = restApi.get('/api/v1/' + ontologyName, {'expand' : ['attributes'], 'q' : {
			'q' : [{
				'field' : 'root',
				'operator' : 'EQUALS',
				'value' : true
			}]
		}}, null);
		return rootOntologyTerms.items;
	}
	
	function removeDuplicate(listOfNodes){
		var uniqueNodes = [];
		if(listOfNodes.length > 0){
			
			var nodeMap = {};
			$.each(listOfNodes, function(index, eachNode){
				if(nodeMap[eachNode.name]){
					if(eachNode.ontologyTermSynonym !== eachNode.label){			
						var existingNode = nodeMap[eachNode.name];
						existingNode.synonyms.push(eachNode.ontologyTermSynonym);
						nodeMap[eachNode.name] = existingNode;
					}
				}else{
					eachNode.synonyms = [];
					if(eachNode.ontologyTermSynonym !== eachNode.label){
						eachNode.synonyms.push(eachNode.ontologyTermSynonym);
					}
					nodeMap[eachNode.name] = eachNode;
				}
			});
			
			$.map(nodeMap, function(value, key){
				uniqueNodes.push(value);
			});
		}
		return uniqueNodes;
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
						'field' : 'name',
						'operator' : 'EQUALS',
						'value' : ontologyTerm.name
					}]
				}}, null);
				if(relatedOntologyTerms.items.length > 0){
					relatedOntologyTerms.items = removeDuplicate(relatedOntologyTerms.items);
					ontologyTermInfo(relatedOntologyTerms.items[0]);
				}
			},
			'lazyload' : function(data, createChildren, doSelect){
				var href = data.node.data.attribute.href;
				var ontologyTerm = restApi.get(href, {'expand' : ['attributes']}, null);
				var childOntologyTerms = removeDuplicate(ontologyTerm.attributes.items);
				data.result = createChildren(childOntologyTerms, doSelect);
			}
		});
	}
	
	function ontologyTermInfo(data){
		var table = $('<table />').addClass('table');
		table.append('<tr><th>Ontology</th><td><a href="' + data.ontologyUrl + '" target="_blank">' + data.ontologyUrl + '</a></td></tr>');
		table.append('<tr><th>OntologyTerm</th><td><a href="' + data.name + '" target="_blank">' + data.name + '</a></td></tr>');
		table.append('<tr><th>Name</th><td>' + data.label + '</td></tr>');
		if(data.description){
			table.append('<tr><th>Definition</th><td>' + data.description + '</td></tr>');
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