<template>
  <div class="row">
    <div class="col">
      <h1>This is the MOLGENIS HelloWorld Component</h1>
      <div class="row">
        <!-- Roles component -->
        <div class="col col-md-4">
          <h3>Role</h3>
          <ul class="list-group">
            <li v-for="sid in sids"
                class="list-group-item"
                :class="{'active': selectedSids.includes(sid)}"
                @click="toggleSid(sid)">
              {{ sid.authority }}
            </li>
          </ul>
        </div>
        <div class="col col-md-8" v-if="selectedSid">
          <h3>Table</h3>
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" placeholder="Select an Entity..."></multiselect>
          <h3>Rows</h3>
          <div v-if="selectedEntityTypeId">
            <form>
              <label for="filter" class="sr-only">Filter:</label>
              <input type="text" class="form-control" id="filter" placeholder="Filter rows..." v-model="filter">
            </form>
            <table class="table table-sm">
              <thead>
              <tr>
                <th>Row</th>
                <th>Owner</th>
                <th :colspan="permissions.length + 1">Permissions</th>
              </tr>
              <tr>
                <th></th>
                <th></th>
                <th>Granted</th>
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
      Multiselect
    },
    filters: {
      capitalizeFirstLetter (string: string): string { return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase() }
    }
  }
</script>
