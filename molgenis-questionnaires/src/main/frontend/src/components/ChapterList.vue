<template>
  <div class="chapter-navigation-container">
    <div class="text-muted" v-if="changesMade && saving">
      <i class="fa fa-spinner fa-spin"></i> {{ 'questionnaire_saving_changes' | i18n }}
    </div>

    <div class="text-muted" v-else-if="changesMade && !saving">
      {{ 'questionnaire_changes_saved' | i18n }}
    </div>

    <ul class="list-group chapter-navigation">
      <a class="list-group-item list-group-item-action disabled header">
        {{ 'questionnaire_chapters' | i18n }}
      </a>

      <a v-for="chapter in chapterNavigationList" class="list-group-item list-group-item-action disabled"
         @click="navigateToChapter(chapter.index)">

        <span :class="{'active-chapter-text': chapter.index === currentChapterId}">
          {{ chapter.label }}
        </span>

        <!-- Progress bar container -->
        <div class="progress">
          <div class="progress-bar" :class="{'progress-bar bg-success':  progressPerChapter[chapter.id] === 100}"
               role="progressbar"
               :style="'width:' + progressPerChapter[chapter.id] + '%;'"
               :aria-valuenow="numberOfFilledInFieldsPerChapter[chapter.id]" aria-valuemin="1"
               :aria-valuemax="numberOfVisibleFieldsPerChapter[chapter.id]">
          </div>
        </div>
      </a>
    </ul>
  </div>
</template>

<style scoped>
  .chapter-navigation-container {
    position: -webkit-sticky;
    position: sticky;
    top: 0rem;
  }

  .list-group-item.disabled.header {
    background-color: #f5f5f5;
  }

  .list-group-item:hover {
    cursor: pointer;
    background-color: #f5f5f5;
  }

  .active-chapter-text {
    font-weight: bold;
  }
</style>

<script>
  export default {
    name: 'ChapterList',
    props: ['questionnaireId', 'currentChapterId', 'changesMade', 'saving'],
    methods: {
      navigateToChapter (index) {
        this.$router.push('/' + this.questionnaireId + '/chapter/' + index)
      }
    },
    computed: {
      chapterNavigationList () {
        return this.$store.getters.getChapterNavigationList
      },

      allVisibleFieldIdsInChapters () {
        return this.$store.getters.getVisibleFieldIdsForAllChapters
      },

      numberOfVisibleFieldsPerChapter () {
        const visibleFieldsPerChapter = {}

        Object.keys(this.allVisibleFieldIdsInChapters).forEach(key => {
          visibleFieldsPerChapter[key] = this.allVisibleFieldIdsInChapters[key].length
        })

        return visibleFieldsPerChapter
      },

      numberOfFilledInFieldsPerChapter () {
        const filledInFieldsPerChapter = {}

        Object.keys(this.allVisibleFieldIdsInChapters).forEach(key => {
          let numberOfFilledInFields = 0
          this.allVisibleFieldIdsInChapters[key].forEach(fieldId => {
            const value = this.$store.state.formData[fieldId]
            if (Array.isArray(value)) {
              if (value.length > 0) {
                numberOfFilledInFields++
              }
            } else {
              if (value !== undefined) {
                numberOfFilledInFields++
              }
            }
          })
          filledInFieldsPerChapter[key] = numberOfFilledInFields
        })

        return filledInFieldsPerChapter
      },

      progressPerChapter () {
        const progressPerChapter = {}

        this.chapterNavigationList.forEach(chapter => {
          progressPerChapter[chapter.id] = (this.numberOfFilledInFieldsPerChapter[chapter.id] / this.numberOfVisibleFieldsPerChapter[chapter.id]) * 100
        })

        return progressPerChapter
      }
    }
  }
</script>
