<template>
  <div id="app">
    <div class="container">
      <div class="row">
        <div class="col-md-3">
          <filters/>
        </div>
        <div class="col-md-9">
          <graph/>
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
