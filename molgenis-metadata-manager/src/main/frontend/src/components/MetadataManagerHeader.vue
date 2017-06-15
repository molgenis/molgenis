<template>
  <div>
    <div class="row">
      <div class="col-md-4 col-sm-12 col-xs-12">
        <h2>Metadata manager</h2>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                     selectLabel="" deselectLabel="" placeholder="Select an Entity..."></multiselect>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <b-button @click="createNewEntity" variant="primary">New</b-button>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapState } from 'vuex'
  import { CREATE_ENTITY_TYPE } from '../store/actions'
  import { SET_SELECTED_ENTITY_TYPE } from '../store/mutations'

  import Multiselect from 'vue-multiselect'

  export default {
    name: 'metadata-manager-header',
    methods: {
      createNewEntity: function () {
        this.$store.dispatch(CREATE_ENTITY_TYPE)
      }
    },
    computed: {
      ...mapState(['entityTypes']),
      selectedEntityType: {
        get () { return this.$store.state.selectedEntityType },
        set (value) {
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, value)
          this.$router.push({path: '/' + value.id})
        }
      }
    },
    components: {
      Multiselect
    }
  }
</script>
<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>
