<template>
  <div class="container">
    <div id="app">
      <router-view></router-view>
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";

  #app {
    background-color: $red;
  }
</style>

<script>
  export default {
    name: 'molgenis-app'
  }
</script>
