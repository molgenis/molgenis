<template>
  <div id="app">
    <div class="container-fluid">
      <div class="row">
        <div class="col-md-8 col-sm-12">
          <br>
          <h1>Possible title</h1>
          <hr>
          <div class="row">
            <div class="col-md-4">
              <filters/>
            </div>
            <div class="col-md-8 ">
              <graph/>
            </div>
          </div>
        </div>
        <div class="col-md-4 col-sm-12">
          <br>
          <attribute-graphs/>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import Filters from './components/Filters'
import Graph from './components/Graph'
import AttributeGraphs from './components/AttributeGraphs'

import { SET_FILTER_ASYNC } from './store/actions'

export default {
  name: 'app',
  components: {
    Filters,
    Graph,
    AttributeGraphs
  },
  created () {
    this.$store.dispatch(SET_FILTER_ASYNC, {name: '', value: false})
  }
}
</script>
