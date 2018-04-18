module.exports = {
  meta: {
    name: 'questionnaire_1',
    label: 'Questionnaire #1',
    description: '<h3>This is the first mocked questionnaire response</h3><p>This is a not started questionnaire used for showing the chapters</p>',
    attributes: [
      {
        name: 'id',
        label: 'Identifier',
        description: 'The ID attribute',
        fieldType: 'STRING',
        nillable: false,
        readOnly: true,
        unique: true,
        visible: true
      },
      {
        name: 'label',
        label: 'Label',
        description: 'The Label attribute',
        fieldType: 'STRING',
        labelAttribute: true,
        nillable: true,
        readOnly: false,
        unique: false,
        visible: true
      },
      {
        name: 'status',
        label: 'Status',
        description: 'The status of the questionnaire',
        fieldType: 'STRING',
        visible: false
      },
      {
        name: 'submitDate',
        label: 'Submit date',
        description: 'The date of submission',
        fieldType: 'DATE',
        nullableExpression: '$("status").value() !== "SUBMITTED"',
        visibleExpression: '$("status").value() === "SUBMITTED"'
      },
      {
        name: 'chapter1',
        label: 'Chapter #1 - Personal information',
        description: 'Questions regarding your personal information',
        fieldType: 'COMPOUND',
        attributes: [
          {
            name: 'ch1_question1',
            label: 'Question #1 - What is your name?',
            description: 'Please fill in your name',
            fieldType: 'STRING',
            visible: true,
            nullableExpression: '$("status").value() !== "SUBMITTED"'
          },
          {
            name: 'ch1_question2',
            label: 'Question #2 - What is your age?',
            description: 'Please fill in your age',
            fieldType: 'INT',
            range: {
              min: 0,
              max: 99
            },
            visible: true,
            nullableExpression: '$("status").value() !== "SUBMITTED"'
          },
          {
            name: 'ch1_question3',
            label: 'Question #3 - This is not required until the end',
            description: 'Verify if we can navigate to next chapter with nullable expressions',
            fieldType: 'TEXT',
            visible: true,
            nullableExpression: '$("status").value() !== "SUBMITTED"'
          },
          {
            name: 'ch1_question4',
            label: 'Question #4 - What is your favorite boolean value ?',
            description: 'Please share',
            fieldType: 'BOOL',
            visible: true,
            nillable: true
          }
        ]
      },
      {
        name: 'chapter2',
        label: 'Chapter #2 - Professional questions',
        description: 'Questions regarding your occupation and skills',
        fieldType: 'COMPOUND',
        attributes: [
          {
            name: 'ch2_question1',
            label: 'Question #1 - Tag your skills',
            description: 'Please check all the skills you have mastered',
            fieldType: 'CATEGORICAL_MREF',
            visible: true,
            refEntity: {
              name: 'skills',
              label: 'Programming skills',
              attributes: [
                {
                  name: 'id',
                  label: 'Identifier',
                  description: 'The ID attribute',
                  fieldType: 'STRING',
                  nillable: false,
                  readOnly: true,
                  unique: true,
                  visible: true
                },
                {
                  name: 'label',
                  label: 'Label',
                  description: 'The Label attribute',
                  fieldType: 'STRING',
                  labelAttribute: true,
                  nillable: true,
                  readOnly: false,
                  unique: false,
                  visible: true
                }
              ],
              idAttribute: 'id',
              labelAttribute: 'label'
            },
            categoricalOptions: [
              {
                id: 'vue',
                label: 'VueJS'
              },
              {
                id: 'react',
                label: 'React'
              },
              {
                id: 'angular',
                label: 'Angular'
              },
              {
                id: 'javascript',
                label: 'JavaScript'
              },
              {
                id: 'java',
                label: 'Java'
              },
              {
                id: 'spring',
                label: 'Spring'
              }
            ]
          },
          {
            name: 'ch2_question2',
            label: 'Question #2 - What is your favorite website',
            description: 'Please check your favorite website',
            fieldType: 'CATEGORICAL',
            visible: true,
            refEntity: {
              name: 'fav_website',
              label: 'Favorite Websites',
              attributes: [
                {
                  name: 'id',
                  label: 'Identifier',
                  description: 'The ID attribute',
                  fieldType: 'STRING',
                  nillable: false,
                  readOnly: true,
                  unique: true,
                  visible: true
                },
                {
                  name: 'label',
                  label: 'Label',
                  description: 'The Label attribute',
                  fieldType: 'STRING',
                  labelAttribute: true,
                  nillable: true,
                  readOnly: false,
                  unique: false,
                  visible: true
                }
              ],
              idAttribute: 'id',
              labelAttribute: 'label'
            },
            categoricalOptions: [
              {
                id: '1',
                label: 'www.molgenis.org'
              },
              {
                id: '2',
                label: 'Other'
              }
            ]
          },
          {
            name: 'ch2_question3',
            label: 'Question #3 - Please specify this "other" you speak of',
            description: 'What is greater than www.molgenis.org?',
            fieldType: 'TEXT',
            visibleExpression: '$("ch2_question2").value() === "2"',
            nullableExpression: '$("ch2_question2").value() !== "2"'
          }
        ]
      }
    ],
    idAttribute: 'id',
    labelAttribute: 'label'
  },
  items: [
    {
      id: '1',
      label: '1',
      status: 'OPEN',
      submitDate: undefined,
      ch1_question1: undefined,
      ch1_question2: undefined,
      ch2_question1: undefined,
      ch2_question2: undefined,
    }
  ]
}
