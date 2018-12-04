<template>
  <div class="row justify-content-center">
    <div class="col-md-6">
      <b-input-group>
        <b-form-input
          v-model="query"
          :placeholder="$t('search-input-placeholder')"
          type="text"/>
        <b-input-group-append>
          <b-btn variant="secondary"><font-awesome-icon icon="search"/></b-btn>
        </b-input-group-append>
      </b-input-group>
    </div>
  </div>
</template>

<script>
export default {
  name: 'NavigatorSearch',
  computed: {
    query: {
      get: function () {
        return this.$store.state.route.query.q
      },
      set: function (query) {
        this.$router.push({
          path: '/',
          params: {...this.$route.params, folder: undefined},
          query: query ? {...this.$route.query, q: query} : undefined
        })
      }
    }
  }
}
</script>
