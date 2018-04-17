<template>
    <div class="app-manager-container mt-2">
        <!-- Hidden file upload -->
        <input id="app-upload-input-field" @change="handleAppUpload" type="file" v-show="false" types=".gz, .zip"/>

        <div class="container">
            <div class="row mb-3 text-center">
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

            <template v-else-if="editing">
                <div class="row">
                    <div class="col-12">
                        <edit-existing-app-component :selectedApp="selectedApp" @save="saveEditedApp"/>
                    </div>
                </div>
            </template>

            <template v-else>
                <div class="row mb-5">
                    <div class="col-12">
                        <button @click="handleNewAppBtnClick" class="btn btn-success float-right">
                            <i class="fa fa-plus-circle"></i> Add new app
                        </button>
                    </div>
                </div>

                <div class="row">
                    <div class="col-12">
                        <div class="app-card-container">
                            <template v-if="apps.length == 0">
                                No apps were found
                            </template>

                            <template v-else>
                                <app-card-carousel :apps="apps"></app-card-carousel>
                            </template>
                        </div>
                    </div>
                </div>
            </template>
        </div>
    </div>
</template>

<style>
    .loading-spinner-container {
        height: 30vh;
    }
</style>

<script>
  import AppCardCarousel from '../components/AppCardCarousel.vue'

  export default {
    name: 'AppManagerContainer',
    data () {
      return {
        selectedFile: null,
        creating: false,
        editing: false,
        selectedApp: null,
      }
    },
    computed: {
      apps () {
        return this.$store.state.apps
      },

      error () {
        return this.$store.state.error
      },

      loading () {
        return this.$store.state.loading
      }
    },
    methods: {
      fetchApps () {
        this.$store.dispatch('FETCH_APPS')
      },

      handleNewAppBtnClick () {
        document.getElementById('app-upload-input-field').click()
      },

      handleAppUpload (event) {
        const file = event.target.files[0]
        this.$store.dispatch('UPLOAD_APP', file)
      },

      saveEditedApp (app) {
        console.log('edited', app)
        this.editing = false
      },

      showAppDetailView (app) {
        console.log(app)
//        this.selectedApp = app
      },
    },
    created () {
      this.$store.dispatch('FETCH_APPS')
    },
    components: {
      AppCardCarousel
    }
  }
</script>