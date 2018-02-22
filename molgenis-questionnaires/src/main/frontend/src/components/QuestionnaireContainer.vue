<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-12">
        <router-view/>
      </div>
    </div>
  </div>
</template>

<style>
  /* Centers the loading spinner */
  .spinner-container {
    height: 80vh;
  }
</style>

<script>
  export default {
    name: 'QuestionnaireContainer'
  }
</script>
