<template>
  <div class="container">
    <div id="app">
      <uml-viewer></uml-viewer>
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";
  /*<!--#app {-->*/
  /*background-color: $red;*/
  /*}*/
</style>

<script>

  import umlViewer from 'components/UmlViewer'

  export default {
    name: 'molgenis-model-registry-uml-viewer',
    data: function () {
      return {
        test: 'Hallo mark'
      }
    },
    components: {
      umlViewer
    }
  }

</script>
