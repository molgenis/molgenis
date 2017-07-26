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
              <span v-if="currentProgressMessage">{{currentProgressMessage}}</span>
              <i class="fa fa-spinner fa-pulse fa-fw"></i>
            </td>

            <td v-else>
              <div v-if="!response.error">
                Imported entities from <strong><a :href="'/menu/main/navigator/' + response.package">{{response.filename}}</a></strong>:
                <br/> 

                <span v-for="entityType in response.entityTypes">
                <a target="_blank"
                   :href="'/menu/main/dataexplorer?entity=' + entityType.id">{{entityType.label}}</a> <span
                  class="success-check"><i class="fa fa-check" aria-hidden="true"></i></span> <br>
              </span>
              </div>
              <div v-else>
                Something went wrong while importing <strong>{{response.filename}}</strong>
              </div>
            </td>

            <td v-if="response.log">
              <div v-if="!showDetails">
                {{response.message}}
              </div>

              <button v-if="showDetails" class="btn btn-sm btn-secondary" @click="toggleDetails">Hide details</button>
              <button v-if="!showDetails" class="btn btn-sm btn-secondary float-left" @click="toggleDetails">
                Show details
              </button>
              <div v-if="showDetails">
                <hr>
                <textarea rows="4" cols="50">
                  {{response.log}}
                </textarea>
              </div>
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
        responses: [],
        currentProgressMessage: null,
        showDetails: false
      }
    },
    methods: {
      toggleDetails () {
        this.showDetails = !this.showDetails
      },
      setFile (event) {
        this.file = event.target.files[0]
      },
      importFile () {
        let entity = {filename: this.file.name, loading: true}
        this.responses.push(entity)

        const formData = new FormData()
        formData.append('file', this.file)

        const options = {
          body: formData,
          method: 'POST',
          credentials: 'same-origin'
        }

        const poller = this.pollJob
        const self = this

        fetch('/plugin/one-click-importer/upload', options).then(response => {
          self.currentProgressMessage = 'Starting import'
          response.text().then(poller).then(job => {
            if (job.status === 'SUCCESS') {
              entity.entityTypes = job.entityTypes
              entity.log = job.log
              entity.loading = false
              entity.message = job.progressMessage
              entity.package = job.package

              self.currentProgressMessage = null
            } else {
              entity.error = job.progressMessage
              entity.message = job.progressMessage
              entity.log = job.log
              entity.loading = false

              self.currentProgressMessage = null
            }
          })
        })

        this.$refs.fileInput.value = null
        this.file = null
      },
      pollJob (jobUrl) {
        const poller = this.pollJob
        const self = this

        return new Promise((resolve) => {
          return fetch(jobUrl, {credentials: 'same-origin'}).then(response => {
            return response.json().then(job => {
              if (job.status === 'RUNNING' || job.status === 'PENDING') {
                self.currentProgressMessage = job.progressMessage ? job.progressMessage : self.currentProgressMessage

                setTimeout(function () {
                  resolve(poller(jobUrl))
                }, 1000)
              } else {
                self.currentProgressMessage = job.progressMessage
                resolve(job)
              }
            })
          })
        })
      }
    }
  }
</script>
