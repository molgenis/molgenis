// @flow
<template>
  <div class="container">
    <Alert/>
    <NavigatorSearch class="navigator-search"/>
    <NavigatorBreadcrumb />
    <NavigatorActions class="navigator-actions"/>
    <NavigatorTable/>
  </div>
</template>

<script>
  import _ from 'lodash'
  import { FETCH_ITEMS_BY_PACKAGE, FETCH_ITEMS_BY_QUERY } from '../store/actions'
  import Alert from './Alert'
  import NavigatorSearch from './NavigatorSearch'
  import NavigatorBreadcrumb from './NavigatorBreadcrumb'
  import NavigatorActions from './NavigatorActions'
  import NavigatorTable from './NavigatorTable'

  export default {
    name: 'Navigator',
    components: {Alert, NavigatorSearch, NavigatorBreadcrumb, NavigatorActions, NavigatorTable},
    methods: {
      submitQuery: _.debounce(function (query) {
        this.$store.dispatch(FETCH_ITEMS_BY_QUERY, query)
      }, 200)
    },
    mounted: function () {
      if (this.$route.query.q) {
        this.$store.dispatch(FETCH_ITEMS_BY_QUERY, this.$route.query.q)
      } else {
        this.$store.dispatch(FETCH_ITEMS_BY_PACKAGE, this.$route.params.package)
      }
    },
    watch: {
      '$route' (to, from) {
        if (to.query && (to.query.q !== from.query.q)) {
          this.submitQuery(to.query.q)
        } else if (to.params.package !== from.params.package) {
          this.$store.dispatch(FETCH_ITEMS_BY_PACKAGE, to.params.package)
        }
      }
    }
  }
</script>

<style scoped>
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
</style>
