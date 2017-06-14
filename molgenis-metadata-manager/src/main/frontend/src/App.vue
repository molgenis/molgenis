<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col">
        <a class="btn pull-left" href="/">
          <i class="fa fa-chevron-left" aria-hidden="true"></i> back to home
        </a>
      </div>
    </div>
    <div class="row">
      <div id="app" class="col">
        <router-view></router-view>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'molgenis-app'
  }
</script>
