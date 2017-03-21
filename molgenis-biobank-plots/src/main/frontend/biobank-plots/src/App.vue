<template>
  <div id="app" class="container-fluid">
    <div class="row">
      <div class="col-2">
        <filters/>
      </div>
      <div class="col-6">
        <graph/>
      </div>
      <div class="col-4">
        <attribute-graphs/>
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
