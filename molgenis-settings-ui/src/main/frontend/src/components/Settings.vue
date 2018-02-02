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
        <v-select v-model="selectedSetting" :options="settings" :filterable="true"></v-select>
      </div>
      <div class="card-body">
        <div class="card-block">
          <div v-if="initialFormData">
            <form-component id="settings-form" :formState="state" :schema="initFormSchema"
                            :initialFormData="initialFormData"></form-component>
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
  import vSelect from 'vue-select'

  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import '../../node_modules/@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'

  import { UPDATE_SETTINGS, GET_SETTINGS_BY_ID } from '../store/actions'
  import { SET_FORM_DATA, SET_ALERT } from '../store/mutations'

  export default {
    name: 'Settings',
    data () {
      return {
        state: {}
      }
    },
    method: {
      onSubmit: (formData) => {
        this.$store.commit(SET_FORM_DATA, formData)
        this.$store.dispatch(UPDATE_SETTINGS, this.$store.state.selectedSetting)
      },
      clearAlert: () => {
        this.$store.commit(SET_ALERT, null)
      }
    },
    computed: {
      initialFormData () {
        return this.$store.state.formData
      },
      initFormSchema () {
        return {fields: this.$store.state.formFields}
      },
      alert () {
        return this.$store.state.alert
      },
      settings () {
        return this.$store.state.settings
      },
      selectedSetting: {
        get () {
          return this.$store.state.selectedSetting
        },
        set (selectedSetting) {
          this.$store.dispatch(GET_SETTINGS_BY_ID, selectedSetting.id)
        }
      }
    },
    created: function () {
      this.$store.dispatch(GET_SETTINGS_BY_ID, this.$store.state.selectedSetting)
    },
    components: {
      FormComponent,
      vSelect
    }
  }
</script>
