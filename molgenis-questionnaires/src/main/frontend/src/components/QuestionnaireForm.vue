<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-md-8">

        <router-link class="btn btn-outline-secondary my-3" to="/">
          <i class="fa fa-chevron-left"></i> {{ 'questionnaire_back_button' | i18n }}
        </router-link>

      </div>
    </div>

    <!-- Loading spinner -->
    <template v-if="loading">
      <div class="spinner-container d-flex justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-5x"></i>
      </div>
    </template>

    <template v-else>
      <div class="row">

        <!-- Form card -->
        <div class="col-xs-12 col-sm-12 col-md-10 col-lg-10 col-xl-10">
          <h1>{{ questionnaire.label }}</h1>
          <p v-html="questionnaire.description"></p>

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

              <button type="submit" class="btn btn-primary" :form="questionnaire.name">
                {{ 'questionnaire_submit' | i18n }}
              </button>
            </div>
          </div>
        </div>

        <!-- Compound chapters -->
        <div class="d-none d-md-block d-lg-block d-xl-block col-2">
          <ul class="list-group chapter-navigation-list">
            <li class="list-group-item disabled">Chapters</li>
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
  import { EntityToStateMapper, FormComponent } from '@molgenis/molgenis-ui-form'
  import api from '@molgenis/molgenis-api-client'

  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'questionnaire-form',
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
        questionnaire: null,
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
        const idAttribute = this.questionnaire.idAttribute
        const idValue = this.entity[idAttribute]
        const options = {
          body: JSON.stringify(formData)
        }

        api.post('/api/v1/' + this.questionnaire.name + '/' + idValue + '?_method=PUT', options)
      }
    },
    computed: {

      /**
       * Determine chapters based on compounds
       */
      chapters () {
        return this.questionnaire.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')
      }
    },
    created () {
      // Retrieve questionnaire via questionnaire API first to set status to OPEN
      api.get('/menu/plugins/questionnaires/' + this.questionnaireName).then(response => {
        response.json().then(data => {
          api.get('/api/v2/' + data.name).then(response => {
            this.questionnaire = response.meta
            this.entity = response.items.length > 0 ? response.items[0] : {}

            this.schema.fields = EntityToStateMapper.generateFormFields(this.questionnaire)
            this.formData = EntityToStateMapper.generateFormData(this.schema.fields, this.entity)
            this.loading = false
          })
        })
      })
    },
    components: {
      FormComponent
    }
  }
</script>
