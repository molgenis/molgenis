// @flow
<template>
  <div class="container">

    <div v-if=false class="alert alert-danger" role="alert">
      <button type="button" class="close"><span aria-hidden="true">&times;</span></button>
      {{error}}
    </div>

    <!-- Search element -->
    <div class="row  mt-1">
      <div class="col-lg-6 input-group">
        <input v-model="query" type="text" class="form-control"
               placeholder="searchterm">
        <span class="input-group-btn">
          <button @click="submitQuery()" class="btn btn-secondary" :disabled="!query"
                  type="button">Submit</button>
        </span>
        <span class="input-group-btn">
          <button @click="clearQuery()" class="btn btn-secondary" :disabled="!query"
                  type="button">Clear</button>
        </span>
      </div>
    </div>
    <div v-if="this.$store.state.result">
      <div class="row  mt-1" v-if="this.$store.state.result.packages">
        <div class="col-lg-12"><h3>Matching packages</h3>
          <div class="row">
            <div class="col-lg-12 padding-card-bottom">
              <div class='card'>
                <ul class="list-group list-group-flush">
                  <li v-for="pack in this.$store.state.result.packages" class="list-group-item">
                    <a href="#"><i class="fa fa-folder-open-o" aria-hidden="true"></i> <span
                      v-html="highlight(pack.label, query)"></span></a>&nbsp
                    <small v-if="pack.description" class="text-muted">
                      <i class="text-small" v-html="highlight(pack.description, query)"></i>
                    </small>
                  </li>
                </ul>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="row mt-1" v-if="this.$store.state.result.entityTypes">
        <div v-if="this.$store.state.result.entityTypes.length > 0" class="col-lg-12"><h3>Matching entities</h3>
          <div class="row mt-1">
            <div v-for="enityType in this.$store.state.result.entityTypes"
                 class="col-lg-12 padding-card-bottom">
              <div class='card'>
                <div class="card-header">
                  <h4 v-html="highlight(enityType.getLabel, query)"></h4>
                  <a href="#" class="card-link"><i class="fa fa-folder-open-o" aria-hidden="true"></i>
                    Show in Navigator</a>
                  <a :href="dataExplorerBaseUrl + '?entity=' + enityType.getId" class="card-link"><i
                    class="fa fa-align-justify" aria-hidden="true"></i>
                    Show in Data Explorer</a>
                </div>
                <div class="container">
                  <div class="row" v-if="enityType.getDescription">
                    <div class="col-lg-4"><b>Description</b>
                    </div>
                    <div class="col-lg-8"><i v-html="highlight(enityType.getDescription, query)"></i>
                    </div>
                  </div>
                  <div class="row mt-1">
                    <div class="col-lg-4"><b>Data</b></div>
                    <div class="col-lg-8"><a v-if="enityType.nrOfMatchingEntities > 0"
                                             :href="dataExplorerBaseUrl + '?entity=' + enityType.getId +'&query[q][0][operator]=SEARCH&query[q][0][value]='+query"
                                             class="card-link"><i
                      class="fa fa-align-justify" aria-hidden="true"></i>
                      {{enityType.nrOfMatchingEntities}} rows</a>
                      <span v-else><i
                        class="fa fa-align-justify" aria-hidden="true"></i>
                      0 rows</span></div>
                  </div>
                  <div class="row mt-1">
                    <div class="col-lg-4"><b>Attributes</b></div>
                    <div class="col-lg-8"><span
                      v-if="enityType.getAttributes.length === 0"><i>0 attributes found</i></span>
                      <ul v-else class="list-unstyled no-margin-bottom">
                        <li v-for="attr in enityType.getAttributes"><span
                          v-html="highlight(attr.label, query)"></span>(<i
                          v-html="highlight(attr.description, query)"></i>)
                        </li>
                      </ul>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <div v-else-if="query" class="col-lg-12"><h3>No matching entities found</h3></div>
  </div>
</template>

<style>
  .padding-card-bottom {
    padding-bottom: 15px;
  }

  .no-margin-bottom {
    margin-bottom: 0px;
  }
</style>


<script>
  import _ from 'lodash'
  import { SEARCH_ALL } from '../store/actions'
  import { SET_SEARCHTERM } from '../store/mutations'
  export default {
    name: 'SearchAll',
    data () {
      return {
        navigatorBaseUrl: window.searchall.navigatorBaseUrl,
        dataExplorerBaseUrl: window.searchall.dataExplorerBaseUrl
      }
    },
    methods: {
      submitQuery: _.throttle(function () {
        this.$store.dispatch(SEARCH_ALL, this.$store.state.query)
      }, 200),
      clearQuery: function () {
        this.$store.commit(SET_SEARCHTERM, undefined)
      },
      highlight: function (text, query) {
        var iQuery = new RegExp(query, 'ig')
        return text.toString().replace(iQuery, function (matchedTxt, a, b) {
          return ('<b class=\'text-success\'>' + matchedTxt + '</b>')
        })
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
