<template>
  <div>
    <div v-if="loading" class="loading-spinner-container text-center">
      <span><i class="fa fa-spinner fa-3x fa-pulse"></i></span>
    </div>

    <div v-else-if="!loading && showResults">

      <!-- List packages found -->
      <div class="package-result-container">
        <div v-if="result.response.packages.length > 0" class="row">
          <div class="col-md-12">
            <h3>{{'matching-packages-label' | i18n }}</h3>

            <ul class="list-group">
              <package-search-result
                v-for="package_ in result.response.packages"
                :package_="package_"
                :highlight="highlight"
                :key="package_.id">
              </package-search-result>
            </ul>
          </div>
        </div>

        <div v-else>
          {{'no-matching-packages-label' | i18n }}
        </div>
      </div>

      <!-- List entity types found -->
      <div class="entity-type-result-container">
        <div class="row" v-if="result.response.entityTypes.length > 0">
          <div class="col-md-12">
            <h3>{{'matching-entitytypes-label' | i18n }}</h3>

            <entity-type-search-result
              v-for="entityType in result.response.entityTypes"
              :entityType="entityType"
              :highlight="highlight"
              :query="result.query"
              :key="entityType.id">
            </entity-type-search-result>
          </div>
        </div>

        <div v-else>
          {{'no-matching-entities-label' | i18n }}
        </div>
      </div>
    </div>

    <div v-else-if="result.query">
      <h3>No search results found for "{{result.query}}"</h3>
    </div>
  </div>
</template>

<style>
  .package-result-container {
    margin-bottom: 2rem;
  }

  .search-result {
    background-color: yellow;
  }
</style>

<script>
  import EntityTypeSearchResult from './EntityTypeSearchResult'
  import PackageSearchResult from './PackageSearchResult'

  import { mapState } from 'vuex'

  export default {
    name: 'search-result-container',
    methods: {
      highlight: function (text) {
        if (text !== null) {
          var iQuery = new RegExp(this.result.query, 'ig')
          return text.toString().replace(iQuery, function (matchedTxt, a, b) {
            return ('<b class=\'search-result\'>' + matchedTxt + '</b>')
          })
        }
      }
    },
    computed: {
      ...mapState(['loading', 'result']),
      showResults () {
        return this.result.response && (this.result.response.packages.length > 0 ||
          this.result.response.entityTypes.length > 0)
      }
    },
    components: {
      EntityTypeSearchResult,
      PackageSearchResult
    }
  }
</script>
