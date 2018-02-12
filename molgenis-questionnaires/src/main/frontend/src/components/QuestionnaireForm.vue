<template>
  <div>
    <!-- Loading spinner -->
    <template v-if="loading">
      <div class="spinner-container d-flex justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-5x"></i>
      </div>
    </template>

    <template v-else>
      <div class="row">

        <div class="col-xs-12 col-sm-12 col-md-10 col-lg-10 col-xl-10">
          <h1>{{ questionnaire.label }}</h1>
          <p v-html="questionnaire.description"></p>

          <!-- Form card -->
          <div class="card mb-3">
            <div class="card-body">
              <form-component
                v-if="!loading && questionnaire"
                :id="questionnaire.name"
                :schema="schema"
                :formState="formState"
                :formData="formData"
                :onValueChanged="onValueChanged">
              </form-component>
            </div>

            <div class="card-footer text-right">
              <router-link to="/" class="btn btn-outline-secondary">
                {{ 'questionnaire_save_and_continue' | i18n }}
              </router-link>

              <button type="submit" class="btn btn-primary" @click="onSubmit">
                {{ 'questionnaire_submit' | i18n }}
              </button>
            </div>
          </div>
        </div>

        <!-- Compound chapters -->
        <div class="d-none d-md-block d-lg-block d-xl-block col-2">
          <ul class="list-group chapter-navigation-list">
            <li class="list-group-item disabled">{{ 'questionnaire_chapter_title' | i18n }}</li>
            <li class="list-group-item chapter-item" v-for="chapter in chapters"
                @click="scrollInToView(chapter.name)">
              <a>{{ chapter.label }}</a>
            </li>
          </ul>
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
  import { EntityToFormMapper, FormComponent } from '@molgenis/molgenis-ui-form'
  import api from '@molgenis/molgenis-api-client'

  import moment from 'moment'

  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'QuestionnaireForm',
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
        entity: null,
        formState: {},
        schema: {
          fields: []
        },
        formData: {}
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
       * Auto save
       * @param formData
       */
      onValueChanged (formData) {
        const options = {
          body: JSON.stringify(formData)
        }

        api.post('/api/v1/' + this.questionnaire.name + '/' + this.questionnaireID + '?_method=PUT', options)
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

      /**
       * Computes the ID of the questionnaire belonging to the current user
       */
      questionnaireID () {
        return this.entity[this.questionnaire.idAttribute]
      },

      /**
       * Determine chapters based on compounds
       */
      chapters () {
        return this.questionnaire.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')
      }
    },
    created () {
      api.get('/api/v2/' + this.questionnaireName).then(response => {
        this.questionnaire = response.meta
        this.entity = response.items.length > 0 ? response.items[0] : {}

        const form = EntityToFormMapper.generateForm(this.questionnaire, this.entity)
        this.schema.fields = form.formFields
        this.formData = form.formData
        this.loading = false
      })
    },
    components: {
      FormComponent
    }
  }
</script>
