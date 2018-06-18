// @flow
import {
  OverView,
  OverViewAnswer,
  OverViewChapter,
  OverViewSection,
  ResponseMetaAttribute,
  QuestionnaireEntityResponse,
  Translation,
  PdfSection,
  EntityFieldType
} from '../flow.types'
// $FlowFixMe
import pdfMake from 'pdfmake/build/pdfmake'
// $FlowFixMe
import pdfFonts from 'pdfmake/build/vfs_fonts'

const compoundFields = (compound) => {
  return compound.attributes.reduce((accum, attribute: ResponseMetaAttribute) => {
    if (attribute.fieldType !== 'COMPOUND') {
      accum[attribute.name] = getEmptyAnswerSlot(attribute.fieldType)
    } else {
      accum = {...accum, ...compoundFields(attribute)}
    }
    return accum
  }, {})
}
const getEmptyAnswerSlot = (fieldType: EntityFieldType): [] | null => {
  switch (fieldType) {
    case 'MREF':
    case 'CATEGORICAL_MREF':
      return []
    default:
      return null
  }
}

export default {
  /**
   * Build a object to hold the questionnaire answers in, based on the questionnaire's metadata structure
   * @param questionnaireResp
   * @returns Object key values map with question ids as key and empty values
   */
  buildFormDataObject: function (questionnaireResp: QuestionnaireEntityResponse) {
    return questionnaireResp.meta.attributes
      .reduce((accum, attribute: ResponseMetaAttribute) => {
        if (!['id', 'owner', 'submitDate', 'status'].includes(attribute.name)){

          if (attribute.fieldType !== 'COMPOUND') {
            accum[attribute.name] = getEmptyAnswerSlot(attribute.fieldType)
          } else
          {
            accum = {...accum, ...compoundFields(attribute)}
          }
        }
        return accum

      }, {})
  },

  /**
   * Takes a filled in questionnaire entity response and transforms it to a OverView object containing the
   * questions and answers ordered into chapters, sections and subsections
   * @param questionnaireResp QuestionnaireEntityResponse
   * @param translations Objects containing local spesific translations for "True" and "False"
   * @returns OverView
   */
  buildOverViewObject: function (questionnaireResp: QuestionnaireEntityResponse, translations: Translation): OverView {
    const answers = questionnaireResp.items[0]

    const buildAnswerLabel = function (attribute: ResponseMetaAttribute): ?string {
      let answerLabel
      if (answers.hasOwnProperty(attribute.name)) {
        const questionId = attribute.name
        const answerType = attribute.fieldType
        const refLabelAttribute = attribute.refEntity ? attribute.refEntity.labelAttribute : undefined
        const answer = answers[questionId]
        switch (answerType) {
          case 'MREF':
          case 'CATEGORICAL_MREF':
            answerLabel = answer.map(a => a[refLabelAttribute]).join(', ')
            break
          case 'BOOL':
            answerLabel = answer ? translations.trueLabel : translations.falseLabel
            break
          case 'ENUM':
            answerLabel = answer.join(', ')
            break
          case 'XREF':
          case 'CATEGORICAL':
            answerLabel = answer[refLabelAttribute]
            break
          case 'INT':
          case 'DECIMAL':
          case 'LONG':
            answerLabel = answer.toString()
            break
          default:
            answerLabel = answer
        }
      }
      return answerLabel !== '' ? answerLabel : undefined
    }

    const buildChapterSection = function (sections: Array<OverViewAnswer | OverViewSection>, attribute: ResponseMetaAttribute): OverViewAnswer | OverViewSection {
      if (attribute.fieldType === 'COMPOUND') {
        const subSections = attribute.attributes.reduce(buildChapterSection, [])
        if (subSections.length > 0) {
          sections.push({
            title: attribute.label,
            chapterSections: subSections
          })
        }
      } else {
        const answerLabel = buildAnswerLabel(attribute)
        if (answerLabel !== undefined) {
          sections.push({
            questionId: attribute.name,
            questionLabel: attribute.label,
            answerLabel: answerLabel
          })
        }
      }

      return sections
    }

    const chapters = questionnaireResp.meta.attributes.reduce((chapters: Array<OverViewChapter>, attribute: ResponseMetaAttribute): OverView => {
      if (attribute.fieldType === 'COMPOUND') {
        const chapterSections = attribute.attributes.reduce(buildChapterSection, [])
        if (chapterSections.length > 0) {
          chapters.push({
            id: attribute.name,
            title: attribute.label,
            chapterSections: chapterSections
          })
        }
      }
      return chapters
    }, [])

    return {
      title: questionnaireResp.meta.label,
      chapters
    }
  },

  buildPdfContent: function (questionnaire: OverView): Array<PdfSection> {
    let content = []

    const printQuestionAndAnswer = (question: OverViewAnswer) => {
      content.push({
        text: question.questionLabel, style: 'questionLabel'
      })
      content.push({
        text: question.answerLabel, style: 'answerLabel'
      })
    }

    const printSection = (section: OverViewAnswer | OverViewSection) => {
      if (section.hasOwnProperty('title')) {
        content.push({text: section.title, style: 'sectionTitle'})
        section.chapterSections.forEach(printSection)
      } else {
        printQuestionAndAnswer(section)
      }
    }

    const printChapter = (chapter) => {
      content.push({text: chapter.title, style: 'chapterTitle'})
    }

    questionnaire.chapters.forEach((chapter) => {
      printChapter(chapter)
      chapter.chapterSections.forEach(printSection)
    })

    return content
  },

  printContent: function (docTitle: string, content: Object) {
    let docDefinition = {
      info: {
        title: 'questionnaire-overview'
      },
      content,
      styles: {
        chapterTitle: {
          fontSize: 16,
          bold: true,
          margin: [0, 20, 0, 10]
        },
        sectionTitle: {
          fontSize: 14,
          margin: [0, 10, 0, 5]
        },
        questionLabel: {
          fontSize: 10,
          margin: [0, 5]
        },
        answerLabel: {
          fontSize: 10,
          italics: true,
          margin: [0, 0, 0, 10]
        }
      }
    }
    const {vfs} = pdfFonts.pdfMake
    pdfMake.vfs = vfs
    pdfMake.createPdf(docDefinition).download(docTitle)
  }
}
