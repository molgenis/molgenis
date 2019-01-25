<template>
  <div id="molgenis-site-menu">
    <CookieWall v-if="showCookieWall" cookieName="permissionforcookies" />
    <NavBar :molgenis-menu="molgenisMenu"></NavBar>
  </div>
</template>

<script>
import Vue from 'vue'
import NavBar from '../node_modules/@molgenis/molgenis-ui-context/src/components/NavBar.vue'
import CookieWall from '../node_modules/@molgenis/molgenis-ui-context/src/components/CookieWall'

export default Vue.extend({
  name: 'molgenis-site-menu',
  components: {
    NavBar, CookieWall
  },
  data () {
    return {
      molgenisMenu: window.molgenisMenu,
      showCookieWall: window.cookieWall
    }
  }
})

</script>
