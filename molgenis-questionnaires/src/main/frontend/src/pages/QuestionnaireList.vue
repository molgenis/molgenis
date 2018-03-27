<template>
  <div class="container">
    <div class="row">
      <div class="col-md-12">
        <div v-if="error" class="alert alert-warning" role="alert">{{ error }}</div>

        <!-- Loading spinner -->
        <template v-if="loading">
          <loading-spinner :message="$t('questionnaire_loading_list')"></loading-spinner>
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
    components: {
      LoadingSpinner,
      QuestionnaireTable
    }
  }
</script>
