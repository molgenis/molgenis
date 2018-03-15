<template>
  <ul class="list-group chapter-navigation">
    <a class="list-group-item list-group-item-action disabled header">
      {{ 'questionnaire_chapters' | i18n }}
    </a>

    <template v-for="chapter in chapterNavigationList">
      <router-link
        :to="'/' + questionnaireId + '/chapter/' + chapter.index"
        class="list-group-item list-group-item-action disabled">
        <span>{{ chapter.label }}</span>
      </router-link>
    </template>
  </ul>
</template>

<style scoped>
  .list-group-item.disabled.header {
    background-color: #f5f5f5;
  }

  .list-group-item:hover {
    cursor: pointer;
    background-color: #f5f5f5;
  }

  .router-link-active {
    border-left: solid 0.25em #007bff;
  }
</style>

<script>
  export default {
    name: 'ChapterList',
    props: ['questionnaireId', 'currentChapterId'],
    computed: {
      chapterNavigationList () {
        return this.$store.getters.getChapterNavigationList
      }
    }
  }
</script>
