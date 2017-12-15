<template>
  <a :href="uri" class="card-link">
    <i class="fa fa-folder-open-o" aria-hidden="true"></i> <span v-html="label"></span>
  </a>
</template>

<script>
  export default {
    name: 'navigator-link',
    props: ['packageId', 'label'],
    data () {
      return {
        uri: window.searchall.navigatorBaseUrl + '/' + this.packageId
      }
    }
  }
</script>
