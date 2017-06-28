// @flow
import type { EditorEntityType, EditorAttribute } from './flow.types'

export const toEntityType = (editorEntityType: Object): EditorEntityType => {
  return {
    'id': editorEntityType.id,
    'label': editorEntityType.label ? editorEntityType.label : 'add_a_label',
    'i18nLabel': editorEntityType.i18nLabel,
    'description': editorEntityType.description ? editorEntityType.description : 'add_a_description',
    'i18nDescription': editorEntityType.i18nDescription,
    'abstract0': editorEntityType.abstract0,
    'backend': editorEntityType.backend,
    'package0': editorEntityType.package0,
    'entityTypeParent': editorEntityType.entityTypeParent,
    'attributes': editorEntityType.attributes.map(attribute => toAttribute(attribute)),
    'tags': editorEntityType.tags,
    'idAttribute': editorEntityType.idAttribute,
    'labelAttribute': editorEntityType.labelAttribute,
    'lookupAttributes': editorEntityType.lookupAttributes,
    'isNew': false
  }
}

export const toAttribute = (attribute: Object): EditorAttribute => {
  return {
    'id': attribute.id,
    'name': attribute.name ? attribute.name : 'add_a_unique_name',
    'type': attribute.type,
    'parent': attribute.parent,
    'refEntityType': attribute.refEntityType,
    'mappedByEntityType': attribute.mappedByEntityType,
    'orderBy': attribute.orderBy,
    'expression': attribute.expression,
    'nullable': attribute.nullable,
    'auto': attribute.auto,
    'visible': attribute.visible,
    'label': attribute.label ? attribute.label : 'add_a_label',
    'i18nLabel': attribute.i18nLabel,
    'description': attribute.description ? attribute.description : 'add_a_description',
    'i18nDescription': attribute.i18nDescription,
    'aggregatable': attribute.aggregatable,
    'enumOptions': attribute.enumOptions,
    'rangeMin': attribute.minRange,
    'rangeMax': attribute.maxRange,
    'readonly': attribute.readonly,
    'unique': attribute.unique,
    'tags': attribute.tags,
    'visibleExpression': attribute.visibleExpression,
    'validationExpression': attribute.validationExpression,
    'defaultValue': attribute.defaultValue,
    'sequenceNumber': attribute.sequenceNumber,
    'isNew': false
  }
}

/**
 * Swap the elements in an array at indexes originalIndex and targetIndex.
 *
 * @param (array) The array.
 * @param (originalIndex) The index of the first element to swap.
 * @param (targetIndex) The index of the second element to swap.
 * @return {Array} A new array with the elements swapped.
 */
export const swapArrayElements = (array: Array<EditorAttribute>, originalIndex: number, targetIndex: number) => {
  if (array.length === 1) return array
  array.splice(targetIndex, 1, array.splice(originalIndex, 1, array[targetIndex])[0])
  return array
}
