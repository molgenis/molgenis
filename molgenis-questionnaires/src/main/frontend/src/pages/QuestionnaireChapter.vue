<template>
  <div class="container-fluid">
    <div class="row">
      <div class="col-12 my-5">
        <!-- Loading spinner -->
        <template v-if="loading">
          <loading-spinner :message="$t('questionnaire_overview_loading_text')"></loading-spinner>
        </template>

        <!-- Error handler -->
        <template v-else-if="!loading && error">
          <questionnaire-error :error="error"></questionnaire-error>
        </template>

        <!-- Questionnaire chapters -->
        <template v-else>
          <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-9 col-xl-9">

              <div class="card mb-2">
                <div class="card-header">
                  <div class="row">

                    <!-- Shows previous / back to start button based on chapter progress -->
                    <div class="col-3 col-sm-4 col-md-4 col-lg-4 col-xl-4">
                      <button
                        type="button"
                        v-if="showPreviousButton"
                        @click="navigateToPreviousChapter"
                        class="btn btn-outline-secondary float-left">

                        <template v-if="changingChapters">
                          <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
                        </template>

                        <template v-else>
                          <span class="d-none d-md-block">{{ 'questionnaire_previous_chapter' | i18n }}</span>
                          <span class="d-md-none"><i class="fa fa-chevron-left"></i></span>
                        </template>
                      </button>

                      <router-link
                        v-show="!showPreviousButton"
                        :to="'/' + questionnaireId"
                        class="btn btn-outline-secondary float-left">
                        <span class="d-none d-md-block">{{ 'questionnaire_back_to_start' | i18n }}</span>
                        <span class="d-md-none"><i class="fa fa-chevron-left"></i></span>
                      </router-link>
                    </div>

                    <!-- Shows current chapter and total number of chapters -->
                    <div class="col-6 col-sm-4 col-md-4 col-lg-4 col-xl-4 text-muted d-flex flex-column justify-content-center align-items-center">
                      Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
                    </div>

                    <!-- Shows next / submit button based on chapter progress -->
                    <div class="col-3 col-sm-4 col-md-4 col-lg-4 col-xl-4">
                      <button
                        type="button"
                        v-if="showNextButton"
                        @click="navigateToNextChapter"
                        class="btn btn-primary float-right">

                        <template v-if="changingChapters">
                          <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
                        </template>

                        <template v-else>
                          <span class="d-none d-md-block">{{ 'questionnaire_next_chapter' | i18n }}</span>
                          <span class="d-md-none"><i class="fa fa-chevron-right"></i></span>
                        </template>
                      </button>

                      <button class="btn btn-primary float-right" @click="submitQuestionnaire" v-else>
                        <span class="d-none d-md-block">{{ 'questionnaire_submit' | i18n }}</span>
                        <span class="d-md-none"><i class="fa fa-check"></i></span>
                      </button>
                    </div>

                  </div>
                </div>

                <!-- Error message container -->
                <div v-if="navigationBlocked" class="error-message-container">
                  <div class="alert alert-warning mb-0" role="alert">
                    {{ 'questionnaire_chapter_incomplete_message' | i18n }}
                  </div>
                </div>

                <!-- Form Container -->
                <div class="card-body">
                  <form-component
                    :id="questionnaireId"
                    :formFields="currentChapter"
                    :formState="formState"
                    :initialFormData="formData"
                    :options="options"
                    @valueChange="onValueChanged">
                  </form-component>
                </div>

                <!-- Error message container -->
                <div v-if="navigationBlocked" class="error-message-container">
                  <div class="alert alert-warning mb-0" role="alert">
                    {{ 'questionnaire_chapter_incomplete_message' | i18n }}
                  </div>
                </div>

                <div class="card-footer">
                  <div class="row">

                    <!-- Shows previous / back to start button based on chapter progress -->
                    <div class="col-3 col-sm-4 col-md-4 col-lg-4 col-xl-4">
                      <button
                        type="button"
                        v-if="showPreviousButton"
                        @click="navigateToPreviousChapter"
                        class="btn btn-outline-secondary float-left">

                        <template v-if="changingChapters">
                          <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
                        </template>

                        <template v-else>
                          <span class="d-none d-md-block">{{ 'questionnaire_previous_chapter' | i18n }}</span>
                          <span class="d-md-none"><i class="fa fa-chevron-left"></i></span>
                        </template>
                      </button>

                      <router-link
                        v-show="!showPreviousButton"
                        :to="'/' + questionnaireId"
                        class="btn btn-outline-secondary float-left">
                        <span class="d-none d-md-block">{{ 'questionnaire_back_to_start' | i18n }}</span>
                        <span class="d-md-none"><i class="fa fa-chevron-left"></i></span>
                      </router-link>
                    </div>

                    <!-- Shows current chapter and total number of chapters -->
                    <div class="col-6 col-sm-4 col-md-4 col-lg-4 col-xl-4 text-muted d-flex flex-column justify-content-center align-items-center">
                      Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
                    </div>

                    <!-- Shows next / submit button based on chapter progress -->
                    <div class="col-3 col-sm-4 col-md-4 col-lg-4 col-xl-4">
                      <button
                        type="button"
                        v-if="showNextButton"
                        @click="navigateToNextChapter"
                        class="btn btn-primary float-right">

                        <template v-if="changingChapters">
                          <i class="fa fa-spinner fa-spin fa-3x my-3"></i>
                        </template>

                        <template v-else>
                          <span class="d-none d-md-block">{{ 'questionnaire_next_chapter' | i18n }}</span>
                          <span class="d-md-none"><i class="fa fa-chevron-right"></i></span>
                        </template>
                      </button>

                      <button class="btn btn-primary float-right" @click="submitQuestionnaire" v-else>
                        <span class="d-none d-md-block">{{ 'questionnaire_submit' | i18n }}</span>
                        <span class="d-md-none"><i class="fa fa-check"></i></span>
                      </button>
                    </div>

                  </div>
                </div>
              </div>
            </div>

            <!-- Renders chapter navigation list -->
            <div class="d-none d-lg-block col-3">
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
    </div>
  </div>
