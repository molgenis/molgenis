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

<style lang="scss">
  @import "~variables";

  .temp-header {
    background-color: $black;
    a {
      color: $white;
    }

    margin-bottom: 20px;
  }

  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
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
