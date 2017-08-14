<template>
      <div id="molgenis-one-click-importer">
        <one-click-importer></one-click-importer>
      </div>
</template>

<style>
  .temp-header {
    background-color: #000000;
    margin-bottom: 20px;
  }

  .temp-header  a {
      color: #ffffff;
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
