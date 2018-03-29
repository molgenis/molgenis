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
          <option v-for="option in settingsOptions" :value="option.id">
            {{option.label}}
          </option>
        </select>

        <div class="float-right mt-2">

          <button
            v-if="!isSaving"
            id="save-btn-top"
            class="btn btn-primary"
            type="submit"
            @click.prevent="onSubmit"
            :disabled="formState.$pristine || formState.$invalid">
            Save changes
          </button>

          <button
            v-else
            id="save-btn-saving"
            class="btn btn-primary"
            type="button"
            disabled="disabled">
            Saving.... <i class="fa fa-spinner fa-spin "></i>
          </button>

        </div>

      </div>
    </div>

    <hr/>

    <div v-if="showForm">
      <h2>{{settingLabel}}</h2>
      <form-component
        id="settings-form"
        :formFields="formFields"
        :initialFormData="formData"
        :formState="formState"
        @valueChange="onValueChanged">
      </form-component>
    </div>
    <div v-else class=""><i class="fa fa-spinner fa-spin fa-3x"></i></div>

  </div>

</template>

<script>
  import { FormComponent, EntityToFormMapper } from '@molgenis/molgenis-ui-form'
  import '../../node_modules/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'
  import api from '@molgenis/molgenis-api-client'

  export default {
    name: 'SettingsUi',
    data () {
      return {
        selectedSetting: null,
        formFields: [],
        formData: {},
        formState: {},
        settingsOptions: [],
        alert: null,
        showForm: false,
        settingLabel: '',
        isSaving: false
      }
    },
    watch: {
      selectedSetting: function (setting) {
        this.showForm = false
        this.$router.push({path: `/${setting}`})
        api.get('/api/v2/' + setting).then(this.initializeForm, this.handleError)
      }
    },
    methods: {
      onValueChanged (formData) {
        this.formData = formData
      },

      onSubmit () {
        this.isSaving = true
        const options = {
          body: JSON.stringify(this.formData)
        }
        const uri = '/api/v1/' + this.selectedSetting + '/' + this.formData.id + '?_method=PUT'
        api.post(uri, options).then(this.handleSuccess, this.handleError)
      },
      clearAlert () {
        this.alert = null
      },
      handleError (message) {
        this.alert = {
          message: typeof message !== 'string' ? 'An error has occurred.' : message,
          type: 'danger'
        }
      },
      handleSuccess () {
        this.formState._reset()
        this.alert = {
          message: 'Settings saved',
          type: 'success'
        }
        this.isSaving = false
      },
      initializeSettingsOptions (response) {
        this.settingsOptions = response.items
      },
      initializeForm (response) {
        const mappedData = EntityToFormMapper.generateForm(response.meta, response.items[0])
        this.formFields = mappedData.formFields
        this.formData = mappedData.formData
        this.settingLabel = response.meta.label
        this.showForm = true
      }
    },
    created: function () {
      this.selectedSetting = this.$route.params.setting
      api.get('/api/v2/sys_md_EntityType?sort=label&num=1000&&q=isAbstract==false;package.id==sys_set')
        .then(this.initializeSettingsOptions, this.handleError)
    },
    components: {
      FormComponent
    }
  }
</script>
