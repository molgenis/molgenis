<template>
  <div>
    <template v-if="loading">
      <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-2x my-2"></i>
        <p>Loading chapters...</p>
      </div>
    </template>

    <template v-else>
      <div class="row">
        <div class="col-12">

          <div class="card my-2">
            <div class="card-header text-center">
              <router-link v-show="showPreviousButton"
                           :to="'/' + questionnaireName + '/chapter/' + previousChapterNumber"
                           class="btn btn-outline-secondary float-left">
                {{ 'questionnaire_previous_chapter' | i18n }}
              </router-link>

              <span class="chapter-progress-container text-muted align-middle">
                  Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
              </span>

              <router-link v-show="showNextButton" :to="'/' + questionnaireName + '/chapter/' + nextChapterNumber"
                           class="btn btn-primary float-right">
                {{ 'questionnaire_next_chapter' | i18n }}
              </router-link>
            </div>

            <div class="card-body">
              <form-component
                :id="questionnaireName"
                :formFields="chapterField"
                :formState="formState"
                :formData="formData"
                :options="options"
                :onValueChanged="onValueChanged">
              </form-component>
            </div>


            <div class="card-footer text-center">
              <router-link v-show="showPreviousButton"
                           :to="'/' + questionnaireName + '/chapter/' + previousChapterNumber"
                           class="btn btn-outline-secondary float-left">
                {{ 'questionnaire_previous_chapter' | i18n }}
              </router-link>

              <span class="chapter-progress-container text-muted align-middle">
                Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
              </span>

              <router-link v-show="showNextButton" :to="'/' + questionnaireName + '/chapter/' + nextChapterNumber"
                           class="btn btn-primary float-right">
                {{ 'questionnaire_next_chapter' | i18n }}
              </router-link>
            </div>
          </div>
        </div>
      </div>
    </template>
  </div>
</template>

<script>
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import api from '@molgenis/molgenis-api-client'

  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'QuestionnaireChapter',
    props: ['questionnaireName', 'chapterId'],
    data () {
      return {
        loading: true,
        formData: this.$store.state.formData,
        formState: {},
        options: {
          showEyeButton: false
        }
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

        api.post('/api/v1/' + this.questionnaireName + '/' + this.$store.state.questionnaireRowId + '?_method=PUT', options)
      }
    },
    computed: {
      nextChapterNumber () {
        return parseInt(this.chapterId) + 1
      },

      showPreviousButton () {
        return parseInt(this.chapterId) > 1
      },

      totalNumberOfChapters () {
        return this.$store.getters.getTotalNumberOfChapters
      },

      showNextButton () {
        return parseInt(this.chapterId) <= this.totalNumberOfChapters
      },

      previousChapterNumber () {
        return parseInt(this.chapterId) - 1
      },

      chapterField () {
        return this.$store.getters.getChapterByIndex(this.chapterId)
      }
    },
    created () {
      if (this.$store.state.chapterFields.length === 0) {
        this.$store.dispatch('GET_QUESTIONNAIRE', this.questionnaireName).then(() => {
          this.loading = false
        })
      } else {
        this.loading = false
      }
    },
    components: {
      FormComponent
    }
  }
</script>
