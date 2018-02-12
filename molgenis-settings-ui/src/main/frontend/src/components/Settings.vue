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

    <div class="row">
      <div class="col-md-12">
      <select v-model="selectedSetting" class="form-control">
        <option v-for="option in settingsOptions" v-bind:value="option.id">
          {{option.label}}
        </option>
      </select>

      <button
        id="save-btn-top"
        class="btn btn-primary float-right mt-2"
        type="submit"
        @click.prevent="onSubmit(formData)"
        :disabled="state.$pristine || !state.$valid">
        Save changes
      </button>
      </div>
    </div>



    <hr/>

    <div v-if="showForm">
      <h2>{{settingLabel}}</h2>
      <form-component
        id="settings-form"
        :formFields="formFields"
        :formData="formData"
        :formState="state">
      </form-component>
    </div>

  </div>

</template>

<script>
  import {FormComponent, EntityToFormMapper} from '@molgenis/molgenis-ui-form'
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
        alert: null,
        showForm: false,
        settingLabel: ''
      }
    },
    watch: {
      selectedSetting: function (setting) {
        if (setting) {
          this.showForm = false
          api.get('/api/v2/' + setting).then(this.initializeForm, this.handleError)
        }
      }
    },
    methods: {
      onSubmit (formData) {
        const options = {
          body: JSON.stringify(formData)
        }
        const uri = '/api/v1/' + this.selectedSetting + '/' + formData.id + '?_method=PUT'
        api.post(uri, options).then(this.handleSuccess, this.handleError)
      },
      clearAlert () {
        this.alert = null
      },
      handleError (message) {
        this.alert = {
          message: message,
          type: 'danger'
        }
      },
      handleSuccess () {
        this.alert = {
          message: 'Settings saved',
          type: 'success'
        }
      },
      initializeSettingsOptions (response) {
        this.settingsOptions = response.items
        this.selectedSetting = this.settingsOptions[0].id
      },
      initializeForm (response) {
        const mappedData = EntityToFormMapper.generateForm(response.meta, response.items[0])
        this.state = {}
        this.formFields = mappedData.formFields
        this.formData = mappedData.formData
        this.settingLabel = response.meta.label
        this.showForm = true
      }
    },
    created: function () {
      api.get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==sys_set')
        .then(this.initializeSettingsOptions, this.handleError)
    },
    components: {
      FormComponent
    }
  }
</script>
