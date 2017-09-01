// @flow
<template>
  <div class="input-group">
    <input v-model="query" type="text" class="form-control"
           :placeholder="$t('search-placeholder')" v-on:keyup.enter="submitQuery()">
    <span class="input-group-btn">
            <button @click="submitQuery()" class="btn btn-secondary" :disabled="!query"
                    type="button"> {{'search-button-label' | i18n}}</button>
          </span>
    <span class="input-group-btn">
            <button @click="clearQuery()" class="btn btn-secondary" :disabled="!query"
                    type="button">  {{'clear-button-label' | i18n}}</button>
          </span>
  </div>
</template>

<script>
  import _ from 'lodash'
  import { SEARCH_ALL } from '../store/actions'
  import { SET_SEARCHTERM, SET_SUBMITTED } from '../store/mutations'
  export default {
    name: 'search-input',
    props: ['placeholder', 'searchLabel', 'clearLabel'],
    methods: {
      submitQuery: _.throttle(function () {
        if (this.$store.state.query) {
          this.$store.commit(SET_SUBMITTED, true)
          this.$store.dispatch(SEARCH_ALL, this.$store.state.query)
        }
      }, 200),
      clearQuery: function () {
        this.$store.commit(SET_SUBMITTED, false)
        this.$store.commit(SET_SEARCHTERM, undefined)
      }
    },
    computed: {
      query: {
        get () {
          return this.$store.state.query
        },
        set (query: string) {
          this.$store.commit(SET_SEARCHTERM, query)
        }
      }
    }
  }
</script>
