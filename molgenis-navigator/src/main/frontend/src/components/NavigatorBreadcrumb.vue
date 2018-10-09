<template>
  <div class="row">
    <div class="col">
      <ol class="breadcrumb">
        <li class="breadcrumb-item">
          <router-link to="/"><font-awesome-icon icon="home"/></router-link>
        </li>
        <li v-if="!query" class="breadcrumb-item" v-for="(package, index) in path">
          <a v-if="index == path.length - 1">{{package.label}}</a>
          <router-link v-else :to="package.id">{{package.label}}</router-link>
        </li>
        <li v-show="query" class="breadcrumb-item">
          <span>{{ 'search-query-label' | i18n }}: <b>{{query}}</b></span>
        </li>
      </ol>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'

  export default {
    name: 'NavigatorBreadcrumb',
    computed: {
      ...mapState(['path', 'query'])
    }
  }
</script>

<style scoped>
  .breadcrumb {
    background-color: transparent;
  }
</style>
