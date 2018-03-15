<template>
  <div class="container">
    <div class="row">
      <div class="col-md-12">
        <div v-if="error" class="alert alert-warning" role="alert">{{ error }}</div>

        <!-- Loading spinner -->
        <template v-if="loading">
          <loading-spinner :message="$t('questionnaire_loading_text')"></loading-spinner>
        </template>

        <!-- Submission text -->
        <template v-else>
          <p class="mt-3" v-html="submissionText"></p>
          <router-link to="/" class="btn btn-outline-secondary">
            {{ 'questionnaire_back_to_questionnaire_list' | i18n }}
          </router-link>
        </template>

      </div>
    </div>
  </div>
</template>

<script>
  import LoadingSpinner from '../components/LoadingSpinner'

  export default {
    name: 'QuestionnaireSubmitted',
    props: ['questionnaireId'],
    computed: {
      loading () {
        return this.$store.state.loading
      },
      error () {
        return this.$store.state.error
      },
      submissionText () {
        return this.$store.state.submissionText
      }
    },
    created () {
      this.$store.dispatch('GET_SUBMISSION_TEXT', this.questionnaireId)
    },
    components: {
      LoadingSpinner
    }
  }
</script>
