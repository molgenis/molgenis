<template>
  <div>
    <div class="row">
      <div class="col">
        <h1>Import your data</h1>
        <hr>
      </div>
    </div>
    <div class="row">
      <div class="col-md-12">
        <dropzone
          id="import-dropzone"
          url="/plugin/one-click-importer/upload"
          :dropzone-options="options"
          v-on:vdropzone-success="showSuccess">

          <div class="text-center">(xlsx, xls, csv, tsv)</div>

        </dropzone>
      </div>
    </div>
  </div>
</template>

<script>
  //  import { UPLOAD } from '../store/actions'
  import Dropzone from 'vue2-dropzone'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        options: {
          useFontAwesome: true,
          acceptedFileTypes: ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'].join(',')
        }
      }
    },
    methods: {
      'showSuccess': function (file, response) {
        console.log('A file was successfully uploaded', file)
        console.log('response', response)
      }
    },
    components: {
      Dropzone
    }
  }
</script>
<style scoped lang="scss">
  @import "~variables";
  @import "~mixins";

  body {
    display: flex;
    background: $white;
    font-size: 16px;
  }

  .uploader {
    box-sizing: border-box;
    width: auto;
    height: 550px;
    display: flex;
    border-radius: 6px;
    box-shadow: 1px 2px 19px rgba(195, 195, 195, 0.43);
    flex-direction: column-reverse;
    background: $white;
    box-sizing: border-box;
  }

  .upload-action .dz-message {
    color: $white;
    text-align: center;
    padding: 20px 40px;
    border: 3px dashed $white;
    border-radius: 4px;
    font-size: 16px;
  }

  .uploader-files {
    flex: 1;
    padding: 40px;
  }

  .progress-indicator {
    display: block;
    background-color: $brand-success;
    border-radius: 8px;
    height: 4px;
  }

  .upload-action {
    background: $brand-info;
    padding: 20px;
    cursor: pointer;
    transition: background 300ms ease;
  }

  .upload-action.is-dragging {
    background: $brand-success;
  }
</style>
