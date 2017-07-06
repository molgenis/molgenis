<template>
  <div class="container">
    <div id="app">
      <h1>{{test}}</h1>
      <p>{{uml}}</p>
      <!--<hello-world></hello-world>-->
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";

  #app {
    background-color: $red;
  }
</style>


<script>

  import umlViewer from 'components/UmlViewer'

  window.$ = require('jquery');
  window.joint = require('jointjs');
  window._ = require('underscore')

  export default {
    name: 'molgenis-model-registry-uml-viewer',
    data: function () {
      return {
        test: 'Hallo mark'
      }
    }
  }


  $(document).ready()

</script>
