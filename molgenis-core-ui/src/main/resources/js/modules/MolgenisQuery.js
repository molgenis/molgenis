define(function(require, exports, module) {
	/**
	 * This module can be used to query the MOLGENIS backend
	 * 
	 * @module MolgenisQuery
	 */

	'use strict';
	var $ = require('jquery');

	/**
	 * Returns all atomic attributes. In case of compound attributes (attributes
	 * consisting of multiple atomic attributes) only the descendant atomic
	 * attributes are returned. The compound attribute itself is not returned.
	 * 
	 * @param attributes
	 * @param restClient
	 * 
	 * @return An array of atomicAttributes
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.prototype.getAtomicAttributes = function(attributes, restClient) {
		var atomicAttributes = [];
		function createAtomicAttributesRec(attributes) {
			$.each(attributes, function(i, attribute) {
				if (attribute.fieldType === 'COMPOUND') {
					// FIXME improve performance by retrieving async
					attribute = restClient.get(attribute.href, {
						'expand' : [ 'attributes' ]
					});
					createAtomicAttributesRec(attribute.attributes);
				} else {
					atomicAttributes.push(attribute);
				}
			});
		}

		createAtomicAttributesRec(attributes);
		return atomicAttributes;
	};

	/**
	 * Returns all compound attributes. In case of compound attributes
	 * (attributes consisting of multiple atomic attributes) only the descendant
	 * atomic attributes are returned. The compound attribute itself is not
	 * returned.
	 * 
	 * @param attributes
	 * @param restClient
	 * 
	 * @return an array of compound attributes
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.getCompoundAttributes = function(attributes, restClient) {
		var compoundAttributes = [];
		function createAtomicAttributesRec(attributes) {
			$.each(attributes, function(i, attribute) {
				if (attribute.fieldType === 'COMPOUND') {
					// FIXME improve performance by retrieving async
					attribute = restClient.get(attribute.href, {
						'expand' : [ 'attributes' ]
					});
					compoundAttributes.push(attribute);
					createAtomicAttributesRec(attribute.attributes);
				}
			});
		}
		createAtomicAttributesRec(attributes);
		return compoundAttributes;
	};

	/**
	 * Uses a restClient to retrieve the metadata for all the gives attributes
	 * 
	 * @param attributes
	 * @param restClient
	 * 
	 * @return a tree of attributes
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.getAllAttributes = function(attributes, restClient) {
		var tree = [];
		function createAttributesRec(attributes) {
			$.each(attributes, function(i, attribute) {
				tree.push(attribute);
				if (attribute.fieldType === 'COMPOUND') {
					// FIXME improve performance by retrieving async
					attribute = restClient.get(attribute.href, {
						'expand' : [ 'attributes' ]
					});
					createAttributesRec(attribute.attributes);
				}
			});
		}
		createAttributesRec(attributes);
		return tree;
	};

	/**
	 * Returns the label for the given attribute
	 * 
	 * @param attribute
	 * @return attribute label
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.getAttributeLabel = function(attribute) {
		var label = attribute.label || attribute.name;
		if (attribute.parent) {
			var parentLabel = attribute.parent.label || attribute.parent.name;
			label = parentLabel + '.' + label;
		}

		return label;
	};

	/**
	 * Checks if the user has write permission on a particular entity
	 * 
	 * @param entityName
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.hasWritePermission = function(entityName) {
		var writable = false;

		$.ajax({
			url : '/permission/' + entityName + "/write",
			dataType : 'json',
			async : false,
			success : function(result) {
				writable = result;
			}
		});

		return writable;
	};

	/**
	 * Checks if the given attribute is a reference
	 * 
	 * @param attribute
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.isRefAttr = function(attribute) {
		switch (attribute.fieldType) {
		case 'CATEGORICAL':
		case 'CATEGORICAL_MREF':
		case 'MREF':
		case 'XREF':
		case 'FILE':
			return true;
		default:
			return false;
		}
	};

	/**
	 * Checks if the given attribute is of type categorical, xref, or file
	 * 
	 * @param attribute
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.isXrefAttr = function(attribute) {
		return attribute.fieldType === 'CATEGORICAL' || attribute.fieldType === 'XREF' || attribute.fieldType === 'FILE';
	};

	/**
	 * Checks if the given attribute is of type Categorical_mref or mref
	 * 
	 * @param attribute
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.isMrefAttr = function(attribute) {
		return attribute.fieldType === 'CATEGORICAL_MREF' || attribute.fieldType === 'MREF';
	};

	/**
	 * Checks if the given attribute is of type compound
	 * 
	 * @param attribute
	 * 
	 * @memberOf MolgenisQuery
	 */
	exports.prototype.isCompoundAttr = function(attribute) {
		return attribute.fieldType === 'COMPOUND';
	};
});