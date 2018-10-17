<template>
  <div class="row">
    <div class="col">
      <ol
        v-if="!query"
        class="breadcrumb">
        <li class="breadcrumb-item">
          <router-link to="/"><font-awesome-icon icon="home"/></router-link>
        </li>
        <li
          v-for="(folder, index) in folderPath"
          :key="folder.id"
          class="breadcrumb-item">
          <a v-if="index == folderPath.length - 1">{{ folder.label }}</a>
          <router-link
            v-else
            :to="{params: {'folderId': folder.id}}">{{ folder.label }}</router-link>
        </li>
      </ol>
      <ol
        v-else
        class="breadcrumb">
        <li class="breadcrumb-item">
          <router-link to="/"><font-awesome-icon icon="home"/></router-link>
        </li>
        <li
          v-show="query"
          class="breadcrumb-item">
          <span>{{ 'search-query-label' | i18n }}: <b>{{ query }}</b></span>
        </li>
      </ol>
    </div>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'

export default {
  name: 'NavigatorBreadcrumb',
  computed: {
    ...mapGetters(['folderPath', 'query'])
  }
}
</script>

<style scoped>
  .breadcrumb {
    background-color: transparent;
  }
</style>
