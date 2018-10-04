<template>
  <b-modal id="packageUpdateModal"
           ref="packageUpdateModal"
           :title="$t('update-package-title')"
           :ok-title="$t('update-package-ok-text')"
           :cancel-title="$t('update-package-cancel-text')"
           @ok="handleOk"
           @shown="resetForm">
    <b-form @submit.stop.prevent="handleSubmit" :validated="validated">
      <b-form-group id="packageLabelInputGroup"
                    :label="$t('package-input-label') + ' *'"
                    label-for="packageNameInput"
                    :invalid-feedback="$t('package-input-label-invalid')">
        <b-form-input ref="updatePackageLabelInput" id="packageLabelInput"
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
    UPDATE_PACKAGE
  } from '../store/actions'
  import { mapState } from 'vuex'

  export default {
    name: 'NavigatorModalPackageUpdate',
    data () {
      return {
        form: {
          label: '',
          description: ''
        },
        validated: false
      }
    },
    computed: {
      ...mapState(['packages', 'selectedPackageIds']),
      selectedPackage () {
        var selectedPackageId = this.selectedPackageIds[0]
        return this.packages.find(aPackage => aPackage.id === selectedPackageId)
      }
    },
    methods: {
      resetForm () {
        this.form = Object.assign({}, {label: this.selectedPackage.label, description: this.selectedPackage.description})
        this.validated = false
        this.$refs.updatePackageLabelInput.$el.focus()
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
        var aPackage = Object.assign({}, this.form, {id: this.selectedPackage.id, parent: this.selectedPackage.parent.id})
        this.$store.dispatch(UPDATE_PACKAGE, aPackage)
        this.$refs.packageUpdateModal.hide()
      }
    }
  }
</script>
