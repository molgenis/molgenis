import * as utils from 'store/utils/utils'

describe('utilities', () => {
  it('should create an EntityType model used in the UI', () => {
    const editorEntityType = {
      'id': '1',
      'attributes': []
    }

    const actual = utils.toEntityType(editorEntityType)
    const expected = {
      'id': '1',
      'label': 'Label...',
      'i18nLabel': undefined,
      'description': 'Description...',
      'i18nDescription': undefined,
      'abstract0': undefined,
      'backend': undefined,
      'package0': undefined,
      'entityTypeParent': undefined,
      'attributes': [],
      'tags': undefined,
      'idAttribute': undefined,
      'labelAttribute': undefined,
      'lookupAttributes': undefined
    }

    expect(expected).to.deep.equal(actual)
  })

  it('should create an Attribute model used in the UI', () => {
    const attribute = {
      'id': '1'
    }

    const actual = utils.toAttribute(attribute)
    const expected = {
      'id': '1',
      'name': 'Name...',
      'type': undefined,
      'parent': undefined,
      'refEntityType': undefined,
      'mappedByEntityType': undefined,
      'orderBy': undefined,
      'expression': undefined,
      'nullable': undefined,
      'auto': undefined,
      'visible': undefined,
      'label': 'Label...',
      'i18nLabel': undefined,
      'description': 'Description...',
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
      'sequenceNumber': undefined
    }

    expect(expected).to.deep.equal(actual)
  })

  it('should swap the location of two objects in an array', () => {
    const array = [1, 2, 3, 4, 5]

    const actual = utils.swapArrayElements(array, 2, 3)
    const expected = [1, 2, 4, 3, 5]

    expect(expected).to.deep.equal(actual)
  })
})
