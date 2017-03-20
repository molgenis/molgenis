<template>
  <div id="app" class="container">
    <div class="row">
      <div class="col-4">
        <filters/>
      </div>
      <div class="col-8">
        <graph/>
      </div>
    </div>
  </div>
</template>

<script>
import Filters from './components/Filters'
import Graph from './components/Graph'

export default {
  name: 'app',
  components: {
    Filters,
    Graph
  }
}
</script>

<style>
</style>
