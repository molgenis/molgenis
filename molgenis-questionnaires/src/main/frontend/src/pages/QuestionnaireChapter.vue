<template>
  <div class="container-fluid">
    <div class="row my-5">
      <div class="col-12">

        <template v-if="loading">
          <loading-spinner :message="$t('questionnaire_loading_chapter_text')"></loading-spinner>
        </template>

        <template v-else>
          <div class="row">
            <div class="col-12">
              <router-link
                id="back-to-start-btn"
                :to="'/' + questionnaireId"
                class="btn btn-outline-secondary">
                {{ 'questionnaire_back_to_start' | i18n }}
              </router-link>
              <hr>
            </div>
          </div>

          <div class="row">
            <div class="col-xs-12 col-sm-12 col-md-12 col-lg-9 col-xl-9">
              <div class="card">

                <!-- ================ CARD HEADER ================ -->
                <div class="card-header">
                  <div id="current-chapter-label" class="col-12 text-muted d-flex flex-column justify-content-center align-items-center">
                    Chapter {{ chapterId }} of {{ totalNumberOfChapters }}
                  </div>
                </div>

                <!-- ================ CARD BODY ================ -->
                <div class="card-body">
                  <chapter-form
                    :currentChapter="currentChapter"
                    :formState="formState"
                    :questionnaireId="questionnaireId">
                  </chapter-form>
                </div>

                <!-- ================ ERROR BLOCK ================ -->
                <div v-if="navigationBlocked" class="alert alert-warning mb-0" role="alert">
                  {{ 'questionnaire_chapter_incomplete_message' | i18n }}
                  <span v-for="(value, key) in formState.$error" >{{ key }} </span>
                </div>

                <div v-if="error" class="alert alert-warning mb-0" role="alert">
                  {{ error }}
                </div>

                <!-- ================ CARD FOOTER ================ -->
                <div class="card-footer">
                  <chapter-navigation
                    :chapterId="chapterId"
                    :currentChapter="currentChapter"
                    :formState="formState"
                    :questionnaireId="questionnaireId">
                  </chapter-navigation>
                </div>
              </div>
            </div>

            <!-- ================ CHAPTER NAVIGATION ================ -->
            <div class="d-none d-lg-block col-3">
              <div class=" chapter-navigation-container">
                <chapter-list
                  :currentChapterId="chapterId"
                  :questionnaireId="questionnaireId">
                </chapter-list>

                <span v-if="saving" class="text-muted">
                  {{ 'questionnaire_saving_changes' | i18n }} <i class="fa fa-spinner fa-spin"></i>
                </span>

                <span v-else-if="!saving && !formState.$pristine" class="text-muted">
                  {{ 'questionnaire_changes_saved' | i18n }}
                </span>
              </div>
            </div>
          </div>
        </template>

      </div>
    </div>
  </div>
</template>

<style scoped>
  .chapter-navigation-container {
    position: -webkit-sticky;
    position: sticky;
    top: 1rem;
  }
</style>

<script>
  import LoadingSpinner from '../components/LoadingSpinner'
  import ChapterNavigation from '../components/ChapterNavigation'
  import ChapterList from '../components/ChapterList'
  import ChapterForm from '../components/ChapterForm'

  export default {
    name: 'QuestionnaireChapter',
    props: ['chapterId', 'questionnaireId'],
    data () {
      return {
        formState: {}
      }
    },
    computed: {
      currentChapter () {
        return this.$store.getters.getChapterByIndex(this.chapterId)
      },
      error () {
        return this.$store.state.error
      },
      loading () {
        return this.$store.state.loading
      },
      saving () {
        return this.$store.getters.isSaving
      },
      navigationBlocked () {
        const errors = this.formState.$error && Object.keys(this.formState.$error).length > 0
        return this.$store.state.navigationBlocked && errors
      },
      totalNumberOfChapters () {
        return this.$store.getters.getTotalNumberOfChapters
      }
    },
    created () {
      this.$store.commit('BLOCK_NAVIGATION', false)
      this.$store.commit('SET_ERROR', '')
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
    },
    beforeRouteUpdate (to, from, next) {
      this.$store.commit('UPDATE_FORM_STATUS', 'OPEN')
      this.$store.commit('SET_ERROR', '')
      next()
    },
    components: {
      ChapterForm,
      ChapterNavigation,
      LoadingSpinner,
      ChapterList
    }
  }
</script>
