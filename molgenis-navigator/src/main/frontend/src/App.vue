<template>
    <div id="app">
      <router-view></router-view>
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
