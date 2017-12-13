<template>
  <div class="input-group">
    <input v-model="query"
           v-on:keypress.enter="submitQuery()"
           type="text"
           class="form-control"
           :placeholder="$t('search-placeholder')">

    <span class="input-group-btn">
      <button @click="submitQuery()" class="btn btn-primary" :disabled="!query"
              type="button"> {{'search-button-label' | i18n}}
      </button>
    </span>

    <span class="input-group-btn">
      <button @click="clearQuery()" class="btn btn-light" :disabled="!query"
              type="button"> {{'clear-button-label' | i18n}}
      </button>
    </span>
  </div>
</template>

<script>
  import { SEARCH_ALL } from '../store/actions'
  import { RESET_RESPONSE } from '../store/mutations'

  export default {
    name: 'search-input',
    data () {
      return {
        query: ''
      }
    },
    methods: {
      submitQuery () {
        if (this.query !== '') {
          this.$store.dispatch(SEARCH_ALL, this.query)
        }
      },
      clearQuery () {
        this.query = ''
        this.$store.commit(RESET_RESPONSE)
      }
    }
  }
</script>
