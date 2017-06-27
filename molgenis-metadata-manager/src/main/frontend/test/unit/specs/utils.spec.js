import * as utils from 'store/utils/utils'

describe('utilities', () => {
  describe('toEntityType', () => {
    it('should create an EntityType model used in the UI', () => {
      const editorEntityType = {
        'id': '1',
        'attributes': []
      }

      const actual = utils.toEntityType(editorEntityType)
      const expected = {
        'id': '1',
        'label': 'add_a_label',
        'i18nLabel': undefined,
        'description': 'add_a_description',
        'i18nDescription': undefined,
        'abstract0': undefined,
        'backend': undefined,
        'package0': undefined,
        'entityTypeParent': undefined,
        'attributes': [],
        'tags': undefined,
        'idAttribute': undefined,
        'labelAttribute': undefined,
        'lookupAttributes': undefined,
        'isNew': false
      }

      expect(expected).to.deep.equal(actual)
    })
  })

  describe('toAttribute', () => {
    it('should create an Attribute model used in the UI', () => {
      const attribute = {
        'id': '1'
      }

      const actual = utils.toAttribute(attribute)
      const expected = {
        'id': '1',
        'name': 'add_a_unique_name',
        'type': undefined,
        'parent': undefined,
        'refEntityType': undefined,
        'mappedByEntityType': undefined,
        'orderBy': undefined,
        'expression': undefined,
        'nullable': undefined,
        'auto': undefined,
        'visible': undefined,
        'label': 'add_a_label',
        'i18nLabel': undefined,
        'description': 'add_a_description',
        'i18nDescription': undefined,
        'aggregatable': undefined,
        'enumOptions': undefined,
        'rangeMin': undefined,
        'rangeMax': undefined,
        'readonly': undefined,
        'unique': undefined,
        'tags': undefined,
        'visibleExpression': undefined,
        'validationExpression': undefined,
        'defaultValue': undefined,
        'sequenceNumber': undefined,
        'isNew': false
      }

      expect(expected).to.deep.equal(actual)
    })
  })

  describe('swapArrayElements', () => {
    it('should swap the location of two objects in an array', () => {
      const array = [1, 2, 3, 4, 5]

      const actual = utils.swapArrayElements(array, 2, 3)
      const expected = [1, 2, 4, 3, 5]

      expect(expected).to.deep.equal(actual)
    })
  })
})
