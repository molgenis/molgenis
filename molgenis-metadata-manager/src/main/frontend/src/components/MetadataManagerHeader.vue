<template>
  <div>
    <div class="row">
      <div class="col-md-4 col-sm-12 col-xs-12">
        <h2>Metadata manager</h2>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <entity-select-box :value="selectedEntityType" :options="entityTypes" :onChange="onChange"></entity-select-box>
      </div>
      <div class="col-md-4 col-sm-12 col-xs-12">
        <b-button @click="createNewEntity" variant="primary">New</b-button>
      </div>
    </div>
  </div>
</template>

<script>
  import { mapGetters } from 'vuex'

  import EntitySelectBox from './generic-components/EntitySelectBox'
  import { GET_ENTITY_TYPE_BY_ID, CREATE_ENTITY_TYPE } from '../store/actions'
  import { CLEAR_EDITOR_ENTITY_TYPE } from '../store/mutations'

  export default {
    name: 'metadata-manager-header',
    computed: {
      ...mapGetters({
        selectedEntityType: 'getSelectedEntityType',
        entityTypes: 'getEntityTypes'
      })
    },
    methods: {
      onChange: function (selectedEntity) {
        if (selectedEntity !== null) {
          this.$router.push('/' + selectedEntity.id)
          this.$store.dispatch(GET_ENTITY_TYPE_BY_ID, selectedEntity.id)
        } else {
          this.$router.push('/')
          this.$store.commit(CLEAR_EDITOR_ENTITY_TYPE)
        }
      },
      createNewEntity: function () {
        this.$store.dispatch(CREATE_ENTITY_TYPE)
      }
    },
    components: {
      EntitySelectBox
    }
  }
</script>
