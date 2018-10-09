<template>
  <div class="row justify-content-center">
    <div class="col-6">
      <b-input-group>
        <b-form-input v-model="query" type="text" :placeholder="$t('search-input-placeholder')"></b-form-input>
        <b-input-group-append>
          <b-btn variant="secondary" disabled><font-awesome-icon icon="search"/></b-btn>
        </b-input-group-append>
      </b-input-group>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'NavigatorSearch',
    data () {
      return {
        query: this.$route.query.q
      }
    },
    watch: {
      query (newVal) {
        this.$router.push({query: newVal ? {...this.$route.query, q: newVal} : undefined})
      }
    }
  }
</script>
