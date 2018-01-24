<template>
  <div class="card">
    <div class="card-header">
      <h5>{{ 'plugin-title' | i18n }}</h5>
    </div>
    <div class="card-body">
      <div id="alert-message" v-if="message" class="alert" :class="error ? 'alert-danger' : 'alert-info'"
           role="alert">
        <button @click="message=null" type="button" class="close"><span aria-hidden="true">&times;</span>
        </button>
        <span id="message-span">{{message}}</span>
      </div>
      <div class="card-block">
        <entity-select-component v-model.lazy="selectedEntity" :id="'settings-select'" :label="'Select entity'"
                                 :description="'Select setting you want to edit'"
                                 :entities="initSettingsOptions"></entity-select-component>
      </div>
      <div class="card-block">
        <div v-if="createForm">
          <form-component id="settings-form" :schema="initFormSchema" :initialFormData="initialFormData"
                          :hooks="hooks"></form-component>
        </div>
      </div>
    </div>
    <div class="card-footer">
      <button id="save-btn" class="btn btn-primary" type="submit" form="settings-form">Save</button>
      <button id="cancel-btn" class="btn btn-secondary" type="reset" form="settings-form">Cancel</button>
    </div>

  </div>
</template>

<script>
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import EntitySelectComponent from '../components/EntitySelectComponent'
  import { GET_SETTINGS, GET_SETTINGS_BY_ID, UPDATE_SETTINGS } from '../store/actions'
  import { SET_FORM_DATA } from '../store/mutations'

  export default {
    name: 'Settings',
    components: {
      FormComponent,
      EntitySelectComponent
    },
    created: function () {
      this.$store.dispatch(GET_SETTINGS).then(() => {
        this.$store.dispatch(GET_SETTINGS_BY_ID, 'sys_set_app').then(() => {
          this.createForm = true
        })
      })
    },
    data () {
      return {
        hooks: {
          onSubmit: (formData) => {
            this.$store.commit(SET_FORM_DATA, formData)
            this.$store.dispatch(UPDATE_SETTINGS, this.selectedEntity)
            this.message = 'Changes saved'
          },
          onCancel: () => {
            this.message = 'onCancel'
          },
          onValueChanged: (formData) => {
            console.log('value changed')
          }
        },
        message: null,
        error: null,
        selectedEntity: null,
        createForm: false
      }
    },
    watch: {
      selectedEntity (updatedValue) {
        this.createForm = false
        this.$store.dispatch(GET_SETTINGS_BY_ID, updatedValue).then(() => {
          this.createForm = true
        })
      }
    },
    computed: {
      initialFormData () {
        return this.$store.state.formData
      },
      initFormSchema () {
        return {fields: this.$store.state.formFields}
      },
      initSettingsOptions () {
        return this.$store.state.settings
      }
    }
  }
</script>
