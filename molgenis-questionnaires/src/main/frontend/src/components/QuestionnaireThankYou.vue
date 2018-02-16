<template>
  <div class="row">
    <div class="col-md-12">
      <p v-html="submissionText"></p>
      <router-link to="/" class="btn btn-outline-secondary">
        {{ 'questionnaire_back_to_questionnaire_list' | i18n }}
      </router-link>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'QuestionnaireThankYou',
    props: ['questionnaireId'],
    computed: {
      submissionText () {
        return this.$store.state.submissionText
      }
    },
    created () {
      this.$store.dispatch('GET_SUBMISSION_TEXT', this.questionnaireId)
    }
  }
</script>
