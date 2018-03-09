<template>
  <div class="container">
    <div class="row">
      <div class="col-md-12">

        <!-- Loading spinner -->
        <template v-if="loading">
          <loading-spinner :message="$t('questionnaire_loading_list')"></loading-spinner>
        </template>

        <!-- Error handler -->
        <template v-else-if="!loading && error">
          <questionnaire-error :error="error"></questionnaire-error>
        </template>

        <!-- Questionnaire table -->
        <template v-else>
          <questionnaire-table></questionnaire-table>
        </template>
      </div>
    </div>
  </div>
</template>

<script>
  import LoadingSpinner from '../components/LoadingSpinner'
  import QuestionnaireError from '../components/QuestionnaireError'
  import QuestionnaireTable from '../components/QuestionnaireTable'

  export default {
    name: 'QuestionnaireList',
    computed: {
      loading () {
        return this.$store.state.loading
      },
      error () {
        return this.$store.state.error
      }
    },
    created () {
      this.$store.dispatch('GET_QUESTIONNAIRE_LIST')
    },
    beforeRouteLeave (next) {
      this.$store.commit('SET_ERROR', '')
      next()
    },
    components: {
      LoadingSpinner,
      QuestionnaireError,
      QuestionnaireTable
    }
  }
</script>
