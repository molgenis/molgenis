<template>
  <div id="app" class="one-click-importer-container">
    <one-click-importer></one-click-importer>
  </div>
</template>

<style>
  .one-click-importer-container {
    padding-top: 40px
  }
</style>

<script>
  import OneClickImporter from './components/OneClickImporter'

  export default {
    name: 'molgenis-one-click-importer',
    components: {
      OneClickImporter
    }
  }
</script>
