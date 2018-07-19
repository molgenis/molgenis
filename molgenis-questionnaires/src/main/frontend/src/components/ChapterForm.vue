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
