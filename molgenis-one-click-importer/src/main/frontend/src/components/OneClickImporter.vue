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
            <input
              id="file-input"
              ref="fileInput"
              type="file"
              accept=".csv, .zip, .xls, .xlsx, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
              @change="importFile"/>
          </div>
        </form>
        <div class="supported-types">
          <span class="text-muted"><em>Supported file types: XLSX, XLS, CSV</em></span>
        </div>
        <br/>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <h5>Imports</h5>
        <ul class="imports-list list-unstyled">

          <li v-for="upload in uploads" v-bind:key="upload.filename" class="upload-item">

            <div v-show="upload.status === 'LOADING' ">
              <i class="fa fa-spinner fa-pulse fa-fw "></i> {{upload.filename}}
            </div>

            <div v-show="upload.status === 'ERROR'">
              <i class="fa fa-times text-danger" ></i> {{upload.filename}}
              <div class="error-message text-muted"><em>Import failed; {{upload.message}}</em></div>
            </div>

            <div v-show="upload.status === 'DONE' ">
              <a target="_blank" :href="'/menu/main/navigator/' + upload.package">
                <i class="fa fa-folder-open-o" ></i> {{upload.package}}
              </a>

              <ul class="list-unstyled">
                <li v-for="table in upload.tables">
                  <span>
                    <a target="_blank" :href="'/menu/main/dataexplorer?entity=' + table.id">
                       <i class="fa fa-list"></i> {{table.label}}
                    </a>
                  </span>
                </li>
              </ul>
            </div>

          </li>

        </ul>
      </div>
    </div>

  </div>
</template>

<style>

  .supported-types {
    padding-top: 1em;
    font-size: smaller;
  }

  .imports-list {
    margin-left: 1rem;
  }

  .list-unstyled .list-unstyled {
    margin-left: 1rem;
    padding-top: 0.5rem;
  }

  .upload-item {
    padding: 0.5rem 0 1rem;
  }

  .error-message {
    margin-left: 1rem;
    padding-top: 0.5em;
    font-size: smaller;
  }

</style>

<script>
  import fetch from 'isomorphic-fetch'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        uploads: []
      }
    },
    methods: {
      importFile (event) {
        const file = event.target.files[0]
        if (!file) {
          return
        }
        let entity = {
          filename: file.name,
          status: 'LOADING'
        }
        this.uploads.unshift(entity)

        const formData = new FormData()
        formData.append('file', file)

        const options = {
          body: formData,
          method: 'POST',
          credentials: 'same-origin'
        }

        const poller = this.pollJob

        fetch('/plugin/one-click-importer/upload', options).then(response => {
          response.text().then(poller).then(job => {
            if (job.status === 'SUCCESS') {
              entity.tables = job.entityTypes
              entity.status = 'DONE'
              entity.message = job.progressMessage
              entity['package'] = job.package
            } else {
              entity.error = job.progressMessage
              entity.message = job.progressMessage
              entity.log = job.log
              entity.status = 'ERROR'
            }
          })
        })

        this.$refs.fileInput.value = null
      },
      pollJob (jobUrl) {
        const poller = this.pollJob

        return new Promise((resolve) => {
          return fetch(jobUrl, {credentials: 'same-origin'}).then(response => {
            return response.json().then(job => {
              if (job.status === 'RUNNING' || job.status === 'PENDING') {
                setTimeout(function () {
                  resolve(poller(jobUrl))
                }, 1000)
              } else {
                resolve(job)
              }
            })
          })
        })
      }
    }
  }
</script>
