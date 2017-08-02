<template>
  <div>
    <div class="container-fluid">
      <div class="row temp-header">
        <div class="col">
          <a class="btn pull-left" href="/">
            <i class="fa fa-chevron-left" aria-hidden="true"></i> back to home
          </a>
        </div>
      </div>
    </div>

    <div class="container">
      <div id="molgenis-one-click-importer">
        <one-click-importer></one-click-importer>
      </div>
    </div>
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
