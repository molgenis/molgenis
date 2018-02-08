<template>
  <div class="row">
    <div class="col-md-12">
      <p v-html="submissionText"></p>
    </div>
  </div>
</template>

<script>
  import api from '@molgenis/molgenis-api-client'

  export default {
    name: 'QuestionnaireThankYou',
    props: {

      /**
       * The name of the questionnaire
       */
      questionnaireName: {
        type: String,
        required: true
      }
    },
    data () {
      return {
        submissionText: null
      }
    },
    created () {
      api.get('/menu/plugins/questionnaires/' + this.questionnaireName + '/thanks').then(response => {
        this.submissionText = response
      }).catch(error => {
        console.log('Something went wrong fetching the questionnaire submission text', error)
      })
    }
  }
</script>
