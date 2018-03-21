<template>
  <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
    <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
    <p>{{ message }}</p>
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
    name: 'LoadingSpinner',
    props: ['message']
  }
</script>
