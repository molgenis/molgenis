<template>
  <div class="row">
    <div class="col">
      <div class="row">
        <div class="col col-md-4">
          <h3>{{'ROLE' | i18n}}</h3>
          <sids :sids="sids"
                :selectedSids="selectedSids"
                :toggleSid="toggleSid"></sids>
        </div>
        <div class="col col-md-8" v-if="selectedSids.length">
          <h3>{{'TABLE' | i18n}}</h3>
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" :placeholder="'SELECT_AN_ENTITY'|i18n"></multiselect>
          <h3>{{'ROWS' | i18n}}</h3>
          <div v-if="selectedEntityTypeId">
            <form>
              <label for="filter" class="sr-only">{{'FILTER_LABEL' | i18n}}:</label>
              <input type="text" class="form-control" id="filter" :placeholder="'FILTER_LABEL'|i18n"
                     @input.stop="filterChanged($event.target.value)">
            </form>
            <acls :permissions="permissions" :acls="acls"></acls>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

<script>
  import { mapState, mapMutations, mapActions } from 'vuex'
  import { TOGGLE_SID, SET_SELECTED_ENTITY_TYPE } from '../store/mutations'
  import { GET_ACLS, FILTER_CHANGED } from '../store/actions'
  import Multiselect from 'vue-multiselect'
  import ACLs from './ACLs'
  import Sids from './Sids'
  import capitalizeFirstLetter from '../filters/capitalizeFirstLetter'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        toggleSid: TOGGLE_SID,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE
      }),
      ...mapActions({
        filterChanged: FILTER_CHANGED
      })
    },
    computed: {
      ...mapState(['sids', 'selectedSids', 'entityTypes', 'selectedEntityTypeId', 'permissions', 'acls', 'filter']),
      selectedEntityType: {
        get () {
          return this.$store.state.selectedEntityType
        },
        set (selectedEntityType) {
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType.id)
          this.$store.dispatch(GET_ACLS)
        }
      }
    },
    components: {
      Multiselect,
      Sids,
      acls: ACLs
    },
    filters: {
      capitalizeFirstLetter
    }
  }
</script>
