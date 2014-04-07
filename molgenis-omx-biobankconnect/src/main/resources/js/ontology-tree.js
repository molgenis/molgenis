(function($, molgenis) {
	"use strict";
	
	var restApi = new molgenis.RestClient();
	
	molgenis.OntologyTree = function OntologyTree(){};
	
	molgenis.OntologyTree.prototype.updateOntologyTree = function(ontologyUrl){
		var request = {
			'q' : [{
				'field' : 'url',
				'operator' : 'EQUALS',
				'value' : ontologyUrl
			}]
		};
		var ontologyIndex = restApi.get("/api/v1/ontologyindex/", {'expand' : ['children'], 'q' : request}, null);
		if(ontologyIndex.items.length > 0){
			var ontologyName = ontologyIndex.items[0].ontologyLabel;
			var childNodes = convertToFancyTreeNode(removeDuplicate(getRootOntologyTerms(ontologyName)));
			var topNode = {
				'name' : ontologyIndex.items[0].url,
				'label' : ontologyName,
				'fieldType' : childNodes.length > 0 ? 'COMPOUND' : 'string',
				'attributes' : childNodes
			};
			console.log(topNode);
			createEntityMetaTree(topNode, null);
		}
	};
	
	function getRootOntologyTerms(ontologyName){
		var rootOntologyTerms = restApi.get('/api/v1/' + ontologyName, {'expand' : ['children'], 'q' : {
			'q' : [{
				'field' : 'root',
				'operator' : 'EQUALS',
				'value' : true
			}]
		}}, null);
		return rootOntologyTerms.items;
	}
	
	function removeDuplicate(listOfTerms){
		var uniqueIds = [];
		var uniqueNodes = [];
		$.each(listOfTerms, function(index, eachTerm){
			if($.inArray(eachTerm.ontologyTermIRI, uniqueIds) === -1)
			{
				uniqueIds.push(eachTerm.ontologyTermIRI);
				uniqueNodes.push(eachTerm);
			}
		});
		return uniqueNodes;
	}
	
	function convertToFancyTreeNode(listOfTerms){
		var nodes = [];
		$.each(listOfTerms, function(index, eachTerm){
			nodes.push({
				'href' : eachTerm.href,
				'name' : eachTerm.ontologyTermIRI,
				'label' : eachTerm.ontologyTerm,
				'ontologyUrl' : eachTerm.ontologyIRI,
				'description' : eachTerm.definition,
				'fieldType' : eachTerm.isLast ? 'string' : 'COMPOUND',
				'attributes' : []
			});
		});
		return nodes;
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
				var ontologyTerm = restApi.get(attribute.href, {'expand' : ['children']}, null);
				var convertedNode = convertToFancyTreeNode([ontologyTerm]);
				ontologyTermInfo(convertedNode[0]);
			},
			'lazyload' : function(data, createChildren, doSelect){
				var href = data.node.data.attribute.href;
				var ontologyTerm = restApi.get(href, {'expand' : ['children']}, null);
				var childOntologyTerms = convertToFancyTreeNode(removeDuplicate(ontologyTerm.children.items));
				data.result = createChildren(childOntologyTerms, doSelect);
			}
		});
	}
	
	function ontologyTermInfo(data){
		var table = $('<table />').addClass('table');
		table.append('<tr><th>Ontology</th><td><a href="' + data.ontologyUrl + '" target="_blank">' + data.ontologyUrl + '</a></td></tr>');
		table.append('<tr><th>OntologyTerm</th><td><a href="' + data.name + '" target="_blank">' + data.name + '</a></td></tr>');
		table.append('<tr><th>Name</th><td>' + data.label + '</td></tr>');
		if(data.definition){
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