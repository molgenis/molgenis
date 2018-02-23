<template>
  <div>
    <!-- Loading spinner -->
    <template v-if="loading">
      <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
        <p>{{ 'questionnaire_loading_text' | i18n }}...</p>
      </div>
    </template>

    <!-- Questionnaire description + start button -->
    <template v-else>
      <div class="row">
        <div class="col-lg-8 col-md-12">
          <h5 class="display-4">{{ questionnaireLabel }}</h5>
          <p v-html="questionnaireDescription"></p>

          <router-link class="btn btn-lg btn-primary mt-2"
                       :to="'/' + questionnaireId + '/chapter/1'">
            {{ 'questionnaire_start' | i18n }}
          </router-link>

          <router-link class="btn btn-lg btn-outline-secondary mt-2"
                       to="/">
            {{ 'questionnaire_back_to_questionnaire_list' | i18n }}
          </router-link>
        </div>
      </div>
    </template>

  </div>
</template>

<script>
  export default {
    name: 'QuestionnaireStart',
    props: ['questionnaireId'],
    data () {
      return {
        loading: true
      }
    },
    computed: {

      /**
       * Retrieve the questionnaire description from the store
       * @return {string} Questionnaire description
       */
      questionnaireDescription () {
        return this.$store.state.questionnaireDescription
      },

      /**
       * Get the label of the questionnaire from the store
       * @return {string} The label of the questionnaire
       */
      questionnaireLabel () {
        return this.$store.state.questionnaireLabel
      }
    },
    created () {
      this.$store.dispatch('START_QUESTIONNAIRE', this.questionnaireId).then(() => {
        if (this.$store.state.chapterFields.length === 0) {
          this.$store.dispatch('GET_QUESTIONNAIRE', this.questionnaireId).then(() => {
            this.loading = false
          })
        } else {
          this.loading = false
        }
      })
    }
  }
</script>
