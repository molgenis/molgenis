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
          :accepted-filetypes="['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'].join(',')"
          :thumbnail-height=100
          :thumbnail-width=200
          :duplicate-check=true
          :use-font-awesome=true
          :showRemoveLink=false
          :preview-template="previewTemplate"
          v-on:vdropzone-success="onComplete"
          v-on:vdropzone-error="onError"
          v-on:duplicate-file="onDuplicate">

            <div class="text-center">supported file types: xlsx, xls, csv, tsv</div>
        </dropzone>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <div id="alert-container">
          <div v-if="message" class="alert alert-warning" role="alert">{{message}}</div>
        </div>
        <table class="table">
          <thead>
            <th>file name</th>
            <th>View data</th>
          </thead>
          <tbody>
            <tr v-for="response in responses" >
              <td>{{response.filename}}</td>
              <td><a :href="response.url">View...</a></td>
            </tr>
          </tbody>
        </table>
      </div>
    </div>

  </div>
</template>

<style scoped>
  #alert-container {
    padding-top: 10px;
  }
</style>

<script>
  import Dropzone from 'vue2-dropzone'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        responses: [],
        message: null
      }
    },
    methods: {
      onComplete (file, response) {
        this.responses.push({url: '/menu/main/dataexplorer?entity=' + response.entityId, filename: response.baseFileName})
        this.message = null
      },
      onDuplicate (file) {
        this.message = 'Can not upload duplicate file [' + file.upload.filename + ']'
      },
      onError (file) {
        this.message = 'Something went wrong when uploading [' + file.upload.filename + ']. Please contact an administrator'
      },
      previewTemplate () {
        return '<div></div>'
      }
    },
    components: {
      Dropzone
    }
  }
</script>
