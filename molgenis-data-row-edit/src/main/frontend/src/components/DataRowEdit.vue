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

    <div v-if="showForm">
      <h2>{{dataTableLabel}}</h2>
      <form-component
        id="settings-form"
        :formFields="formFields"
        :initialFormData="formData"
        :formState="formState"
        @valueChange="onValueChanged">
      </form-component>
    </div>
    <div v-else class=""><i class="fa fa-spinner fa-spin fa-3x"></i></div>

    <div class="row">
          <div class="col-md-12">
            <a
              id="cancel-btn"
              v-bind:href="dataExplorerBaseUrl"
              class="btn btn-secondary">
              Cancel
            </a>

            <button
              v-if="!isSaving"
              id="save-btn"
              class="btn btn-primary"
              type="submit"
              @click.prevent="onSubmit"
              :disabled="formState.$pristine || formState.$invalid">
              Save
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

</template>

<script>
  import { FormComponent, EntityToFormMapper } from '@molgenis/molgenis-ui-form'
  import '../../node_modules/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'
  import api from '@molgenis/molgenis-api-client'

  export default {
    name: 'DataRowEdit',
    data () {
      return {
        dataExplorerBaseUrl: window.__INITIAL_STATE__.dataExplorerBaseUrl,
        dataTableLabel: '',
        dataTableId: 'sys_Language',
        dataRowId: 'en',
        formFields: [],
        formData: {},
        formState: {},
        alert: null,
        showForm: false,
        isSaving: false
      }
    },
    methods: {
      onValueChanged (updatedFormData) {
        this.formData = updatedFormData
      },
      onSubmit () {
        this.isSaving = true
        const options = {
          body: JSON.stringify(this.formData)
        }
        const uri = '/api/v1/' + this.dataTableId + '/' + this.dataRowId + '?_method=PUT'
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
      initializeForm (response) {
        // noinspection JSUnusedLocalSymbols
        const { _meta, _href, ...rowData } = response
        this.dataTableLabel = _meta.label
        const mappedData = EntityToFormMapper.generateForm(_meta, rowData)
        this.formFields = mappedData.formFields
        this.formData = mappedData.formData
        this.showForm = true
      }
    },
    created: function () {
      this.dataTableId = this.$route.params.dataTableId
      this.dataRowId = this.$route.params.rowId
      api.get('/api/v2/' + this.dataTableId + '/' + this.dataRowId).then(this.initializeForm, this.handleError)
    },
    components: {
      FormComponent
    }
  }
</script>
