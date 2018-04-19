// @flow
<template>
  <div>
    <my-tour :firstPackage="items[0]" :homeUrl="homeUrl" :search="submitQuery"></my-tour>
    <div v-if="error != undefined" class="alert alert-danger" role="alert">
      <button @click="error=null" type="button" class="close"><span aria-hidden="true">&times;</span></button>
      {{error}}
    </div>

    <!-- Search element -->
    <div class="navigator-search row justify-content-center">
      <div class="input-group col-lg-6">
        <input id="navigator-search-input" v-model="query" type="text" class="form-control" :placeholder="$t('search-input-placeholder')">
        <div class="input-group-append">

          <button @click="submitQuery()" class="btn btn-outline-secondary" :disabled="!query"
                  type="button">{{ 'search-button' | i18n }}
          </button>

          <button @click="reset()" class="btn btn-outline-secondary" :disabled="!query"
                  type="button">{{ 'clear-button' | i18n }}
          </button>
        </div>
      </div>
    </div>

    <!-- Breadcrumb element -->
    <div class="navigator-path row">
      <div class="col input-group">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <a :href="homeUrl" @click="reset">
              <i class="fa fa-home" aria-hidden="true"></i>
            </a>
          </li>
          <li class="breadcrumb-item" v-for="package in path">
            <a v-if="isLast(path, package)">{{package.label}}</a>
            <router-link v-else :to="package.id">{{package.label}}</router-link>
          </li>
          <li v-show="query" class="breadcrumb-item">
            <span>{{ 'search-query-label' | i18n }}: <b>{{query}}</b></span>
          </li>
        </ol>
      </div>
    </div>

    <!-- Main table element -->
    <b-table bordered :items="items" :fields="fields" :filter="filter" class="text-left">
      <template slot="label" scope="label">
        <span v-if="label.item.type === 'entity'">
            <a :href="'/menu/main/dataexplorer?entity=' + label.item.id + '&hideselect=true'">
              <i class="fa fa-list" aria-hidden="true"></i> {{label.item.label}}
            </a>
          </span>
        <span v-else>
          <router-link :to="label.item.id">
            <i class="fa fa-folder-open-o" aria-hidden="true"></i> {{label.item.label}}
          </router-link>
        </span>
      </template>
    </b-table>
  </div>
</template>

<style>
  .navigator-path {
    margin-top: 2rem;
  }

  .navigator-path .breadcrumb {
    background-color: transparent;
  }
</style>

<script>
  import _ from 'lodash'
  import { QUERY_PACKAGES, QUERY_ENTITIES, RESET_STATE, GET_STATE_FOR_PACKAGE } from '../store/actions'
  import { SET_QUERY, SET_ERROR, RESET_PATH, SET_PACKAGES } from '../store/mutations'
  import { Package, INITIAL_STATE } from '../store/state'
  import { mapState } from 'vuex'

  import MyTour from './NavigatorTour'

  export default {
    name: 'Navigator',
    components: { MyTour },
    data () {
      return {
        fields: {
          label: {
            label: this.$t('table-col-header-name'),
            sortable: true,
            'class': 'text-nowrap'
          },
          description: {
            label: this.$t('table-col-header-description'),
            sortable: false,
            'class': 'd-none d-md-table-cell'
          }
        },
        filter: null,
        homeUrl: INITIAL_STATE.baseUrl
      }
    },
    methods: {
      submitQuery: _.throttle(function () {
        this.$store.commit(SET_PACKAGES, [])
        this.$store.commit(RESET_PATH)
        this.$store.dispatch(QUERY_PACKAGES, this.$store.state.query)
        this.$store.dispatch(QUERY_ENTITIES, this.$store.state.query)
      }, 200),
      selectPackage: function (packageId: string) {
        this.$store.commit(SET_QUERY, undefined)
        this.$store.dispatch(GET_STATE_FOR_PACKAGE, packageId)
      },
      isLast: function (list: Array<Package>, item: Package) {
        const tail = list[list.length - 1]
        return !!tail && !!item && tail.id === item.id
      },
      reset: function () {
        this.$store.commit(SET_QUERY, undefined)
        this.$store.dispatch(RESET_STATE)
      }
    },
    computed: {
      query: {
        get () {
          return this.$store.state.query
        },
        set (query) {
          this.$store.commit(SET_QUERY, query)
          this.submitQuery()
        }
      },
      ...mapState(['packages', 'entities', 'path']),
      items () {
        return [].concat(this.packages).concat(this.entities)
      },
      error: {
        get () {
          return this.$store.state.error
        },
        set (error) {
          this.$store.commit(SET_ERROR, error)
        }
      }
    },
    mounted: function () {
      this.$route.params.package ? this.selectPackage(this.$route.params.package) : this.$store.dispatch(RESET_STATE)
    },
    watch: {
      '$route' (to, from) {
        this.selectPackage(this.$route.params.package)
      }
    }
  }
</script>
