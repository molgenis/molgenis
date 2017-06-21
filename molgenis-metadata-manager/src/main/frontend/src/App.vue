<template>
  <div class="container-fluid">
    <div class="row temp-header">
      <div class="col">
        <a class="btn pull-left" href="/">
          <i class="fa fa-chevron-left" aria-hidden="true"></i> back to home
        </a>
      </div>
    </div>
    <div class="row">
      <div id="app" class="col">
        <hr>
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<style>
  .temp-header {
    background-color: #0c0d0e;
  }

  .temp-header a {
    color: white;
  }

</style>

<script>
  export default {
    name: 'molgenis-metadata-manager'
  }
</script>
