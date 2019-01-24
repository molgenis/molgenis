// @flow
<template>
  <div class="container">
    <Alerts/>
    <Jobs/>
    <NavigatorSearch class="navigator-search"/>
    <NavigatorBreadcrumb />
    <NavigatorActions class="navigator-actions"/>
    <NavigatorTable/>
  </div>
</template>

<script>
import _ from 'lodash'
import { mapGetters } from 'vuex'
import { FETCH_RESOURCES_BY_FOLDER, FETCH_RESOURCES_BY_QUERY } from '../store/actions'
import Alerts from './Alerts'
import Jobs from './Jobs'
import NavigatorSearch from './NavigatorSearch'
import NavigatorBreadcrumb from './NavigatorBreadcrumb'
import NavigatorActions from './NavigatorActions'
import NavigatorTable from './NavigatorTable'

export default {
  name: 'Navigator',
  components: {Alerts, Jobs, NavigatorSearch, NavigatorBreadcrumb, NavigatorActions, NavigatorTable},
  computed: {
    ...mapGetters(['query', 'folderId'])
  },
  watch: {
    '$route' (to, from) {
      if (to.query && (to.query.q !== from.query.q)) {
        if (to.query.q) {
          _.debounce(this.fetchResourcesByQuery, 100)()
        } else {
          this.fetchResourcesByPackage()
        }
      } else if (to.params.folderId !== from.params.folderId) {
        this.fetchResourcesByPackage()
      }
    }
  },
  mounted: function () {
    if (this.query) {
      this.fetchResourcesByQuery()
    } else {
      this.fetchResourcesByPackage()
    }
  },
  methods: {
    fetchResourcesByQuery: function () {
      this.$store.dispatch(FETCH_RESOURCES_BY_QUERY, this.query)
    },
    fetchResourcesByPackage: function () {
      this.$store.dispatch(FETCH_RESOURCES_BY_FOLDER, this.folderId)
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

  .job-alerts {
    left: 1rem;
    right: 1rem;
    bottom: 60px; /* default footer height */
  }

  @media (min-width: 1200px) {
    .job-alerts {
      left: 75%;
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

  .btn-tooltip-wrapper {
    display: inline;
    padding: 0.375rem 0rem;
  }

  .fa-disabled {
    opacity: 0.6;
  }
</style>
