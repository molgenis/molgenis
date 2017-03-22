<template>
  <div id="app">
    <div class="container">
      <div class="row">
        <div class="col-md-3">
          <filters/>
        </div>
        <div class="col-md-9">
          <graph/>
          <br/>
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

export default {
  name: 'app',
  components: {
    Filters,
    Graph,
    AttributeGraphs
  }
}
</script>
