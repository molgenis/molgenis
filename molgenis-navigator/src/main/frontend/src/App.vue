<template>
  <div class="container">
    <a class="btn" href="/">
      <i class="fa fa-chevron-left" aria-hidden="true"></i> back to home
    </a>
    <h1 style="padding-top: 1rem; text-align: center">Navigator</h1>

    <div id="app">
      <router-view></router-view>
    </div>
  </div>
</template>

<style>
  #app {
    font-family: 'Avenir', Helvetica, Arial, sans-serif;
    -webkit-font-smoothing: antialiased;
    -moz-osx-font-smoothing: grayscale;
    text-align: center;
    color: #2c3e50;
  }
</style>

<script>

  export default {
    name: 'molgenis-app'
  }
</script>
