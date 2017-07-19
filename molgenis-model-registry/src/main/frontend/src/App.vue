<template>
  <div id="app">
    <uml-viewer></uml-viewer>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";
</style>

<script>

  import umlViewer from 'components/UmlViewer'
  import { GET_UMLDATA } from './store/actions'

  export default {
    name: 'molgenis-model-registry-uml-viewer',
    components: {
      umlViewer
    },
    created: function () {
      this.$store.dispatch(GET_UMLDATA)
    }
  }

</script>
