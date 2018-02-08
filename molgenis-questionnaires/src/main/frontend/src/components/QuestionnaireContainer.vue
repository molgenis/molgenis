<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-md-12">

        <router-link class="btn btn-outline-secondary my-3" to="/">
          <i class="fa fa-chevron-left"></i> {{ 'questionnaire_back_button' | i18n }}
        </router-link>

      </div>
    </div>

    <div class="row">
      <div class="col-12">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<script>
  import api from '@molgenis/molgenis-api-client'

  export default {
    name: 'QuestionnaireContainer',
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
        questionnaire: {}
      }
    },
    created () {
      api.get('/menu/plugins/questionnaires/' + this.questionnaireName).then(questionnaire => {
        this.questionnaire = questionnaire
      })
    }
  }
</script>
