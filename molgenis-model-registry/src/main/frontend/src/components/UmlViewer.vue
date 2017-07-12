<template>
  <div id="sample">
    <!-- read out graph so we can watch it -->
    <div style="border: solid 1px black; width:600px; height:600px" ref="targetDiv" v-if="graph"></div>
  </div>
</template>

<script>

  import { newGraph } from './Graph.js'
  import { mapGetters } from 'vuex'

  export default {
    name: 'model-registry-uml-viewer',
    updated () {
      const targetDiv = this.$refs.targetDiv
      console.log(this.graph)
      newGraph(targetDiv, this.graph)
    },
    computed: {
      ...mapGetters(['graph'])
    }

  }

</script>
