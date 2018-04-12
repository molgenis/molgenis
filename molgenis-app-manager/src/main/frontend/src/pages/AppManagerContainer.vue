<template>
    <div class="app-manager-container mt-2">
        <div class="container">
            <div class="row mb-3">
                <div class="col-6">
                    <h1>App manager</h1>
                </div>
            </div>

            <div class="row" v-if="creating">
                <div class="col-12">
                    <create-new-app-component
                            @save="saveCreatedApp">
                    </create-new-app-component>
                </div>
            </div>

            <div class="row" v-else-if="editing">
                <div class="col-12">
                    <edit-existing-app-component
                            :selectedApp="selectedApp"
                            @save="saveEditedApp">
                    </edit-existing-app-component>
                </div>
            </div>

            <div class="row" v-else>
                <div class="col-6">
                    <button class="btn btn-dark float-right mb-2" @click="createNewApp">
                        Add a new app
                    </button>
                    <table class="table table-bordered table-hover">
                        <tbody>
                        <tr v-for="app in apps" :key="app.id" @click="showDetailedAppView(app)">
                            <td>
                                <button class="btn btn-sm btn-dark" :disabled="app.isActive" @click="editExistingApp">
                                    <i class="fa fa-edit"></i>
                                </button>
                            </td>
                            <td>{{app.label}}</td>
                            <td>
                                <toggle-button
                                        v-model="app.isActive"
                                        :sync="true"
                                        :labels="{checked: 'Active', unchecked: 'Inactive'}"
                                        :width="75"
                                        @change="toggleAppActiveState(app)">
                                </toggle-button>
                            </td>
                        </tr>
                        </tbody>
                    </table>
                </div>

                <div class="col-6" v-if="selectedApp">
                    <app-card-component :app="selectedApp"></app-card-component>
                </div>
            </div>
        </div>
    </div>
</template>

<script>
  import Vue from 'vue'
  import ToggleButton from 'vue-js-toggle-button'

  import AppCardComponent from '../components/AppCardComponent.vue'
  import CreateNewAppComponent from '../components/CreateNewAppComponent.vue'
  import EditExistingAppComponent from '../components/EditExistingAppComponent.vue'

  import api from '@molgenis/molgenis-api-client'

  Vue.use(ToggleButton)

  export default {
    name: 'AppManagerContainer',
    data () {
      return {
        apps: [],
        creating: false,
        editing: false,
        selectedApp: null
      }
    },
    methods: {
      createNewApp () {
        this.creating = true
      },

      editExistingApp () {
        this.editing = true
      },

      fetchApps () {
        api.get('/plugin/appmanager/apps').then(response => {
          this.apps = response
        })
      },

      saveCreatedApp (app) {
        console.log('created', app)
        this.creating = false
      },

      saveEditedApp (app) {
        console.log('edited', app)
        this.editing = false
      },

      showDetailedAppView (app) {
        this.selectedApp = app
      },

      toggleAppActiveState (app) {
        if (app.isActive) {
          api.get('/plugin/appmanager/activate/' + app.id).then(response => {
            console.log(response)
          })
        } else {
          api.get('/plugin/appmanager/deactivate/' + app.id).then(response => {
            console.log(response)
          })
        }
      }
    },
    created () {
      this.fetchApps()
    },
    components: {
      AppCardComponent,
      CreateNewAppComponent,
      EditExistingAppComponent
    }
  }
</script>