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
              <input type="text" class="form-control" id="filter" :placeholder="'FILTER_LABEL'|i18n" v-model="filter">
            </form>
            <table class="table table-sm">
              <thead>
              <tr>
                <th>{{'ROW' | i18n}}</th>
                <th>{{'OWNER' | i18n}}</th>
                <th :colspan="permissions.length + 1">{{'PERMISSIONS' | i18n}}</th>
              </tr>
              <tr>
                <th></th>
                <th></th>
                <th>{{'GRANTED' | i18n}}</th>
                <th v-for="permission in permissions">{{permission | capitalizeFirstLetter}}</th>
              </tr>
              </thead>
              <tbody>
              <template v-for="acl in filteredAcls">
                <tr v-for="(ace, index) in acl.aces">
                  <td>
                    <span v-if="index == 0">{{acl.entityLabel || acl.entityId}}</span>
                  </td>
                  <td>
                    <span v-if="index == 0">{{ index == 0 && acl.owner.username }}</span>
                  </td>
                  <td v-if="ace.granted"><i class="fa fa-check"></i></td>
                  <td v-else><i class="fa fa-ban"></i></td>
                  <td v-for="permission in permissions">
                    <div class="form-check">
                      <label class="form-check-label">
                        <input class="form-check-input" type="checkbox"
                               :checked="ace.permissions.indexOf(permission) >= 0">
                      </label>
                    </div>
                  </td>
                </tr>
              </template>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

<script>
  import { mapState, mapGetters, mapMutations } from 'vuex'
  import { TOGGLE_SID, SET_SELECTED_ENTITY_TYPE, SET_FILTER } from '../store/mutations'
  import Multiselect from 'vue-multiselect'
  import Sids from './Sids'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        toggleSid: TOGGLE_SID,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE
      })
    },
    computed: {
      ...mapState(['sids', 'selectedSids', 'entityTypes', 'acls', 'selectedEntityTypeId', 'permissions']),
      ...mapGetters(['filteredAcls']),
      selectedEntityType: {
        get () {
          return this.$store.state.selectedEntityType
        },
        set (selectedEntityType) {
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType.id)
        }
      },
      filter: {
        get () {
          return this.$store.state.filter
        },
        set (filter) {
          this.$store.commit(SET_FILTER, filter)
        }
      }
    },
    components: {
      Multiselect,
      Sids
    },
    filters: {
      capitalizeFirstLetter (string: string): string { return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase() }
    }
  }
</script>
