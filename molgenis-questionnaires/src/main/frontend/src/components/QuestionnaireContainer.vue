<template>
  <div class="container-fluid">
    <h5 class="display-4">{{questionnaireLabel}}</h5>
    <div class="row">
      <div class="col-12">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<style>
  .spinner-container {
    height: 80vh;
  }
</style>

<script>

  export default {
    name: 'QuestionnaireContainer',
    computed: {
      questionnaireLabel () {
        return this.$store.state.questionnaireLabel
      }
    }
  }
</script>
