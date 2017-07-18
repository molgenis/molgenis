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
          v-on:vdropzone-success="onComplete"
          v-on:vdropzone-error="onError"
          v-on:duplicate-file="onDuplicate">

          <div v-if="message" class="alert alert-warning" role="alert">{{message}}</div>
          <div class="text-center">supported file types: xlsx, xls, csv, tsv</div>

        </dropzone>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <ul>
          <li v-for="response in responses">
            <a :href="response.url">View {{response.filename}}</a>
          </li>
        </ul>
      </div>
    </div>

  </div>
</template>

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
        this.responses.push({url: '/menu/main/dataexplorer?entity=' + response, filename: file.upload.filename})
        this.message = null
      },
      onDuplicate (file) {
        this.message = 'Can not upload duplicate file [' + file.upload.filename + ']'
      },
      onError (file) {
        this.message = 'Something went wrong when uploading [' + file.upload.filename + ']. Please contact an administrator'
      }
    },
    components: {
      Dropzone
    }
  }
</script>
