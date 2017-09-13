<template>
  <div class="container">
    <div id="app">
      <router-view></router-view>
    </div>
  </div>
</template>

<style>
  #app {
    background-color: white;
  }
</style>

<script>
  export default {
    name: 'molgenis-app'
  }
</script>
