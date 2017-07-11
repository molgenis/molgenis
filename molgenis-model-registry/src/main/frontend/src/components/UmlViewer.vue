<template>
  <div id="sample">
    <div style="border: solid 1px black; width:600px; height:600px" ref="targetDiv"></div>
  </div>
</template>

<script>

  import { newGraph } from './Graph.js'
  import { mapGetters } from 'vuex'

  export default {
    name: 'model-registry-uml-viewer',
    mounted: function () {
      const targetDiv = this.$refs.targetDiv
      newGraph(targetDiv, this.graph)
    },
    updated: function () {
      const targetDiv = this.$refs.targetDiv
      newGraph(targetDiv, this.graph)
    },
    computed: {
      ...mapGetters(['graph'])
    }

  }

</script>
