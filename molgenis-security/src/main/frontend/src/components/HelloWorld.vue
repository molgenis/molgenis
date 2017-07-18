<template>
  <div class="row">
    <div class="col">
      <h1>This is the MOLGENIS HelloWorld Component</h1>
      <div class="row">
        <!-- Roles component -->
        <div class="col col-md-4">
          <h2>Role</h2>
          <ul class="list-group">
            <li v-for="sid in sids"
                class="list-group-item"
                :class="{'active': selectedSid && (sid.authority === selectedSid.authority)}"
                @click="setSelectedSid(sid)">
              {{ sid.authority }}
            </li>
          </ul>
        </div>
        <div class="col col-md-8" v-if="selectedSid">
          <h2>Table</h2>
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" placeholder="Select an Entity..."></multiselect>
          <div v-if="selectedEntityTypeId">
            <h2>Rows</h2>
            <ul class="list-group">
              <li v-for="sid in acls"
                  class="list-group-item">
                {{ sid.authority }}
              </li>
            </ul>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

<script>
  import { mapState, mapMutations } from 'vuex'
  import { SET_SELECTED_SID, SET_SELECTED_ENTITY_TYPE } from '../store/mutations'
  import Multiselect from 'vue-multiselect'

  export default {
    name: 'hello-world',
    methods: {
      ...mapMutations({
        setSelectedSid: SET_SELECTED_SID,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE
      })
    },
    computed: {
      ...mapState(['sids', 'selectedSid', 'entityTypes', 'acls', 'selectedEntityTypeId']),
      selectedEntityType: {
        get () {
          const result = this.$store.state.selectedEntityType
          console.log('selectedEntityType', result)
          return result
        },
        set (selectedEntityType) {
          console.log('set selected entity type', selectedEntityType)
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType.id)
        }
      }
    },
    components: {
      Multiselect
    }
  }
</script>
