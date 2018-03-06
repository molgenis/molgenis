import QuestionnaireOverviewEntry from 'src/components/QuestionnaireOverviewEntry'
import { createLocalVue, shallow } from '@vue/test-utils'
import Vuex from 'vuex'

const $t = (key) => {
  const translations = {
    'questionnaire_overview_loading_text': 'loading overview',
    'questionnaires_overview_title': 'overview',
    'questionnaire_boolean_true': 'Ja',
    'questionnaire_boolean_false': 'Nee'
  }
  return translations[key]
}

describe('QuestionnaireOverviewEntry component', () => {
  let localVue

  beforeEach(() => {
    localVue = createLocalVue()
    localVue.use(Vuex)
    localVue.filter('i18n', $t)
  })

  it('should render 1 level deep entry correctly', () => {
    const propsData = {
      attributes: [
        {
          name: 'field1',
          label: 'Field 1',
          fieldType: 'STRING'
        },
        {
          name: 'field2',
          label: 'Field 2',
          fieldType: 'STRING'
        }
      ],
      data: {
        field1: 'value',
        field2: 'other value'
      }
    }

    const wrapper = shallow(QuestionnaireOverviewEntry, {propsData})
    const dls = wrapper.findAll('dl')

    expect(dls.length).to.equal(2)

    expect(dls.at(0).find('dt').text()).to.equal('Field 1')
    expect(dls.at(0).find('dd').text()).to.equal('value')

    expect(dls.at(1).find('dt').text()).to.equal('Field 2')
    expect(dls.at(1).find('dd').text()).to.equal('other value')
  })

  it('should render multiple levels correctly', () => {
    const propsData = {
      attributes: [
        {
          name: 'compound',
          label: 'Compound group',
          fieldType: 'COMPOUND',
          attributes: [
            {
              name: 'field1',
              label: 'Field 1',
              fieldType: 'STRING'
            },
            {
              name: 'field2',
              label: 'Field 2',
              fieldType: 'STRING'
            }
          ]
        }
      ],
      data: {
        field1: 'value',
        field2: 'other value'
      }
    }

    const wrapper = shallow(QuestionnaireOverviewEntry, {propsData})
    expect(wrapper.contains('div.pl-3')).to.equal(true)
    expect(wrapper.contains('hr')).to.equal(true)
  })

  describe('hasValue method', () => {
    const propsData = {
      attributes: [
        {
          name: 'field1',
          label: 'Field 1',
          fieldType: 'STRING'
        },
        {
          name: 'field2',
          label: 'Field 2',
          fieldType: 'STRING'
        },
        {
          name: 'field3',
          label: 'Field 3',
          fieldType: 'STRING'
        },
        {
          name: 'field4',
          label: 'Field 4',
          fieldType: 'STRING'
        }
      ],
      data: {
        field1: 'value',
        field2: undefined,
        field3: [],
        field4: ['other value']
      }
    }

    const wrapper = shallow(QuestionnaireOverviewEntry, {propsData})
    it('should return [TRUE] if there is a value', () => {
      expect(wrapper.vm.hasValue(propsData.attributes[0])).to.equal(true)
    })

    it('should return [TRUE] if there is a list with a length larger then 0', () => {
      expect(wrapper.vm.hasValue(propsData.attributes[3])).to.equal(true)
    })

    it('should return [FALSE] if value is undefined', () => {
      expect(wrapper.vm.hasValue(propsData.attributes[1])).to.equal(false)
    })

    it('should return [FALSE] if there is an empty list', () => {
      expect(wrapper.vm.hasValue(propsData.attributes[2])).to.equal(false)
    })
  })

  describe('hasAChildWithData', () => {
    it('should return false if compound does not have a child with a value', () => {
      const propsData = {
        attributes: [
          {
            name: 'compound',
            label: 'Compound group',
            fieldType: 'COMPOUND',
            attributes: [
              {
                name: 'field1',
                label: 'Field 1',
                fieldType: 'STRING'
              },
              {
                name: 'field2',
                label: 'Field 2',
                fieldType: 'STRING'
              }
            ]
          }
        ],
        data: {
          field1: undefined,
          field2: undefined
        }
      }

      const wrapper = shallow(QuestionnaireOverviewEntry, {propsData})
      expect(wrapper.vm.hasAChildWithData(propsData.attributes[0])).to.equal(false)
    })

    it('should return true if compound has one child with a value', () => {
      const propsData = {
        attributes: [
          {
            name: 'compound',
            label: 'Compound group',
            fieldType: 'COMPOUND',
            attributes: [
              {
                name: 'compound1',
                label: 'Compound 1',
                fieldType: 'COMPOUND',
                attributes: [
                  {
                    name: 'field1',
                    label: 'Field 1',
                    fieldType: 'STRING'
                  }
                ]
              },
              {
                name: 'field2',
                label: 'Field 2',
                fieldType: 'STRING'
              }
            ]
          }
        ],
        data: {
          field1: 'value',
          field2: undefined
        }
      }

      const wrapper = shallow(QuestionnaireOverviewEntry, {propsData})
      expect(wrapper.vm.hasAChildWithData(propsData.attributes[0])).to.equal(true)
    })
  })

  describe('getReadableValue method', () => {
    const propsData = {
      attributes: [
        {
          name: 'string',
          label: 'Field 1',
          fieldType: 'STRING'
        },
        {
          name: 'mref',
          label: 'Field 2',
          fieldType: 'MREF',
          refEntity: {
            labelAttribute: 'label'
          }
        },
        {
          name: 'cat_mref',
          label: 'Field 3',
          fieldType: 'CATEGORICAL_MREF',
          refEntity: {
            labelAttribute: 'label'
          }
        },
        {
          name: 'bool1',
          label: 'Field 4',
          fieldType: 'BOOL'
        },
        {
          name: 'enum',
          label: 'Field 5',
          fieldType: 'ENUM'
        },
        {
          name: 'categorical',
          label: 'Field 6',
          fieldType: 'CATEGORICAL',
          refEntity: {
            labelAttribute: 'label'
          }
        },
        {
          name: 'xref',
          label: 'Field 7',
          fieldType: 'XREF',
          refEntity: {
            labelAttribute: 'label'
          }
        },
        {
          name: 'bool2',
          label: 'Field 8',
          fieldType: 'BOOL'
        },
        {
          name: 'noValue',
          label: 'Field 9',
          fieldType: 'STRING'
        }
      ],
      data: {
        string: 'value',
        mref: [
          {
            id: 'idValue1',
            label: 'labelValue1'
          },
          {
            id: 'idValue2',
            label: 'labelValue2'
          }
        ],
        cat_mref: [
          {
            id: 'idValue1',
            label: 'labelValue1'
          }
        ],
        bool1: false,
        enum: ['enum1'],
        categorical: {
          id: 'idValue1',
          label: 'labelValue1'
        },
        xref: {
          id: 'idValue1',
          label: 'labelValue1'
        },
        bool2: true,
        noValue: undefined
      }
    }
    const mocks = {$t}

    const wrapper = shallow(QuestionnaireOverviewEntry, {propsData, mocks})

    it('should generate a readable value for [STRING] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[0])).to.equal('value')
    })

    it('should generate a readable value for [MREF] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[1])).to.equal('labelValue1, labelValue2')
    })

    it('should generate a readable value for [CATEGORICAL_MREF] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[2])).to.equal('labelValue1')
    })

    it('should generate a readable value for [BOOLEAN] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[3])).to.equal('Nee')
    })

    it('should generate a readable value for [ENUM] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[4])).to.equal('enum1')
    })

    it('should generate a readable value for [CATEGORICAL] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[5])).to.equal('labelValue1')
    })

    it('should generate a readable value for [XREF] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[6])).to.equal('labelValue1')
    })

    it('should generate a readable value for [BOOLEAN] attributes', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[7])).to.equal('Ja')
    })

    it('should generate an empty string if value is undefined', () => {
      expect(wrapper.vm.getReadableValue(propsData.attributes[8])).to.equal('')
    })
  })
})
