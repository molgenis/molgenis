<template>
    <div class="app-manager-container mt-2">
        <!-- Hidden file upload -->
        <input id="app-upload-input-field" @change="handleFileUpload" ref="app-upload-field" type="file" v-show="false"
               accept=".gz, .zip"/>

        <div class="container">
            <div class="row text-center">
                <div class="col-12">
                    <h1>App manager</h1>

                    <template v-if="error">
                        <div class="alert alert-warning" role="alert">
                            {{ error }}
                        </div>
                    </template>

                    <hr>
                </div>
            </div>

            <template v-if="loading">
                <div class="row">
                    <div class="col-12">
                        <div class="loading-spinner-container d-flex flex-column justify-content-center align-items-center">
                            <i class="fa fa-spinner fa-spin fa-5x my-3"></i>
                            <p class="text-muted">Fetching apps...</p>
                        </div>
                    </div>
                </div>
            </template>

            <template v-else>
                <div class="row">
                    <div class="col-12">
                        <button @click="triggerFileBrowser" class="btn btn-success float-right mb-3">
                            <i class="fa fa-plus-circle"></i> Add new app
                        </button>
                    </div>
                </div>

                <div class="row">
                    <div class="col-12">
                        <div class="search-box-container mb-4 d-flex justify-content-center">
                            <input v-model="searchQuery" class="form-control search-box" type="search"
                                   placeholder="Search apps" aria-label="Search apps">
                        </div>
                    </div>
                </div>

                <div class="row">
                    <div class="col-12">
                        <div class="app-card-container">
                            <template v-if="apps.length == 0">
                                No apps were found
                            </template>

                            <template v-else>
                                <app-card-gallery :apps="apps"></app-card-gallery>
                            </template>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<style>
    .search-box {
        border-radius: 10px;
    }

    .loading-spinner-container {
        height: 30vh;
    }
</style>

<script>
  import AppCardGallery from '../components/AppCardGallery.vue'

  export default {
    name: 'AppManagerContainer',
    data () {
      return {
        searchQuery: ''
      }
    },
    computed: {
      apps () {
        const apps = this.$store.state.apps
        const query = this.searchQuery
        return query ? apps.filter(app => app.label.indexOf(query) >= 0 || app.description.indexOf(query) >= 0) : apps
      },

      error () {
        return this.$store.state.error
      },

      loading () {
        return this.$store.state.loading
      }
    },
    methods: {
      triggerFileBrowser () {
        // Trigger file upload by clicking the hidden input
        document.getElementById('app-upload-input-field').click()
      },

      handleFileUpload (event) {
        const file = event.target.files[0]
        this.$store.dispatch('UPLOAD_APP', file)

        // Clear the existing value to make it possible to upload the same file again
        this.$refs['app-upload-field'].value = ''
      }
    },
    created () {
      this.$store.dispatch('FETCH_APPS')
    },
    components: {
      AppCardGallery
    }
  }
</script>