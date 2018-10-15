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
          v-for="(item, index) in path"
          :key="item.id"
          class="breadcrumb-item">
          <a v-if="index == path.length - 1">{{ item.label }}</a>
          <router-link
            v-else
            :to="{params: {'item': item.id}}">{{ item.label }}</router-link>
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
import { mapGetters, mapState } from 'vuex'

export default {
  name: 'NavigatorBreadcrumb',
  computed: {
    ...mapGetters(['query']),
    ...mapState(['path'])
  }
}
</script>

<style scoped>
  .breadcrumb {
    background-color: transparent;
  }
</style>
