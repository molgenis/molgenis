<template>
  <div>
    <!-- Loading spinner -->
    <template v-if="loading">
      <div class="spinner-container d-flex justify-content-center align-items-center text-muted">
        <i class="fa fa-spinner fa-spin fa-1x"> </i>&nbsp; {{ loadingMessage }}
      </div>
    </template>

    <template v-else>
      <div class="row">

        <div class="col-lg-8 col-md-12">
          <h1>{{ questionnaireLabel }}</h1>
          <p v-html="questionnaireDescription"></p>

          <router-link class="btn btn-lg btn-primary mt-2" to="/chapter_1">
            {{ 'questionnaire_start' | i18n }}
          </router-link>
        </div>

      </div>
    </template>

  </div>
</template>

<style>
  .chapter-item:hover {
    cursor: pointer;
    background-color: whitesmoke;
  }

  .chapter-navigation-list {
    background-color: #c0c0c0;
    position: fixed;
    top: 70px;
    z-index: 100;
  }

  .spinner-container {
    height: 80vh;
  }
</style>

<script>
  import { EntityToFormMapper } from '@molgenis/molgenis-ui-form'
  import api from '@molgenis/molgenis-api-client'

  import moment from 'moment'

  export default {
    name: 'QuestionnaireStart',
    props: {

      /**
       * The name of the questionnaire to load
       */
      questionnaireName: {
        type: String,
        required: true
      }
    },
    data () {
      return {
        loading: true,
        loadingMessage: '',
        entity: null,
        formState: {},
        formData: {},
        chapterFields: [],
        questionnaireLabel: '',
        questionnaireDescription: '',
        rowId: '',
        chapters: []
      }
    },
    methods: {

      /**
       * Click handler for compound chapters
       */
      scrollInToView (elementId) {
        const element = document.getElementById(elementId + '-fs') // Forms generate fieldsets with ID 'element-id-fs'
        element.scrollIntoView()
      },

      /**
       * On submit handler
       * 1) Set status to SUBMITTED
       * 2) Trigger form validation
       * 3) Post
       */
      onSubmit () {
        // Trigger submit
        this.formData.status = 'SUBMITTED'
        this.formState.$submitted = true
        this.formState._validate()

        // Check if form is valid
        if (this.formState.$valid) {
          // Generate submit timestamp
          this.formData.submitDate = moment().toISOString()
          const options = {
            body: JSON.stringify(this.formData)
          }

          // Submit to server and redirect to thank you page
          api.post('/api/v1/' + this.questionnaire.name + '/' + this.questionnaireID + '?_method=PUT', options).then(() => {
            this.$router.push({path: '/' + this.questionnaireName + '/thanks'})
          }).catch(error => {
            console.log('Something went wrong submitting the questionnaire', error)
          })
        } else {
          this.formData.status = 'OPEN'
          this.formState.$submitted = false
        }
      }
    },
    created () {
      this.loadingMessage = 'Initializing questionnaire...'
      api.get('/api/v2/' + this.questionnaireName).then(response => {
        this.loadingMessage = 'Building questionnaire data...'
        this.questionnaireLabel = response.meta.label
        this.questionnaireDescription = response.meta.description

        this.entity = response.items.length > 0 ? response.items[0] : {}

        // The ID of the questionnaire belonging to the current use
        this.rowId = this.entity[response.meta.idAttribute]

        this.chapters = response.meta.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')

        const form = EntityToFormMapper.generateForm(response.meta, this.entity)

        this.loadingMessage = 'Setup completed...'

        this.chapterFields = form.formFields.slice(1, 2)
        // const restFields = form.formFields.slice(2, form.formFields.length - 1)

        this.formData = form.formData
        this.loading = false
      })
    }
  }
</script>
