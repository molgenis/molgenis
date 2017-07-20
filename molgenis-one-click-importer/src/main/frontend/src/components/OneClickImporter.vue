<template>
  <div>
    <div class="row">
      <div class="col">
        <h1>Import your data</h1>
        <hr>
        <div v-if="error != undefined" class="alert alert-danger" role="alert">
          <button @click="error=null" type="button" class="close"><span aria-hidden="true">&times;</span></button>
          {{error}}
        </div>
      </div>
    </div>

    <div class="row">
      <div class="col-md-12">
        <form id="upload-form" v-on:submit.prevent class="form-inline">
          <div class="form-group">
            <input ref="fileInput" id="file-input" type="file" @change="setFile"/>
          </div>
          <button class="btn btn-primary" type="submit" @click="importFile" :disabled="file === null">Import</button>
        </form>
        <br/>
        <!--<dropzone FIXME-->
        <!--id="import-dropzone"-->
        <!--url="/plugin/one-click-importer/upload"-->
        <!--:accepted-filetypes="['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'].join(',')"-->
        <!--:thumbnail-height=100-->
        <!--:thumbnail-width=200-->
        <!--:duplicate-check=true-->
        <!--:use-font-awesome=true-->
        <!--:showRemoveLink=false-->
        <!--:preview-template="previewTemplate"-->
        <!--v-on:vdropzone-success="onComplete"-->
        <!--v-on:vdropzone-error="onError"-->
        <!--v-on:duplicate-file="onDuplicate">-->

        <!--<div class="text-center">supported file types: xlsx, xls, csv, tsv</div>-->
        <!--</dropzone>-->
      </div>
    </div>

    <div class="row">
      <div class="col">
        <table class="table">
          <thead>
          <th>file name</th>
          <th>View data</th>
          </thead>
          <tbody>
          <tr v-for="response in responses">
            <td>{{response.filename}}</td>
            <td><a target="_blank" :href="response.url">View...</a></td>
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
  import fetch from 'isomorphic-fetch'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        file: null,
        responses: [],
        error: null
      }
    },
    methods: {
      setFile: function (event) {
        this.file = event.target.files[0]
      },
      importFile: function () {
        const formData = new FormData()
        formData.append('file', this.file)

        const options = {
          body: formData,
          method: 'POST',
          credentials: 'same-origin'
        }

        let self = this

        fetch('/plugin/one-click-importer/upload', options).then(response => {
          if (response.headers.get('content-type') === 'application/json') {
            response.json().then(function (json) {
              const feedback = response.ok ? json : json.errors[0].message
              if (response.ok) {
                self.responses.push({
                  url: '/menu/main/dataexplorer?entity=' + feedback.entityId,
                  filename: feedback.baseFileName
                })
              } else {
                self.error = feedback
              }
            })
          }
        })
        this.$refs.fileInput.value = null
      }

//     FIXME onComplete (file, response) {
//        this.responses.push({
//          url: '/menu/main/dataexplorer?entity=' + response.entityId,
//          filename: response.baseFileName
//        })
//        this.message = null
//      },
//      onDuplicate (file) {
//        this.message = 'Can not upload duplicate file [' + file.upload.filename + ']'
//      },
//      onError (file) {
//        this.message = 'Something went wrong when uploading [' + file.upload.filename + ']. Please contact an administrator'
//      },
//      previewTemplate () {
//        return '<div></div>'
//      }
    },
    components: {
      Dropzone
    }
  }
</script>
