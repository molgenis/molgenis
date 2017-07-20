<template>
  <!-- read out graph so we can watch it -->
  <div id="graph-container" ref="graphDiv" v-if="umlData"></div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";

  #graph-container {
    border: solid 1px black;
    width: 100%;
    height: 1000px;
  }
</style>

<script>

  import {newGraph} from './Graph.js'
  import {mapGetters} from 'vuex'

  export default {
    name: 'model-registry-uml-viewer',
    updated () {
      const graphDiv = this.$refs.graphDiv
      newGraph(graphDiv, this.umlData)
    },
    computed: {
      ...mapGetters(['umlData'])
    }

  }

</script>
