<template>
  <div class="row">
    <div class="col-xs-12 col-sm-12 col-md-10 col-lg-10 col-xl-10">

      <form-component
        :id="questionnaireName"
        :formFields="chapterFields"
        :formState="formState"
        :formData="formData"
        :onValueChanged="onValueChanged">
      </form-component>

      <router-link to="/" class="btn btn-outline-secondary">
        {{ 'questionnaire_save_and_continue' | i18n }}
      </router-link>

      <!--<button type="submit" class="btn btn-primary" @click="onSubmit">-->
        <!--{{ 'questionnaire_submit' | i18n }}-->
      <!--</button>-->

    </div>
  </div>
</template>

<script>
  import api from '@molgenis/molgenis-api-client'
  import { FormComponent } from '@molgenis/molgenis-ui-form'

  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'QuestionnaireChapter',
    props: {
      chapterFields: {
        type: Array,
        required: true
      },
      formData: {
        type: Object,
        required: true
      }
    },
    data () {
      return {
        formState: {}
      }
    },
    methods: {
      /**
       * Auto save
       * @param formData
       */
      onValueChanged (formData) {
        const options = {
          body: JSON.stringify(formData)
        }

        api.post('/api/v1/' + this.questionnaire.name + '/' + this.questionnaireID + '?_method=PUT', options)
      }
    },
    components: {
      FormComponent
    }
  }
</script>
