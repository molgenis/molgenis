// @flow
<template>
  <div class="container">
    <Alerts/>
    <NavigatorSearch class="navigator-search"/>
    <NavigatorBreadcrumb />
    <NavigatorActions class="navigator-actions"/>
    <NavigatorTable/>
  </div>
</template>

<script>
import _ from 'lodash'
import { mapGetters } from 'vuex'
import { FETCH_ITEMS_BY_PACKAGE, FETCH_ITEMS_BY_QUERY } from '../store/actions'
import Alerts from './Alerts'
import NavigatorSearch from './NavigatorSearch'
import NavigatorBreadcrumb from './NavigatorBreadcrumb'
import NavigatorActions from './NavigatorActions'
import NavigatorTable from './NavigatorTable'

export default {
  name: 'Navigator',
  components: {Alerts, NavigatorSearch, NavigatorBreadcrumb, NavigatorActions, NavigatorTable},
  computed: {
    ...mapGetters(['query', 'packageId'])
  },
  watch: {
    '$route' (to, from) {
      if (to.query && (to.query.q !== from.query.q)) {
        if (to.query.q) {
          _.debounce(this.fetchItemsByQuery, 100)()
        } else {
          this.fetchItemsByPackage()
        }
      } else if (to.params.package !== from.params.package) {
        this.fetchItemsByPackage()
      }
    }
  },
  mounted: function () {
    if (this.query) {
      this.fetchItemsByQuery()
    } else {
      this.fetchItemsByPackage()
    }
  },
  methods: {
    fetchItemsByQuery: function () {
      this.$store.dispatch(FETCH_ITEMS_BY_QUERY, this.query)
    },
    fetchItemsByPackage: function () {
      this.$store.dispatch(FETCH_ITEMS_BY_PACKAGE, this.packageId)
    }
  }
}
</script>

<style>
  @media (min-width: 1200px) {
    .container {
      width: 1199px;
    }
  }

  .navigator-search {
    margin-bottom: 1rem;
  }

  .navigator-actions {
    margin-bottom: 2rem;
  }

  .btn.disabled {
    cursor: not-allowed !important;
  }

  .fa-disabled {
    opacity: 0.6;
  }
</style>
