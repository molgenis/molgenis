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
  import type { OverViewAnswer, OverViewSection } from '../flow.types'

  import LoadingSpinner from '../components/LoadingSpinner'
  import QuestionnaireOverviewEntry from '../components/QuestionnaireOverviewEntry'
  import questionnaireService from '../services/questionnaireService'
  import pdfMake from 'pdfmake/build/pdfmake'
  import pdfFonts from 'pdfmake/build/vfs_fonts'

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

        let content = []
        const styles = {
          chapterTitle: {
            fontSize: 16,
            bold: true,
            margin: [0, 20]
          },
          sectionTitle: {
            fontSize: 14,
            margin: [5, 10]
          },
          questionLabel: {
            fontSize: 10,
            margin: [0, 5]
          },
          answerLabel: {
            fontSize: 10,
            italics: true
          }
        }

        const printQuestionAndAnswer = (question: OverViewAnswer) => {
          content.push({
            text: question.questionLabel, style: 'questionLabel'
          })
          const answerLabel = question.answerLabel === undefined ? 'empty' : question.answerLabel
          content.push({
            text: answerLabel, style: 'answerLabel'
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

        overView.chapters.forEach((chapter, index) => {
          printChapter(chapter)
          chapter.chapterSections.forEach(printSection)
        })

        let docDefinition = {
          info: {
            title: 'questionnaire-overview'
          },
          content,
          styles
        }
        const {vfs} = pdfFonts.pdfMake
        pdfMake.vfs = vfs
        pdfMake.createPdf(docDefinition).download()
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
