<template>

  <div class="container-fluid">
    <div class="row temp-header">
      <div class="col">
        <a class="btn pull-left" href="/">
          <i class="fa fa-chevron-left" aria-hidden="true"></i> back to home
        </a>
      </div>
    </div>

    <h1 style="padding-top: 1rem; text-align: center">Navigator</h1>

    <div id="app">
      <router-view></router-view>
    </div>

  </div>
</template>

<style>

  .temp-header {
    background-color: #000000;

  }

  .temp-header a {
    color: #ffffff;
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
  export default {
    name: 'molgenis-navigator'
  }
</script>
