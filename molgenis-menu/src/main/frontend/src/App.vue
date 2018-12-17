<template>
  <div id="app">
    <NavBar></NavBar>
  </div>
</template>

<script lang="ts">
import Vue from 'vue'
import NavBar from '../node_modules/molgenis-ui-context/src/components/NavBar.vue'

export default Vue.extend({
  name: 'app',
  components: {
    NavBar
  }
})

</script>
