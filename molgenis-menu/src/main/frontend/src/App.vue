<template>
  <div id="app">
    <NavBar></NavBar>
    <img alt="Vue logo" src="./assets/logo.png">
    <HelloWorld msg="Welcome to Your Vue.js + TypeScript App"/>
  </div>
</template>

<script lang="ts">
  /* eslint-disable */
import Vue from 'vue'
// $tsignore
import navBar from '../node_modules/molgenis-ui-context/dist/navBar.umd.js'
import HelloWorld from './components/HelloWorld.vue'

export default Vue.extend({
  name: 'app',
  components: {
    HelloWorld, navBar
  }
})

</script>

<style>
#app {
  font-family: 'Avenir', Helvetica, Arial, sans-serif;
  -webkit-font-smoothing: antialiased;
  -moz-osx-font-smoothing: grayscale;
  text-align: center;
  color: #2c3e50;
  margin-top: 60px;
}
</style>
