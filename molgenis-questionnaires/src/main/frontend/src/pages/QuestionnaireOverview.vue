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
            <button @click="printOverView" type="button" class="btn btn-primary"><i class="fa fa-download" aria-hidden="true"></i> {{ 'questionnaire_overview_download_btn_label' | i18n }}</button>
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
      printOverView () {
        const translations = {
          trueLabel: this.$t('questionnaire_boolean_true'),
          falseLabel: this.$t('questionnaire_boolean_false')
        }
        const overView = questionnaireService.buildOverViewObject(this.$store.state.questionnaire, translations)
        const reportData = this.$store.state.reportData
        const pfdContent = questionnaireService.buildPdfContent(overView, reportData)
        questionnaireService.printContent(overView.title, pfdContent)
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
