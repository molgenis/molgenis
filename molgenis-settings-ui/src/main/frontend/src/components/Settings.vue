<template>
  <div class="container">
    <div class="card">
      <div class="card-header">
        <entity-select-component></entity-select-component>
      </div>
      <div class="card-body">
        <div id="alert-message" v-if="message" class="alert" :class="error ? 'alert-danger' : 'alert-info'"
             role="alert">
          <button @click="message=null" type="button" class="close"><span aria-hidden="true">&times;</span>
          </button>
          <span id="message-span">{{message}}</span>
        </div>
        <div class="card-block">
          <div v-if="initialFormData">
            <form-component id="settings-form" :schema="initFormSchema" :initialFormData="initialFormData"
                            :hooks="hooks"></form-component>
          </div>
        </div>
      </div>
      <div class="card-footer">
        <button id="save-btn" class="btn btn-primary" type="submit" form="settings-form">Save</button>
      </div>

    </div>
  </div>
</template>

<script>
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import EntitySelectComponent from '../components/EntitySelectComponent'
  import { GET_SETTINGS, UPDATE_SETTINGS } from '../store/actions'
  import { SET_FORM_DATA } from '../store/mutations'

  import '../../node_modules/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'
  import 'font-awesome/css/font-awesome.css'

  export default {
    name: 'Settings',
    components: {
      FormComponent,
      EntitySelectComponent
    },
    created: function () {
      this.$store.dispatch(GET_SETTINGS).then(() => {
        this.createForm = true
      })
    },
    data () {
      return {
        hooks: {
          onSubmit: (formData) => {
            this.$store.commit(SET_FORM_DATA, formData)
            this.$store.dispatch(UPDATE_SETTINGS, this.$store.state.selectedSetting)
            this.message = 'Changes saved'
          }
        },
        message: null,
        error: null,
        createForm: false
      }
    },
    computed: {
      initialFormData () {
        return this.$store.state.formData
      },
      initFormSchema () {
        return {fields: this.$store.state.formFields}
      }
    }
  }
</script>
