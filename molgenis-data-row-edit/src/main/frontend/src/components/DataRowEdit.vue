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

      <h5>{{dataTableLabel}}</h5>

      <form-component
        id="data-row-edit-form"
        :formFields="formFields"
        :initialFormData="formData"
        :formState="formState"
        @valueChange="onValueChanged">
      </form-component>

      <div class="row">
        <div class="col-md-12">
          <button
            id="cancel-btn"
            @click.prevent="goBackToPluginCaller"
            class="btn btn-secondary">
            {{ 'data-row-edit-cancel-button-label' | i18n }}
          </button>

          <button
            v-if="!isSaving"
            id="save-btn"
            class="btn btn-primary"
            type="submit"
            @click.prevent="onSubmit"
            :disabled="formState.$pristine || formState.$invalid">
            {{ 'data-row-edit-save-button-label' | i18n }}
          </button>

          <button
            v-else
            id="save-btn-saving"
            class="btn btn-primary"
            type="button"
            disabled="disabled">
            {{ 'data-row-edit-save-busy-state-label' | i18n }} <i class="fa fa-spinner fa-spin "></i>
          </button>
        </div>
      </div>

    </div>
    <div v-else class=""><i class="fa fa-spinner fa-spin fa-3x"></i></div>

  </div>

</template>

<script>
  import { FormComponent, EntityToFormMapper } from '@molgenis/molgenis-ui-form'
  import '../../node_modules/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'
  import api from '@molgenis/molgenis-api-client'

  const entityMapperSettings = {
    showNonVisibleAttributes: true
  }

  export default {
    name: 'DataRowEdit',
    props: {
      dataTableId: {
        type: String,
        required: true
      },
      dataRowId: {
        type: String,
        required: false,
        default: null
      }
    },
    data () {
      return {
        dataExplorerBaseUrl: window.__INITIAL_STATE__.dataExplorerBaseUrl,
        dataTableLabel: '',
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
        const postDetails = this.dataRowId !== null ? this.dataTableId + '/' + this.dataRowId : this.dataTableId
        const uri = '/api/v1/' + postDetails + '?_method=PUT'
        api.post(uri, options).then(this.goBackToPluginCaller, this.handleError)
      },
      goBackToPluginCaller () {
        window.history.go(-1)
      },
      clearAlert () {
        this.alert = null
      },
      handleError (message) {
        this.alert = {
          message: typeof message !== 'string' ? this.$t('data-row-edit-default-error-message') : message,
          type: 'danger'
        }
        this.showForm = true
        this.isSaving = false
      },
      initializeForm (mappedData) {
        this.formFields = mappedData.formFields
        this.formData = mappedData.formData
        this.showForm = true
      },
      /**
       * response mixes data and metadata, this function creates a new object with separate properties for data and metadata
       * @param response
       * @returns {{_meta: *, rowData: *}}
       */
      parseEditResponse (response) {
        // noinspection JSUnusedLocalSymbols
        const { _meta, _href, ...rowData } = response
        return {_meta, rowData}
      }
    },
    created: function () {
      if (this.dataRowId !== null) {
        api.get('/api/v2/' + this.dataTableId + '/' + this.dataRowId).then((response) => {
          this.dataTableLabel = response._meta.label
          const { _meta, rowData } = this.parseEditResponse(response)
          const mappedData = EntityToFormMapper.generateForm(_meta, rowData, { mapperMode: 'UPDATE', ...entityMapperSettings })
          this.initializeForm(mappedData)
        }, this.handleError)
      } else {
        api.get('/api/v2/' + this.dataTableId + '?num=0').then((response) => {
          this.dataTableLabel = response.meta.label
          const mappedData = EntityToFormMapper.generateForm(response.meta, {}, { mapperMode: 'CREATE', ...entityMapperSettings })
          this.initializeForm(mappedData)
        }, this.handleError)
      }
    },
    components: {
      FormComponent
    }
  }
</script>
