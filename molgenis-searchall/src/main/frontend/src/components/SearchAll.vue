<template>
  <div>
    <div v-if=false class="alert alert-danger" role="alert">
      <button type="button" class="close"><span aria-hidden="true">&times;</span></button>
      {{error}}
    </div>

    <div class="row mt-1">
      <div class="col-lg-6">
        <search-input></search-input>
      </div>
    </div>

    <div v-if="this.$store.state.result">

      <div class="row  mt-1" v-if="this.$store.state.result.packages.length > 0">
        <packages-result :packages="this.$store.state.result.packages" :navigator="navigatorBaseUrl"
                         :highlight="highlight"></packages-result>
      </div>

      <div v-else-if="this.$store.state.submitted">{{'no-matching-packages-label' | i18n }}</div>

      <div class="row mt-1" v-if="this.$store.state.result.entityTypes.length > 0">
        <div class="col-lg-12"><h3>
          {{'matching-entitytypes-label' | i18n }}</h3>
          <div class="row mt-1">
            <div v-for="entityType in this.$store.state.result.entityTypes"
                 class="col-lg-12 padding-card-bottom">
              <entity-result :entityType="entityType" :dataexplorer="dataExplorerBaseUrl"
                             :navigator="navigatorBaseUrl" :highlight="highlight"></entity-result>
            </div>
          </div>
        </div>
      </div>

      <div v-else-if="this.$store.state.submitted">{{'no-matching-entities-label' | i18n }}</div>

    </div>
  </div>
</template>

<style>
  .padding-card-bottom {
    padding-bottom: 15px;
  }

  .no-margin-bottom {
    margin-bottom: 0px;
  }

  .search-result {
    background-color: yellow;
  }
</style>


<script>
  import SearchInput from './SearchInput'
  import PackagesResult from './PackagesResult'
  import EntityResult from './EntityResult'

  export default {
    name: 'SearchAll',
    components: {SearchInput, PackagesResult, EntityResult},
    data () {
      return {
        navigatorBaseUrl: window.searchall.navigatorBaseUrl,
        dataExplorerBaseUrl: window.searchall.dataExplorerBaseUrl,
        submitted: true
      }
    },
    methods: {
      highlight: function (text) {
        if (text !== null) {
          var iQuery = new RegExp(this.$store.state.query, 'ig')
          return text.toString().replace(iQuery, function (matchedTxt, a, b) {
            return ('<b class=\'search-result\'>' + matchedTxt + '</b>')
          })
        }
      }
    }
  }
</script>
