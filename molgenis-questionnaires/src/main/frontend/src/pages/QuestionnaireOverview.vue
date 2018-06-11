<template>
  <div class="container">
    <div class="row">
      <div class="col-12">
        <div v-if="error" class="alert alert-warning" role="alert">{{ error }}</div>

        <!-- Loading spinner -->
        <template v-if="loading">
          <loading-spinner :message="$t('questionnaire_overview_loading_text')"></loading-spinner>
        </template>

        <!-- Questionnaire overview -->
        <template v-else>
          <div id="pdf-container">
            <h1 class="display-3">{{ 'questionnaires_overview_title' | i18n }}</h1>
            <button @click="testBuild" type="button" class="btn btn-primary">Download</button>
            <hr>

            <questionnaire-overview-entry
              :attributes="getQuestionnaireFields()"
              :data="getQuestionnaireData()">
            </questionnaire-overview-entry>
          </div>
        </template>

      </div>
    </div>
  </div>
</template>

<script>
  import LoadingSpinner from '../components/LoadingSpinner'
  import QuestionnaireOverviewEntry from '../components/QuestionnaireOverviewEntry'
  import questionnaireService from '../services/questionnaireService'
  import * as JSPDF from 'jspdf'
  import type { OverViewAnswer, OverViewSection } from '../flow.types'

  export default {
    name: 'QuestionnaireOverview',
    props: ['questionnaireId'],
    methods: {
      getQuestionnaireFields () {
        return this.$store.state.questionnaire.meta.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')
      },
      getQuestionnaireData () {
        return this.$store.state.questionnaire.items[0]
      },
      testBuild () {
        const translations = {
          trueLabel: this.$t('questionnaire_boolean_true'),
          falseLabel: this.$t('questionnaire_boolean_false')
        }
        const overView = questionnaireService.buildOverViewObject(this.$store.state.questionnaire, translations)
        console.log(overView)

        const chapterHeadingFontSize = 16
        const sectionHeadingFontSize = 14
        const answerFontSize = 12
        const doc = new JSPDF()
        doc.setFont('helvetica', 'neue')
        let offset = 15

        const cleanString = function (input) {
          let output = ''
          for (let i = 0; i < input.length; i++) {
            if (input.charCodeAt(i) <= 127) {
              output += input.charAt(i)
            }
          }
          return output
        }

        const printQuestionAndAnswer = (question: OverViewAnswer) => {
          doc.setFontSize(answerFontSize)
          const cleaned = cleanString(question.questionLabel)
          doc.text(cleaned, 20, offset)
          offset += 10
          if (question.answerLabel === undefined) {
            doc.setFontType('italic')
            doc.text('empty', 20, offset)
            doc.setFontType('normal')
          } else {
            doc.setFontType('italic')
            doc.text(question.answerLabel, 20, offset)
            doc.setFontType('normal')
          }

          offset += 15
        }

        const printSection = (section: OverViewAnswer | OverViewSection) => {
          if (section.hasOwnProperty('title')) {
            doc.setFontSize(sectionHeadingFontSize)
            doc.text(section.title, 20, offset)
            offset += 20
            section.chapterSections.forEach(printSection)
          } else {
            printQuestionAndAnswer(section)
          }
        }

        const printChapter = (chapter) => {
          offset = 15
          doc.setFontSize(chapterHeadingFontSize)
          doc.text(chapter.title, 15, offset)
          offset += 20

          chapter.chapterSections.forEach(printSection)
        }

        overView.chapters.forEach((chapter, index) => {
          printChapter(chapter)
          if (index < overView.chapters.length - 1) {
            doc.addPage()
          }
        })

        const fileName = this.questionnaireLabel + '.pdf'
        doc.save(fileName)
      }
    },
    computed: {
      loading () {
        return this.$store.state.loading
      },
      error () {
        return this.$store.state.error
      }
    },
    created () {
      this.$store.dispatch('GET_QUESTIONNAIRE_OVERVIEW', this.questionnaireId)
    },
    components: {
      LoadingSpinner,
      QuestionnaireOverviewEntry
    }
  }
</script>
