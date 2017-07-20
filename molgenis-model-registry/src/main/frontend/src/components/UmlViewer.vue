<template>
  <!-- read out graph so we can watch it -->
  <div class="container">
    <div v-if="error != undefined" class="alert alert-danger" role="alert">
      <button @click="error=null" type="button" class="close"><span aria-hidden="true">&times;</span></button>
      {{error}}
    </div>
    <div id="graph-container" ref="graphDiv" v-if="umlData"></div>
  </div>
</template>

<style scoped src="bootstrap/dist/css/bootstrap.css"></style>
<style scoped lang="scss">
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
  import {SET_ERROR} from '../store/mutations'

  export default {
    name: 'model-registry-uml-viewer',
    updated () {
      const graphDiv = this.$refs.graphDiv
      newGraph(graphDiv, this.umlData)
    },
    computed: {
      ...mapGetters(['umlData']),
      error: {
        get () {
          return this.$store.state.error
        },
        set (error) {
          this.$store.commit(SET_ERROR, error)
        }
      }
    }

  }

</script>
