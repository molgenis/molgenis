<template>
  <div>

    <template v-if="loading">
      <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center">
        <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
        <p>{{ 'questionnaire_loading_chapter_text' | i18n }} {{ chapterId }}...</p>
      </div>
    </template>

    <template v-else>
      <div class="row">
        <div class="col-12">

          <div class="card my-2">
            <div class="card-header">

              <div class="row">
                <div class="col-4">
                  <router-link v-show="showPreviousButton"
                               :to="'/' + questionnaireId + '/chapter/' + previousChapterNumber"
                               class="btn btn-outline-secondary float-left">
                    {{ 'questionnaire_previous_chapter' | i18n }}
                  </router-link>
                </div>

                <div class="col-4 text-muted d-flex flex-column justify-content-center align-items-center">
                  Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
                </div>

                <div class="col-4">
                  <router-link v-if="showNextButton" :to="'/' + questionnaireId + '/chapter/' + nextChapterNumber"
                               class="btn btn-primary float-right">
                    {{ 'questionnaire_next_chapter' | i18n }}
                  </router-link>

                  <button class="btn btn-primary float-right" @click="submitQuestionnaire" v-else>
                    {{ 'questionnaire_submit' | i18n }}
                  </button>
                </div>
              </div>

            </div>

            <div class="progress mt-0 pt-0">
              <div class="progress-bar" role="progressbar"
                   :style="'width:' + progressPercentage + '%;'"
                   :aria-valuenow="chapterId" aria-valuemin="1"
                   :aria-valuemax="totalNumberOfChapters"></div>
            </div>

            <div class="card-body">

              <form-component
                :id="questionnaireId"
                :formFields="chapterField"
                :formState="formState"
                :formData="formData"
                :options="options"
                :onValueChanged="onValueChanged">
              </form-component>
            </div>


            <div class="card-footer">

              <div class="row">
                <div class="col-4">
                  <router-link v-if="showPreviousButton"
                               :to="'/' + questionnaireId + '/chapter/' + previousChapterNumber"
                               class="btn btn-outline-secondary float-left">
                    {{ 'questionnaire_previous_chapter' | i18n }}
                  </router-link>
                </div>

                <div class="col-4 text-muted d-flex flex-column justify-content-center align-items-center">
                  Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
                </div>

                <div class="col-4">
                  <router-link v-if="showNextButton" :to="'/' + questionnaireId + '/chapter/' + nextChapterNumber"
                               class="btn btn-primary float-right">
                    {{ 'questionnaire_next_chapter' | i18n }}
                  </router-link>

                  <button class="btn btn-primary float-right" @click="submitQuestionnaire" v-else>
                    {{ 'questionnaire_submit' | i18n }}
                  </button>
                </div>
              </div>

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
    props: ['questionnaireId', 'chapterId'],
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
        console.log(formData)
        const options = {
          body: JSON.stringify({
            entities: [formData]
          }),
          method: 'PUT'
        }

        api.post('/api/v2/' + this.questionnaireId, options)
      },

      submitQuestionnaire () {
        console.log('submit')
      }
    },
    computed: {

      /**
       * Determine the number for the next chapter
       *
       * @return {number}
       */
      nextChapterNumber () {
        return parseInt(this.chapterId) + 1
      },

      /**
       * Determine the number for the previous chapter
       *
       * @return {number}
       */
      previousChapterNumber () {
        return parseInt(this.chapterId) - 1
      },

      /**
       * Determine whether to show the next button
       *
       * @return {boolean}
       */
      showNextButton () {
        return parseInt(this.chapterId) < this.totalNumberOfChapters
      },

      /**
       * Determine whether to show the previous button
       *
       * @return {boolean}
       */
      showPreviousButton () {
        return parseInt(this.chapterId) > 1
      },

      /**
       * Get the total number of chapters from the store
       *
       * @return {number} Total number of chapters
       */
      totalNumberOfChapters () {
        return this.$store.getters.getTotalNumberOfChapters
      },

      /**
       * Calculate the percentage of progress based on the
       * current chapter and total number of chapters
       *
       * @return {number}
       */
      progressPercentage () {
        const chapterIdNumber = parseInt(this.chapterId)
        return chapterIdNumber === 1 ? 0 : (chapterIdNumber / this.totalNumberOfChapters) * 100
      },

      /**
       * Get the current chapter field schema from the store
       *
       * @return {Array<Object>} A field schema for the current chapter wrapped in an array
       */
      chapterField () {
        return this.$store.getters.getChapterByIndex(this.chapterId)
      }
    },
    created () {
      if (this.$store.state.chapterFields.length === 0) {
        this.$store.dispatch('GET_QUESTIONNAIRE', this.questionnaireId).then(() => {
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
