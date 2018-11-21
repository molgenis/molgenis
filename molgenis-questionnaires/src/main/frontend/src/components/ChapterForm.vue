<template>
  <form-component
    :id="questionnaireId"
    :formFields="[currentChapter]"
    :formState="formState"
    :initialFormData="formData"
    :options="options"
    @valueChange="onValueChanged">
  </form-component>
</template>

<style>
  .questionnaire-chapter fieldset fieldset fieldset > small,
  .questionnaire-chapter fieldset fieldset fieldset > legend {
    font-size: 100%;
  }

  .questionnaire-chapter fieldset fieldset fieldset > legend {
    padding-top: 1rem;
  }
</style>

<script>
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import 'flatpickr/dist/flatpickr.css'
  import '@molgenis/molgenis-ui-form/dist/static/css/molgenis-ui-form.css'

  export default {
    name: 'ChapterForm',
    props: ['currentChapter', 'formState', 'questionnaireId'],
    data () {
      return {
        options: {
          showEyeButton: false
        }
      }
    },
    methods: {
      onValueChanged (formData) {
        this.$store.dispatch('AUTO_SAVE_QUESTIONNAIRE', {formData, formState: this.formState})
      }
    },
    computed: {
      formData () {
        return this.$store.state.formData
      }
    },
    components: {
      FormComponent
    }
  }
</script>
