<template>
  <div id="molgenis-site-menu">
    <NavBar :molgenis-menu="molgenisMenu"></NavBar>
  </div>
</template>

<script>
import Vue from 'vue'
import NavBar from '../node_modules/molgenis-ui-context/src/components/NavBar.vue'

export default Vue.extend({
  name: 'molgenis-site-menu',
  components: {
    NavBar
  },
  data () {
    return {
      molgenisMenu: window.molgenisMenu
    }
  }
})

</script>
