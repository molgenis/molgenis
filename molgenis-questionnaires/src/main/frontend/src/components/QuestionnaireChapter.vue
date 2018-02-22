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
          <h5 class="display-4">{{ questionnaireLabel }}</h5>
        </div>
      </div>

      <div class="row">
        <div class="col-9">

          <div class="card mb-2">
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

            <div v-if="showProgressBar" class="progress mt-0 pt-0">
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
                :initialFormData="formData"
                :options="options"
                @valueChange="onValueChanged">
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

        <div class="col-3">
          <chapter-list
            :questionnaireId="questionnaireId"
            :changesMade="changesMade"
            :saving="saving">
          </chapter-list>
        </div>

      </div>
    </template>

  </div>
</template>

<script>
  import ChapterList from './ChapterList'

  import { debounce } from 'lodash'
  import { FormComponent } from '@molgenis/molgenis-ui-form'
  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'QuestionnaireChapter',
    props: ['questionnaireId', 'chapterId'],
    data () {
      return {
        loading: true,
        saving: false,
        changesMade: false,
        formState: {},
        formData: this.$store.state.formData,
        options: {
          showEyeButton: false
        }
      }
    },
    methods: {

      /**
       * Run the auto save action with the updated form data
       * debounce for 2 seconds so not every key press triggers a server call
       */
      onValueChanged (formData) {
        this.saving = true
        this.changesMade = true
        this.autoSave(formData)
      },

      autoSave: debounce(function (formData) {
        this.$store.dispatch('AUTO_SAVE_QUESTIONNAIRE', formData).then(() => {
          this.saving = false
        })
      }, 2000),

      /**
       * Submit the questionnaire
       */
      submitQuestionnaire () {
        this.$store.dispatch('SUBMIT_QUESTIONNAIRE', this.formData).then(() => {
          this.$router.push('/' + this.questionnaireId + '/thanks')
        })
      }
    },
    computed: {
      /**
       * Determine the number for the next chapter
       *
       * @return {number} number of the next chapter
       */
      nextChapterNumber () {
        return parseInt(this.chapterId) + 1
      },

      /**
       * Determine the number for the previous chapter
       *
       * @return {number} number for the previous chapter
       */
      previousChapterNumber () {
        return parseInt(this.chapterId) - 1
      },

      /**
       * Determine whether to show the next button
       *
       * @return {boolean} show next button
       */
      showNextButton () {
        return parseInt(this.chapterId) < this.totalNumberOfChapters
      },

      /**
       * Determine whether to show the previous button
       *
       * @return {boolean} show previous button
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
       * @return {number} progress percentage
       */
      progressPercentage () {
        return (parseInt(this.chapterId) / this.totalNumberOfChapters) * 100
      },

      /**
       * Show the progress bar if there is more then 1 chapter
       * @return {boolean} Whether to show the progress bar
       */
      showProgressBar () {
        return this.totalNumberOfChapters > 1
      },

      /**
       * Get the current chapter field schema from the store
       *
       * @return {Array<Object>} A field schema for the current chapter wrapped in an array
       */
      chapterField () {
        return this.$store.getters.getChapterByIndex(this.chapterId)
      },

      /**
       * Get the label of the questionnaire from the store
       * @return {string} The label of the questionnaire
       */
      questionnaireLabel () {
        return this.$store.state.questionnaireLabel
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
      FormComponent,
      ChapterList
    }
  }
</script>
