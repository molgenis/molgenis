<template>
  <div id="pdf-container" v-if="questionnaire">
    <h1 class="display-3">{{ 'questionnaires_overview_title' | i18n }}</h1>
    <hr>

    <questionnaire-overview-entry :attributes="attributes" :data="data"/>

  </div>
</template>

<script>
  import api from '@molgenis/molgenis-api-client'

  import QuestionnaireOverviewEntry from './QuestionnaireOverviewEntry'

  export default {
    name: 'QuestionnaireOverview',
    props: {

      /**
       * The name of the questionnaire to load
       */
      questionnaireName: {
        type: String,
        required: true
      }
    },
    data () {
      return {
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
      api.get('/api/v2/' + this.questionnaireName).then(response => {
        this.questionnaire = response
      })
    },
    components: {
      QuestionnaireOverviewEntry
    }
  }
</script>
