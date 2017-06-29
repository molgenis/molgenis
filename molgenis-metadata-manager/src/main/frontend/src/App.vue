<template>
  <div class="container-fluid">
    <div class="row temp-header">
      <div class="col">
        <a class="btn pull-left" href="/">
          <i class="fa fa-chevron-left" aria-hidden="true"></i> {{ 'back-to-home-button' | i18n }}
        </a>
      </div>
    </div>
    <div class="row">
      <div id="app" class="col app-container">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";

  .temp-header {
    background-color: $black;
  }

  .temp-header a {
    color: $white;
  }

  .app-container {
    padding-top: 40px
  }

</style>

<script>
  export default {
    name: 'molgenis-metadata-manager'
  }
</script>
