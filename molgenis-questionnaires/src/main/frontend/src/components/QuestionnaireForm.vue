<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-md-8">

        <router-link class="btn btn-outline-secondary my-3" to="/">
          <i class="fa fa-chevron-left"></i> {{ 'questionnaire_back_button' | i18n }}
        </router-link>

      </div>
    </div>

    <template v-if="!loading">
      <div class="row">
        <div class="col-md-10">

          <h1>{{ questionnaire.label }}</h1>
          <p v-html="questionnaire.description"></p>

          <div class="card mb-3">
            <div class="card-body">
              <form-component
                v-if="!loading && schema.fields.length > 0"
                :id=questionnaire.id
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

        <div class="col-md-2">
          <ul class="list-group chapter-navigation-list">
            <li class="list-group-item chapter-item" v-for="chapter in topLevelChapters"
                @click="scrollInToView(chapter.name)">
              <a>{{ chapter.label }}</a>
            </li>
          </ul>
        </div>
      </div>
    </template>

    <template v-else>
      <i class="fa fa-spinner fa-spin fa-5x"></i>
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
</style>

<script>
  import { EntityToStateMapper, FormComponent } from '@molgenis/molgenis-ui-form'
  import api from '@molgenis/molgenis-api-client'

  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'questionnaire-form',
    props: {
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
        }
      }
    },
    methods: {
      scrollInToView (elementId) {
        // Forms generate fieldsets with ID 'element-id-fs'
        const element = document.getElementById(elementId + '-fs')
        element.scrollIntoView()
      },
      onValueChanged (formData) {
//        const idAttribute = this.questionnaire.idAttribute
//        const idValue = this.entity[idAttribute]
//        const options = {
//          body: JSON.stringify(formData)
//        }

        console.log(formData)

//        api.post('/api/v1/' + this.questionnaire.name + '/' + idValue + '?_method=PUT', options).then(response => {
//          console.log(response)
//        })
      }
    },
    computed: {
      topLevelChapters () {
        return this.questionnaire.attributes.filter(attribute => attribute.fieldType === 'COMPOUND')
      },
      formData () {
        if (this.schema.fields.length > 0 && this.entity !== null) {
          return EntityToStateMapper.generateFormData(this.schema.fields, this.entity)
        }
      }
    },
    created () {
      api.get('/api/v2/' + this.questionnaireName).then(response => {
        this.questionnaire = response.meta
        this.entity = response.items.length > 0 ? response.items[0] : {}

        this.schema.fields = EntityToStateMapper.generateFormFields(this.questionnaire)
        this.loading = false
      })
    },
    components: {
      FormComponent
    }
  }
</script>
