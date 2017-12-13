<template>
  <div class="container search-all-container">
    <div class="row">
      <div class="col-md-12">
        <div class="col-md-6 offset-md-6 search-input">
          <search-input></search-input>
        </div>
        <hr>
      </div>
    </div>
    <div class="row">
      <div class="col-md-12 search-result-container">
        <search-result-container></search-result-container>
      </div>
    </div>
  </div>
</template>

<style>
  .search-all-container {
    margin-top: 1rem;
  }

  .search-input {
    margin-bottom: 1rem;
  }
</style>

<script>
  import SearchInput from './components/SearchInput.vue'
  import SearchResultContainer from './components/SearchResultContainer.vue'

  export default {
    name: 'molgenis-searchall',
    components: {
      SearchInput,
      SearchResultContainer
    }
  }
</script>
