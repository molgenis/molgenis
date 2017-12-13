<template>
  <li class="list-group-item">
    <navigator-link :packageId="package_.id" :label="highlight(package_.label)"></navigator-link>
    &nbsp;
    <small v-if="package_.description" class="text-muted">
      <i class="text-small" v-html="highlight(package_.description)"></i>
    </small>
  </li>
</template>

<script>
  import NavigatorLink from './NavigatorLink'

  export default {
    name: 'package-search-result',
    props: ['package_', 'highlight'],
    components: {
      NavigatorLink
    }
  }
</script>
