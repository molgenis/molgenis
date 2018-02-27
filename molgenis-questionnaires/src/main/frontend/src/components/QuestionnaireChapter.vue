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

                <!-- Shows previous / back to start button based on chapter progress -->
                <div class="col-4">
                  <button
                    type="button"
                    v-if="showPreviousButton"
                    @click="navigateToChapter(previousChapterNumber)"
                    class="btn btn-outline-secondary float-left">
                    {{ 'questionnaire_previous_chapter' | i18n }}
                  </button>

                  <router-link
                    v-show="!showPreviousButton"
                    :to="'/' + questionnaireId"
                    class="btn btn-outline-secondary float-left">
                    {{ 'questionnaire_back_to_start' | i18n }}
                  </router-link>
                </div>

                <!-- Shows current chapter and total number of chapters -->
                <div class="col-4 text-muted d-flex flex-column justify-content-center align-items-center">
                  Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
                </div>

                <!-- Shows next / submit button based on chapter progress -->
                <div class="col-4">
                  <button
                    type="button"
                    v-if="showNextButton"
                    @click="navigateToChapter(nextChapterNumber)"
                    class="btn btn-primary float-right">
                    {{ 'questionnaire_next_chapter' | i18n }}
                  </button>

                  <button class="btn btn-primary float-right" @click="submitQuestionnaire" v-else>
                    {{ 'questionnaire_submit' | i18n }}
                  </button>
                </div>

              </div>
            </div>

            <!-- Progress bar container -->
            <div v-if="showProgressBar" class="progress mt-0 pt-0">
              <div class="progress-bar" role="progressbar"
                   :style="'width:' + progressPercentage + '%;'"
                   :aria-valuenow="chapterId" aria-valuemin="1"
                   :aria-valuemax="totalNumberOfChapters">
              </div>
            </div>

            <!-- Error message container -->
            <div v-if="navigationBlocked" class="error-message-container">
              <div class="alert alert-warning" role="alert">
                {{ 'chapter_incomplete_message' | i18n }}
              </div>
            </div>

            <!-- Form Container -->
            <div class="card-body">
              <div v-if="showForm">
                <form-component
                  :id="questionnaireId"
                  :formFields="currentChapter"
                  :formState="formState"
                  :initialFormData="formData"
                  :options="options"
                  @valueChange="onValueChanged">
                </form-component>
              </div>

              <div class="spinner-container text-muted d-flex flex-column justify-content-center align-items-center"
                   v-else>
                <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
              </div>
            </div>

            <div class="card-footer">
              <div class="row">

                <!-- Shows previous button based on chapter progress -->
                <div class="col-6">
                  <button
                    type="button"
                    v-if="showPreviousButton"
                    @click="navigateToChapter(previousChapterNumber)"
                    class="btn btn-outline-secondary float-left">
                    {{ 'questionnaire_previous_chapter' | i18n }}
                  </button>
                </div>

                <!-- Shows next / submit button based on chapter progress -->
                <div class="col-6">
                  <button
                    type="button"
                    v-if="showNextButton"
                    @click="navigateToChapter(nextChapterNumber)"
                    class="btn btn-primary float-right">
                    {{ 'questionnaire_next_chapter' | i18n }}
                  </button>

                  <button class="btn btn-primary float-right" @click="submitQuestionnaire" v-else>
                    {{ 'questionnaire_submit' | i18n }}
                  </button>
                </div>

              </div>
            </div>
          </div>
        </div>

        <!-- Renders chapter navigation list -->
        <div class="col-3">
          <chapter-list
            :questionnaireId="questionnaireId"
            :currentChapterId="chapterId"
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
    props: {
      chapterId: {
        type: Number
      },
      questionnaireId: {
        type: String
      }
    },
    data () {
      return {
        changesMade: false,
        formState: {},
        loading: true,
        navigationBlocked: false,
        options: {
          showEyeButton: false
        },
        saving: false,
        showForm: true
      }
    },
    methods: {
      onValueChanged (formData) {
        this.$store.commit('SET_FORM_DATA', formData)
      },

      /**
       * Forces validation if a user wants to go to the next chapter
       * Triggers client side validation by setting status to 'SUBMITTED'
       */
      navigateToChapter (chapterNumber) {
        this.$store.commit('UPDATE_FORM_STATUS', 'SUBMITTED')
        this.formState.$submitted = true

        this.$nextTick(() => {
          if (this.formState.$valid) {
            this.$store.commit('UPDATE_FORM_STATUS', 'OPEN')
            this.$router.push('/' + this.questionnaireId + '/chapter/' + chapterNumber)
          } else {
            this.navigationBlocked = true
          }
        })
      },

      /**
       * Redirect to thank you page when submit is succesfull
       */
      submitQuestionnaire () {
        this.$store.dispatch('SUBMIT_QUESTIONNAIRE', this.formData).then(() => {
          this.$router.push('/' + this.questionnaireId + '/thanks')
        })
      },

      /**
       * Debounce for 2 seconds so not every key press triggers a server call
       */
      autoSave: debounce(function (lastUpdated) {
        this.$store.dispatch('AUTO_SAVE_QUESTIONNAIRE', lastUpdated).then(() => {
          this.saving = false
        })
      }, 2000)
    },
    watch: {

      /**
       * Watch changes in formData to run the auto save action with ONLY the updated attribute
       */
      formData (newValue, oldValue) {
        this.saving = true
        this.changesMade = true

        const lastUpdatedAttribute = Object.keys(newValue).find(key => {
          return newValue[key] !== oldValue[key]
        })

        this.autoSave({
          'attribute': lastUpdatedAttribute,
          'value': newValue[lastUpdatedAttribute]
        })
      }
    },
    computed: {
      formData () {
        return this.$store.state.formData
      },
      nextChapterNumber () {
        return this.chapterId + 1
      },
      previousChapterNumber () {
        return this.chapterId - 1
      },
      showNextButton () {
        return this.chapterId < this.totalNumberOfChapters
      },
      showPreviousButton () {
        return this.chapterId > 1
      },
      totalNumberOfChapters () {
        return this.$store.getters.getTotalNumberOfChapters
      },

      /**
       * Calculate the percentage of progress based on the
       * current chapter and total number of chapters
       */
      progressPercentage () {
        return (this.chapterId / this.totalNumberOfChapters) * 100
      },
      showProgressBar () {
        return this.totalNumberOfChapters > 1
      },
      currentChapter () {
        return this.$store.getters.getChapterByIndex(this.chapterId)
      },
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
