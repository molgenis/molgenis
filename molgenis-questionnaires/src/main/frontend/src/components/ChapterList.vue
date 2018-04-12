<template>
  <ul class="list-group chapter-navigation">
    <a class="list-group-item list-group-item-action disabled header">
      {{ 'questionnaire_chapters' | i18n }}
    </a>

    <template v-for="chapter in chapterNavigationList">
      <template v-if="!saving">
        <router-link
          :to="'/' + questionnaireId + '/change/' + chapter.index"
          class="list-group-item list-group-item-action">

          <span>{{ chapter.label }}</span>

          <div class="progress" style="height: 5px;">
            <div class="progress-bar"
                 :class="{'bg-success': chapterProgress[chapter.id] === 100}"
                 role="progressbar"
                 :style="'width: ' + chapterProgress[chapter.id] + '%'"
                 :aria-valuenow="chapterProgress[chapter.id]"
                 aria-valuemin="0"
                 aria-valuemax="100">
            </div>
          </div>

        </router-link>
      </template>
      <template v-else>
        <a
          :disabled="saving"
          :class="{'disabled': saving}"
          class="list-group-item list-group-item-action">

          <span>{{ chapter.label }}</span>

          <div class="progress" style="height: 5px;">
            <div class="progress-bar"
                 :class="{'bg-success': chapterProgress[chapter.id] === 100}"
                 role="progressbar"
                 :style="'width: ' + chapterProgress[chapter.id] + '%'"
                 :aria-valuenow="chapterProgress[chapter.id]"
                 aria-valuemin="0"
                 aria-valuemax="100">
            </div>
          </div>

        </a>
      </template>
    </template>
  </ul>
</template>

<style scoped>
  .list-group-item.disabled {
    background-color: #f5f5f5;
  }

  .list-group-item.disabled:hover {
    cursor: default;
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
    props: ['questionnaireId'],
    computed: {
      chapterNavigationList () {
        return this.$store.getters.getChapterNavigationList
      },

      chapterProgress () {
        return this.$store.getters.getChapterProgress
      },

      saving () {
        return this.$store.getters.isSaving
      }
    }
  }
</script>
