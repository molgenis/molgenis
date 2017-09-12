<template>
      <div id="app" class="col app-container">
        <router-view></router-view>
      </div>
</template>

<style>

  .app-container {
    padding-top: 40px
  }

</style>

<script>
  export default {
    name: 'molgenis-metadata-manager'
  }
</script>
