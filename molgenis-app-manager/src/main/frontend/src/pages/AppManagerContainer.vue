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
                                <swiper :options="swiperOptions" ref="app-swiper">
                                    <swiper-slide v-for="app in apps" :key="app.id" @click="showAppDetailView(app)">
                                        <app-card-component :app="app"></app-card-component>
                                    </swiper-slide>

                                    <div class="swiper-button-prev" slot="button-prev"></div>
                                    <div class="swiper-button-next" slot="button-next"></div>
                                    <div class="swiper-pagination" slot="pagination"></div>
                                </swiper>
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
  import Vue from 'vue'

  import ToggleButton from 'vue-js-toggle-button'
  import VueAwesomeSwiper from 'vue-awesome-swiper'
  import 'swiper/dist/css/swiper.css'

  import AppCardComponent from '../components/AppCardComponent.vue'
  import EditExistingAppComponent from '../components/EditExistingAppComponent.vue'

  import api from '@molgenis/molgenis-api-client'

  Vue.use(ToggleButton)
  Vue.use(VueAwesomeSwiper)

  export default {
    name: 'AppManagerContainer',
    data () {
      return {
        apps: [],
        selectedFile: null,
        creating: false,
        editing: false,
        loading: true,
        error: '',
        selectedApp: null,
        swiperOptions: {
          slidesPerView: 3,
          centeredSlides: true,
          effect: 'coverflow',
          spaceBetween: 5,
          loop: true,
          watchOverflow: true,
          pagination: {
            el: '.swiper-pagination',
            type: 'bullets'
          },
          navigation: {
            nextEl: '.swiper-button-next',
            prevEl: '.swiper-button-prev'
          }
        }
      }
    },
    methods: {
      deleteApp (app) {
        api.get('/plugin/appmanager/delete/' + app.id).then(() => {
          this.fetchApps()
        }, error => {
          this.error = error
        })
      },

      fetchApps () {
        api.get('/plugin/appmanager/apps').then(response => {
          this.apps = response
          this.loading = false
        }, () => {
          this.error = 'no permission to see apps, please login before continuing'
        })
      },

      handleNewAppBtnClick () {
        document.getElementById('app-upload-input-field').click()
      },

      handleAppUpload (event) {
        const file = event.target.files[0]
        api.postFile('/plugin/appmanager/upload', file).then(() => {
          this.fetchApps()
        }, error => {
          this.error = error
        })
      },

      saveEditedApp (app) {
        console.log('edited', app)
        this.editing = false
      },

      showAppDetailView (app) {
        this.selectedApp = app
      },

      toggleAppActiveState (app) {
        if (app.isActive) {
          api.get('/plugin/appmanager/activate/' + app.id).then(response => {
            console.log(response)
          }, error => {
            this.error = error
          })
        } else {
          api.get('/plugin/appmanager/deactivate/' + app.id).then(response => {
            console.log(response)
          }, error => {
            this.error = error
          })
        }
      }
    },
    created () {
      this.fetchApps()
    }
    ,
    components: {
      AppCardComponent,
      EditExistingAppComponent
    }
  }
</script>