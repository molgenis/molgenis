<template>
  <div>
    <!-- Loading spinner -->
    <template v-if="loading">
      <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-2x my-2"></i>
        <p>Initializing questionnaire...</p>
      </div>
    </template>

    <template v-else>
      <div class="row">

        <div class="col-lg-8 col-md-12">
          <h1>{{ questionnaireLabel }}</h1>
          <p v-html="questionnaireDescription"></p>

          <router-link class="btn btn-lg btn-primary mt-2" :to="'/' + questionnaireName + '/chapter/1'">
            {{ 'questionnaire_start' | i18n }}
          </router-link>
        </div>

      </div>
    </template>

  </div>
</template>

<script>
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
        formData: this.$store.state.formData,
        chapterFields: []
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
    computed: {
      questionnaireLabel () {
        return this.$store.state.questionnaireLabel
      },

      questionnaireDescription () {
        return this.$store.state.questionnaireDescription
      }
    },
    created () {
      this.$store.dispatch('GET_QUESTIONNAIRE', this.questionnaireName).then(() => {
        this.loading = false
      })
    }
  }
</script>
