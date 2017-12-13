<template>
  <a :href="uri" class="card-link">
    <i class="fa fa-align-justify" aria-hidden="true"></i> {{'show-in-dataexplorer-link' | i18n }}
  </a>
</template>

<script>
  export default {
    name: 'dataexplorer-link',
    props: ['entityTypeId'],
    data () {
      return {
        uri: window.searchall.dataExplorerBaseUrl + '?entity=' + this.entityTypeId
      }
    }
  }
</script>
