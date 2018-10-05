// @flow
<template>
  <div class="container navigator">
    <div v-if="error != undefined" class="alert alert-danger" role="alert">
      <button @click="error=null" type="button" class="close"><span
        aria-hidden="true">&times;</span></button>
      {{error}}
    </div>

    <!-- Search element -->
    <div class="navigator-search row justify-content-center">
      <div class="input-group col-lg-6">
        <input v-model="query" type="text" class="form-control"
               :placeholder="$t('search-input-placeholder')">
        <div class="input-group-append">
          <button @click="submitQuery()" class="btn btn-primary" :disabled="!query"
                  type="button"><font-awesome-icon icon="search" size="lg"/>
          </button>
        </div>
      </div>
    </div>

    <!-- Breadcrumb element -->
    <div class="navigator-path row">
      <div class="col-10 input-group">
        <ol class="breadcrumb">
          <li class="breadcrumb-item">
            <a :href="homeUrl" v-on:click="reset"><!-- TODO replace with router-link -->
              <font-awesome-icon icon="home"/>
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

    <NavigatorActions class="navigator-actions"/>
    <!-- Main table element -->
    <b-table bordered :items="items" :fields="fields" :filter="filter" class="text-left">
      <template slot="HEAD_selected" scope="data">
        <b-form-checkbox @click.native.stop v-model="allSelected"
                         @change="toggleAllSelected"></b-form-checkbox>
      </template>
      <template slot="selected" scope="row">
        <b-form-checkbox @click.native.stop :checked="isSelected(row.item)"
                         @change="toggleSelected(row.item, $event)"></b-form-checkbox>
      </template>
      <template slot="label" scope="label">
        <span v-if="label.item.type === 'entity'">
            <a :href="'/menu/main/dataexplorer?entity=' + label.item.id + '&hideselect=true'">
              <font-awesome-icon icon="list"/> {{label.item.label}}
            </a>
          </span>
        <span v-else>
          <router-link :to="label.item.id">
            <font-awesome-icon :icon="['far', 'folder-open']"/> {{label.item.label}}
          </router-link>
        </span>
      </template>
    </b-table>
  </div>
</template>

<style scoped>
  @media (min-width: 1200px) {
    .navigator {
      width: 1199px;
    }
  }

  .navigator-actions {
    margin-bottom: 10px;
  }

  .navigator-search {
    margin-bottom: 10px;
  }

  .navigator-path .breadcrumb {
    background-color: transparent;
  }
</style>

<script>
  import _ from 'lodash'
  import {
    QUERY_PACKAGES,
    QUERY_ENTITIES,
    RESET_STATE,
    GET_STATE_FOR_PACKAGE,
    SELECT_ALL_PACKAGES_AND_ENTITY_TYPES,
    DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES,
    DESELECT_PACKAGE,
    SELECT_PACKAGE,
    DESELECT_ENTITY_TYPE,
    SELECT_ENTITY_TYPE
  } from '../store/actions'
  import { SET_QUERY, SET_ERROR, RESET_PATH, SET_PACKAGES } from '../store/mutations'
  import { Package, INITIAL_STATE } from '../store/state'
  import { mapState } from 'vuex'
  import NavigatorActions from './NavigatorActions'

  export default {
    name: 'Navigator',
    components: {NavigatorActions},
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
          },
          selected: null
        },
        filter: null,
        homeUrl: INITIAL_STATE.baseUrl,
        allSelected: false
      }
    },
    methods: {
      submitQuery: _.throttle(function () {
        this.$store.commit(SET_PACKAGES, [])
        this.$store.commit(RESET_PATH)
        const query = this.$store.state.query
        if (query === undefined || query === null || query === '') {
          this.$store.dispatch(RESET_STATE)
        } else {
          this.$store.dispatch(QUERY_PACKAGES, query)
          this.$store.dispatch(QUERY_ENTITIES, query)
        }
        this.allSelected = false
      }, 200),
      selectPackage: function (packageId: string) {
        this.$store.commit(SET_QUERY, undefined)
        this.$store.dispatch(GET_STATE_FOR_PACKAGE, packageId)
        this.allSelected = false
      },
      isLast: function (list: Array<Package>, item: Package) {
        const tail = list[list.length - 1]
        return !!tail && !!item && tail.id === item.id
      },
      reset: function () {
        this.$store.commit(SET_QUERY, undefined)
        this.$store.dispatch(RESET_STATE)
      },
      toggleSelected: function (item, checked) {
        if (checked) {
          this.$store.dispatch(item.type === 'entity' ? SELECT_ENTITY_TYPE : SELECT_PACKAGE,
            item.id)
          this.allSelected = this.nrItems === this.nrSelectedItems
        } else {
          this.$store.dispatch(item.type === 'entity' ? DESELECT_ENTITY_TYPE : DESELECT_PACKAGE,
            item.id)
          this.allSelected = false
        }
      },
      isSelected: function (item) {
        const itemIds = item.type === 'entity' ? this.$store.state.selectedEntityTypeIds
          : this.$store.state.selectedPackageIds
        return itemIds.indexOf(item.id) !== -1
      },
      toggleAllSelected: function (checked) {
        this.$store.dispatch(
          checked ? SELECT_ALL_PACKAGES_AND_ENTITY_TYPES : DESELECT_ALL_PACKAGES_AND_ENTITY_TYPES)
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
      ...mapState(['packages', 'entities', 'path', 'selectedEntityTypeIds', 'selectedPackageIds']),
      items () {
        var packages = this.packages.map(aPackage => Object.assign(
          this.selectedPackageIds.indexOf(aPackage.id) === -1 ? {}
            : {toggleSelected: this.toggleSelected}, aPackage))
        var entityTypes = this.entities.map(entityType => Object.assign(
          this.selectedEntityTypeIds.indexOf(entityType.id) === -1 ? {}
            : {toggleSelected: this.toggleSelected}, entityType))
        return packages.concat(entityTypes)
      },
      error: {
        get () {
          return this.$store.state.error
        },
        set (error) {
          this.$store.commit(SET_ERROR, error)
        }
      },
      nrItems () {
        return this.entities.length + this.packages.length
      },
      nrSelectedItems () {
        return this.selectedEntityTypeIds.length + this.selectedPackageIds.length
      }
    },
    mounted: function () {
      this.$route.params.package ? this.selectPackage(this.$route.params.package)
        : this.$store.dispatch(RESET_STATE)
    },
    watch: {
      '$route' (to, from) {
        this.selectPackage(this.$route.params.package)
      }
    }
  }
</script>
