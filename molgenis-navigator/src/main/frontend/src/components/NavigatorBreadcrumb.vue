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
          v-for="(pathComponent, index) in packagePath"
          :key="pathComponent.id"
          class="breadcrumb-item">
          <a v-if="index == packagePath.length - 1">{{ pathComponent.label }}</a>
          <router-link
            v-else
            :to="{params: {'package': pathComponent.id}}">{{ pathComponent.label }}</router-link>
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
    ...mapGetters(['packagePath', 'query'])
  }
}
</script>

<style scoped>
  .breadcrumb {
    background-color: transparent;
  }
</style>
