<template>
  <div class="row">
    <div class="col-12">
      <div class="error-container text-muted d-flex flex-column justify-content-center align-items-center">
        <div class="error-box p-5">
          <h4>Something went wrong</h4>
          <pre> {{error}} </pre>

          <router-link class="btn btn-lg btn-outline-secondary mt-2"
                       to="/">
            {{ 'questionnaire_back_to_questionnaire_list' | i18n }}
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
  .error-container {
    height: 80vh;
  }

  .error-box {
    border: solid 2px #0b0b0b;
    background-color: #fefffa;
  }
</style>

<script>
  export default {
    name: 'QuestionnaireError',
    props: ['error'],
    beforeRouteLeave () {
      this.$store.commit('SET_ERROR', '')
    }
  }
</script>
