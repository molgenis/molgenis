<template>
  <b-modal id="packageCreateModal"
           ref="packageCreateModal"
           :title="$t('create-package-title')"
           :ok-title="$t('create-package-ok-text')"
           :cancel-title="$t('create-package-cancel-text')"
           @ok="handleOk"
           @shown="resetForm">
    <b-form @submit.stop.prevent="handleSubmit" :validated="validated">
      <b-form-group id="packageLabelInputGroup"
                    :label="$t('package-input-label') + ' *'"
                    label-for="packageNameInput"
                    :invalid-feedback="$t('package-input-label-invalid')">
        <b-form-input ref="createPackageLabelInput" id="packageLabelInput"
                      type="text"
                      v-model="form.label"
                      required>
        </b-form-input>
      </b-form-group>
      <b-form-group id="packageDescriptionInputGroup"
                    :label="$t('package-input-description')"
                    label-for="packageDescriptionInput">
        <b-form-input id="packageDescriptionInput"
                      type="text"
                      v-model="form.description">
        </b-form-input>
      </b-form-group>
    </b-form>
  </b-modal>
</template>

<script>
  import {
    CREATE_PACKAGE
  } from '../store/actions'

  export default {
    name: 'NavigatorModalPackageCreate',
    data () {
      return {
        form: {
          label: '',
          description: ''
        },
        validated: false
      }
    },
    methods: {
      resetForm () {
        this.form = {
          label: '',
          description: ''
        }
        this.validated = false
        this.$refs.createPackageLabelInput.$el.focus()
      },
      handleOk (evt) {
        evt.preventDefault()
        if (!this.form.label) {
          this.validated = true
        } else {
          this.handleSubmit()
        }
      },
      handleSubmit () {
        var aPackage = Object.assign({}, this.form, {parent: this.$route.params.package})
        this.$store.dispatch(CREATE_PACKAGE, aPackage)
        this.$refs.packageCreateModal.hide()
      }
    }
  }
</script>
