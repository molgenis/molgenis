<template>
  <div class="container">

    <!-- Alert container -->
    <div class="row">
      <div class="col-md-12">
        <div id="alert-message" v-if="alert" :class="'alert alert-' + alert.type" role="alert">
          <button @click="clearAlert()" type="button" class="close">
            <span aria-hidden="true">&times;</span>
          </button>
          <span id="message-span">{{alert.message}}</span>
        </div>
      </div>
    </div>

    <!-- Setting select + form container -->
    <div class="card">
      <div class="card-header">
        <v-select v-model="selectedSetting"
                  :options="settings"
                  :filterable="true"
                  @input="updateSelectedSetting">
        </v-select>
      </div>

      <div class="card-body">
        <div class="card-block">
          <div v-if="formData">
            <form-component
              id="settings-form"
              :formFields="formFields"
              :formData="formData"
              :formState="state">
            </form-component>
          </div>
        </div>
      </div>

      <div class="card-footer">
        <button id="save-btn" class="btn btn-primary"type="submit" @click.prevent="onSubmit(formData)">Save</button>
      </div>

    </div>
  </div>
</template>

<script>
  import vSelect from 'vue-select'
  import { FormComponent, EntityToFormMapper } from '@molgenis/molgenis-ui-form'
  import '../../node_modules/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'
  import api from '@molgenis/molgenis-api-client'

  export default {
    name: 'Settings',
    data () {
      return {
        selectedSetting: null,
        state: {},
        formFields: [],
        formData: {},
        settingsOptions: [],
        alert: null
      }
    },
    methods: {
      onSubmit: (formData) => {
        console.log('onSubmit')
        const options = {
          body: JSON.stringify(formData)
        }
        const uri = '/api/v1/' + this.selectedSetting + '/' + formData.id + '?_method=PUT'
        api.post(uri, options).then(() => {
          this.alert = {
            message: 'Settings are successfully saved',
            type: 'success'
          }
        }, this.handleError)
      },
      updateSelectedSetting (selectedSetting) {
        console.log('updateSelectedSetting')
        // this.$store.dispatch(GET_SETTINGS_BY_ID, selectedSetting)
      },
      clearAlert: () => {
        this.alert = null
      },
      handleError (error) {
        this.alert = {
          message: error,
          type: 'danger'
        }
      },
      initializeSettingsOptions (response) {
        this.settingsOptions = response.items
      },
      initializeForm (response) {
        const mappedData = EntityToFormMapper.generateForm(response.meta, response.items[0])
        this.formFields = mappedData.formFields
        this.formData = mappedData.formData
      }
    },
    created: function () {
      api.get('/api/v2/sys_set_settings').then(this.initializeSettingsOptions, this.handleError)
      api.get('/api/v2/' + this.selectedSetting).then(this.initializeForm, this.handleError)
    },
    components: {
      FormComponent,
      vSelect
    }
  }
</script>
