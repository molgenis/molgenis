<template>
  <div id="pdf-container">

    <template v-if="loading">
      <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
        <p>{{ 'questionnaire_overview_loading_text' | i18n }}</p>
      </div>
    </template>

    <template v-else>
      <h1 class="display-3">
        {{ 'questionnaires_overview_title' | i18n }}
      </h1>

      <hr>

      <questionnaire-overview-entry
        :attributes="attributes"
        :data="data">
      </questionnaire-overview-entry>
    </template>
  </div>
</template>

<script>
  import QuestionnaireOverviewEntry from './QuestionnaireOverviewEntry'

  export default {
    name: 'QuestionnaireOverview',
    props: ['questionnaireId'],
    data () {
      return {
        loading: true,
        questionnaire: null
      }
    },
    computed: {
      data () {
        return this.questionnaire.items[0]
      },
      attributes () {
        return this.questionnaire.meta.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')
      }
    },
    created () {
      this.$store.dispatch('GET_QUESTIONNAIRE_OVERVIEW', this.questionnaireId).then(response => {
        this.questionnaire = response
        this.loading = false
      })
    },
    components: {
      QuestionnaireOverviewEntry
    }
  }
</script>
