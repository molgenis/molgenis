<template>
  <div class="row">
    <div class="col-4">
      <button
        id="prev-chapter-btn"
        type="button"
        :disabled="saving"
        v-if="showPreviousButton"
        @click="navigateToPreviousChapter"
        class="btn btn-outline-secondary float-left">
        <span class="d-none d-md-block">{{ 'questionnaire_previous_chapter' | i18n }}</span>
        <span class="d-md-none"><i class="fa fa-chevron-left"></i></span>
      </button>
    </div>

    <div class="col-4 text-muted d-flex flex-column justify-content-center align-items-center">
      <span v-if="saving">
        {{ 'questionnaire_saving_changes' | i18n }} <i class="fa fa-spinner fa-spin"></i>
      </span>
    </div>

    <div class="col-4">
      <button
        id="next-chapter-btn"
        type="button"
        v-if="showNextButton"
        :disabled="saving"
        @click="validateBeforeNavigatingToNextChapter"
        class="btn btn-primary float-right">
        <span class="d-none d-md-block">{{ 'questionnaire_next_chapter' | i18n }}</span>
        <span class="d-md-none"><i class="fa fa-chevron-right"></i></span>
      </button>

      <button
        id="submit-questionnaire-btn"
        class="btn btn-primary float-right"
        @click="validateBeforeSubmit"
        :disabled="saving || submitting" v-else>
        <template v-if="submitting">
          <i class="fa fa-spinner fa-spin"></i>
        </template>

        <template v-else>
          <span class="d-none d-md-block">{{ 'questionnaire_submit' | i18n }}</span>
          <span class="d-md-none"><i class="fa fa-check"></i></span>
        </template>
      </button>
    </div>
  </div>
</template>

<script>
  import moment from 'moment'

  export default {
    name: 'ChapterNavigation',
    props: ['chapterId', 'currentChapter', 'formState', 'questionnaireId'],
    data () {
      return {
        submitting: false
      }
    },
    computed: {
      allChaptersAreComplete () {
        return Object.values(this.chapterCompletion).every(value => value)
      },

      chapterCompletion () {
        return this.$store.getters.getChapterCompletion
      },

      error () {
        return this.$store.state.error
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
      saving () {
        return this.$store.getters.isSaving
      }
    },
    methods: {
      clearStateAfterSuccessfulNavigation () {
        this.$store.commit('BLOCK_NAVIGATION', false)
        this.$store.commit('SET_ERROR', '')
        this.formState._reset()
      },

      navigateToNextChapter () {
        this.$router.push('/' + this.questionnaireId + '/change/' + this.nextChapterNumber)
        this.clearStateAfterSuccessfulNavigation()
      },

      navigateToPreviousChapter () {
        this.$router.push('/' + this.questionnaireId + '/change/' + this.previousChapterNumber)
        this.clearStateAfterSuccessfulNavigation()
      },

      /**
       * Forces validation if a user wants to go to the next chapter
       * Triggers client side validation by setting status to 'SUBMITTED'
       */
      validateBeforeNavigatingToNextChapter () {
        this.$store.commit('UPDATE_FORM_STATUS', 'SUBMITTED')
        this.formState._submit()

        if (this.chapterCompletion[this.currentChapter.id] === true) {
          this.navigateToNextChapter()
        } else {
          this.$store.commit('BLOCK_NAVIGATION', true)
        }
      },

      validateBeforeSubmit () {
        this.$store.commit('UPDATE_FORM_STATUS', 'SUBMITTED')
        this.formState._submit()

        if (this.chapterCompletion[this.currentChapter.id] === true) {
          if (this.allChaptersAreComplete) {
            this.submitting = true
            this.submitQuestionnaire()
          } else {
            const incompleteChapters = Object.keys(this.chapterCompletion).find(chapter => !this.chapterCompletion[chapter])
            const error = this.$t('questionnaire_forgotten_chapters') + ': ' + incompleteChapters

            this.$store.commit('SET_ERROR', error)
          }
        } else {
          this.$store.commit('BLOCK_NAVIGATION', true)
        }
      },

      submitQuestionnaire () {
        const submitDate = moment().toISOString()
        this.$store.dispatch('SUBMIT_QUESTIONNAIRE', submitDate).then(() => {
          this.$router.push('/' + this.questionnaireId + '/submitted')
          this.submitting = false
        })
      }
    }
  }
</script>