</template>

<script>
  import LoadingSpinner from '../components/LoadingSpinner'
  import QuestionnaireError from '../components/QuestionnaireError'
  import ChapterList from '../components/ChapterList'
  import moment from 'moment'

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
        changingChapters: false,
        changesMade: false,
        formState: {},
        navigationBlocked: false,
        options: {
          showEyeButton: false
        },
        saving: false
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
      navigateToNextChapter () {
        this.$store.commit('UPDATE_FORM_STATUS', 'SUBMITTED')
        this.formState.$submitted = true

        this.$nextTick(() => {
          if (this.formState.$valid) {
            this.$store.commit('UPDATE_FORM_STATUS', 'OPEN')
            this.navigationBlocked = false
            this.$router.push('/' + this.questionnaireId + '/chapter/' + this.nextChapterNumber)
          } else {
            this.navigationBlocked = true
          }
        })
      },

      navigateToPreviousChapter () {
        if (this.navigationBlocked) {
          this.$store.commit('UPDATE_FORM_STATUS', 'OPEN')
        }
        this.$router.push('/' + this.questionnaireId + '/chapter/' + this.previousChapterNumber)
      },

      /**
       * Redirect to thank you page when submit is successful
       */
      submitQuestionnaire () {
        this.$store.commit('UPDATE_FORM_STATUS', 'SUBMITTED')
        this.formState.$submitted = true

        this.$nextTick(() => {
          if (this.formState.$valid) {
            const submitDate = moment().toISOString()

            this.$store.dispatch('SUBMIT_QUESTIONNAIRE', submitDate).then(() => {
              this.navigationBlocked = false
              this.$router.push('/' + this.questionnaireId + '/submitted')
            })
          } else {
            this.navigationBlocked = true
          }
        })
      }
    },
    watch: {

      /**
       * Watch changes in formData to run the auto save action with ONLY the updated attribute
       * Will not trigger auto saves on id attributes, as these are often read-only
       */
      formData (newValue, oldValue) {
        const lastUpdatedAttribute = Object.keys(newValue).find(key => {
          return newValue[key] !== oldValue[key]
        })

        if (lastUpdatedAttribute !== this.$store.state.questionnaire.meta.idAttribute) {
          this.saving = true
          this.changesMade = true

          const lastUpdated = {
            'attribute': lastUpdatedAttribute,
            'value': newValue[lastUpdatedAttribute]
          }

          this.$store.dispatch('AUTO_SAVE_QUESTIONNAIRE', lastUpdated).then(() => {
            this.saving = false
          })
        }
      }
    },
    computed: {
      loading () {
        return this.$store.state.loading
      },
      error () {
        return this.$store.state.error
      },
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
       * Return the current chapter wrapped in an array to feed it to the form generator
       */
      currentChapter () {
        return [this.$store.getters.getChapterByIndex(this.chapterId)]
      }
    },
    created () {
      if (this.$store.state.chapterFields.length === 0) {
        this.$store.dispatch('GET_QUESTIONNAIRE', this.questionnaireId)

        if (!this.$store.state.mapperOptions.booleanLabels) {
          const mapperOptions = {
            booleanLabels: {
              trueLabel: this.$t('questionnaire_boolean_true'),
              falseLabel: this.$t('questionnaire_boolean_false'),
              nillLabel: this.$t('questionnaire_boolean_null')
            }
          }
          this.$store.commit('SET_MAPPER_OPTIONS', mapperOptions)
        }
      }
    },
    beforeRouteUpdate (to, from, next) {
      // Triggers when the route changes, but the component is reused
      // This is the case for dynamic urls e.g. /chapter/:chapterId
      this.changingChapters = true
      next()
      this.changingChapters = false
    },
    beforeRouteLeave (to, from, next) {
      this.$store.commit('SET_ERROR', '')
      next()
    },
    components: {
      LoadingSpinner,
      QuestionnaireError,
      FormComponent,
      ChapterList
    }
  }
</script>
