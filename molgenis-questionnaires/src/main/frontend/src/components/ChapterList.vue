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

      <a v-for="chapter in chapterNavigationList"
         class="list-group-item list-group-item-action disabled"
         :class="{'active-chapter' : chapter.index === currentChapterId}">

        <span v-if="isChapterCompleted(chapter.id)">
          <i class="fa fa-check text-success"></i>
        </span>

        {{ chapter.label }}
      </a>
    </ul>
  </div>
</template>

<style scoped>
  .chapter-navigation-container {
    position: -webkit-sticky;
    position: sticky;
    top: 20px;
  }

  .list-group-item.disabled.header {
    background-color: #f5f5f5;
  }

  .active-chapter {
    background-color: #e5f6ff;
  }
</style>

<script>
  export default {
    name: 'ChapterList',
    props: ['questionnaireId', 'currentChapterId', 'changesMade', 'saving'],
    methods: {
      isChapterCompleted (chapterId) {
        return this.$store.getters.getChapterProgress[chapterId] === 'complete'
      }
    },
    computed: {
      chapterNavigationList () {
        return this.$store.getters.getChapterNavigationList
      }
    }
  }
</script>
