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

      <a v-for="chapter in chapterNavigationList" class="list-group-item list-group-item-action disabled">
        <span v-if="isChapterCompleted(chapter.id)">
          <i class="fa fa-check text-success"></i>
        </span>

        <span :class="{'active-chapter-text': chapter.index === currentChapterId}">
          {{ chapter.label }}
        </span>
      </a>
    </ul>
  </div>
</template>

<style scoped>
  .chapter-navigation-container {
    position: -webkit-sticky;
    position: sticky;
    top: 80px;
  }

  .list-group-item.disabled.header {
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
      isChapterCompleted (chapterId) {
        return this.chapterProgress[chapterId] === 'complete'
      }
    },
    computed: {
      chapterProgress () {
        return this.$store.getters.getChapterProgress
      },
      chapterNavigationList () {
        return this.$store.getters.getChapterNavigationList
      }
    }
  }
</script>
