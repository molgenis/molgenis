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
        <form id="upload-form" v-on:submit.prevent class="form-inline">
          <div class="form-group">
            <input ref="fileInput" id="file-input" type="file" @change="setFile"/>
          </div>
          <button class="btn btn-primary" type="submit" @click="importFile" :disabled="file === null">Import</button>
        </form>
        <div class="supported-types">
          <span class="text-muted"><em>Supported file types: XLSX, XLS, CSV</em></span>
        </div>
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
          <th>Uploads</th>
          <th></th>
          </thead>
          <tbody>
          <tr v-for="response in responses">
            <td v-if="response.loading">
              <span>{{response.filename}}</span>
              <i class="fa fa-spinner fa-pulse fa-fw"></i>
            </td>
            <td v-else>
              <a target="_blank" :href="response.url">{{response.filename}}</a>
              <span class="success-check"><i class="fa fa-check" aria-hidden="true"></i></span>
            </td>
            <td v-if="response.error">
              <span class="error-message">{{response.error}}</span>
            </td>
            <td v-else></td>
          </tr>
          </tbody>
        </table>
      </div>
    </div>
  </div>
</template>

<style lang="scss">
  @import "~variables";

  .error-message {
    color: $brand-danger
  }

  .supported-types {
    padding-top: 1em;
    font-size: smaller;
  }

  .success-check {
    color: $brand-success
  }

</style>

<script>
  import fetch from 'isomorphic-fetch'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        file: null,
        responses: []
      }
    },
    methods: {
      setFile: function (event) {
        this.file = event.target.files[0]
      },
      importFile: function () {
        let entity = {filename: this.file.name, loading: true}
        this.responses.push(entity)

        const formData = new FormData()
        formData.append('file', this.file)

        const options = {
          body: formData,
          method: 'POST',
          credentials: 'same-origin'
        }

        fetch('/plugin/one-click-importer/upload', options).then(response => {
          if (response.headers.get('content-type') === 'application/json') {
            response.json().then(function (json) {
              const feedback = response.ok ? json : json.errors[0].message
              if (response.ok) {
                entity.filename = feedback.baseFileName
                entity.loading = false
                entity.url = '/menu/main/dataexplorer?entity=' + feedback.entityId
              } else {
                entity.error = feedback
                entity.loading = false
              }
            })
          }
        })
        this.$refs.fileInput.value = null
        this.file = null
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
    }
  }
</script>
