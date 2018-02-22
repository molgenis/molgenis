<template>
  <div class="chapter-navigation-container">
    <div class="text-muted" v-if="changesMade && saving">
      <i class="fa fa-spinner fa-spin"></i> {{ 'questionnaire_saving_changes' | i18n }}
    </div>

    <div class="text-muted" v-else-if="changesMade && !saving">
      {{ 'questionnaire_changes_saved' | i18n }}
    </div>

    <ul class="list-group chapter-navigation">
      <a class="list-group-item list-group-item-action disabled">
        {{ 'questionnaire_jump_to_chapter' | i18n }}
      </a>

      <router-link
        v-for="chapter in chapterNavigationList"
        :to="'/' + questionnaireId + '/chapter/' + chapter.index"
        :key="chapter.index"
        class="list-group-item list-group-item-action chapter-navigation-item">
        {{ chapter.label }}
      </router-link>
    </ul>
  </div>
</template>

<style scoped>
  .chapter-navigation-container {
    position: -webkit-sticky;
    position: sticky;
    top: 20px;
  }

  .list-group-item.disabled {
    background-color: #f5f5f5;
  }

  .router-link-active.chapter-navigation-item {
    border-left: solid 4px #c9302c;
  }
</style>

<script>
  export default {
    name: 'ChapterList',
    props: ['questionnaireId', 'changesMade', 'saving'],
    computed: {

      /**
       * Get a list of chapters to provide a navigation menu
       *
       * @return {Array<Object>} A list of chapters with label and chapter index
       */
      chapterNavigationList () {
        return this.$store.getters.getChapterNavigationList
      }
    }
  }
</script>
