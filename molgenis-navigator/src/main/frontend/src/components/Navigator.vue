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
  import { mapGetters } from 'vuex'
  import { FETCH_ITEMS_BY_PACKAGE, FETCH_ITEMS_BY_QUERY } from '../store/actions'
  import Alert from './Alert'
  import NavigatorSearch from './NavigatorSearch'
  import NavigatorBreadcrumb from './NavigatorBreadcrumb'
  import NavigatorActions from './NavigatorActions'
  import NavigatorTable from './NavigatorTable'

  export default {
    name: 'Navigator',
    components: {Alert, NavigatorSearch, NavigatorBreadcrumb, NavigatorActions, NavigatorTable},
    computed: {
      ...mapGetters(['query', 'packageId'])
    },
    methods: {
      fetchItemsByQuery: function () {
        this.$store.dispatch(FETCH_ITEMS_BY_QUERY, this.query)
      },
      fetchItemsByPackage: function () {
        this.$store.dispatch(FETCH_ITEMS_BY_PACKAGE, this.packageId)
      }
    },
    mounted: function () {
      if (this.query) {
        this.fetchItemsByQuery()
      } else {
        this.fetchItemsByPackage()
      }
    },
    watch: {
      '$route' (to, from) {
        console.log(from.query, to.query)
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

  .fa-disabled {
    opacity: 0.6;
  }
</style>
