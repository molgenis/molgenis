<template>
  <div>
    <div class="row">
      <div class="col-md-12">
        <form id="upload-form" @submit.prevent class="form">
          <div class="form-group">
            <label for="file-input">{{ 'import-input-label' | i18n }}</label>
            <input
              id="file-input"
              class="form-control"
              ref="fileInput"
              type="file"
              accept=".csv, .zip, .xls, .xlsx, text/csv, application/zip, application/vnd.openxmlformats-officedocument.spreadsheetml.sheet,application/vnd.ms-excel"
              @change="importFile"/>
            <div class="supported-types">
              <span class="text-muted"><em>{{ 'file-types' | i18n }}: XLSX, XLS, CSV</em></span>
            </div>
          </div>
        </form>
      </div>
    </div>

    <div class="row">
      <div class="col">
        <strong>{{ 'import-list-header' | i18n }}</strong>
        <ul class="imports-list list-unstyled">

          <!-- Running job -->
          <div v-if="job" v-show="job && job.status === 'RUNNING' || job.status === 'PENDING' ">
            <i class="fa fa-spinner fa-pulse fa-fw "></i> {{job.file}}
          </div>

          <!-- List of finished jobs -->
          <li v-for="job in finishedJobs" :key="job.file" class="upload-item">

            <div v-if="job.status === 'FAILED'">
              <i class="fa fa-times text-danger"></i> {{job.file}}
              <div class="error-message text-muted"><em>{{ 'error-message-prefix' | i18n }}; {{job.progressMessage}}</em></div>
            </div>

            <div v-if="job.status === 'SUCCESS' ">
              <span v-if="navigatorBaseUrl">
                <a target="_blank" :href="navigatorBaseUrl + '/' + job.package">
                  <i class="fa fa-folder-open-o"></i> {{job.package}}
                </a>
              </span>

              <span v-else>
                <i class="fa fa-folder-open-o"></i> {{job.package}}
              </span>

              <ul class="list-unstyled">
                <li v-for="table in JSON.parse(job.entityTypes)">

                  <span v-if="dataExplorerBaseUrl">
                    <a target="_blank" :href="dataExplorerBaseUrl + '?entity=' + table.id">
                      <i class="fa fa-list"></i> {{table.label}}
                     </a>
                  </span>

                  <span v-else>
                    <i class="fa fa-list"></i> {{table.label}}
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
  import { mapState } from 'vuex'
  import { IMPORT_FILE } from '../store/actions'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        navigatorBaseUrl: window.__INITIAL_STATE__.navigatorBaseUrl,
        dataExplorerBaseUrl: window.__INITIAL_STATE__.dataExplorerBaseUrl
      }
    },
    computed: {
      ...mapState(['job', 'finishedJobs'])
    },
    methods: {
      importFile (event) {
        const file = event.target.files[0]
        if (!file) return

        this.$store.dispatch(IMPORT_FILE, file)
        this.$refs.fileInput.value = null
      }
    }
  }
</script>
