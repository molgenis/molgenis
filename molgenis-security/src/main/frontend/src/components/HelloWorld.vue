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
            <table class="table table-sm">
              <thead>
              <tr>
                <th>Row</th>
                <th>Owner</th>
                <th :colspan="permissions.length">Permissions</th>
              </tr>
              <tr>
                <th></th>
                <th></th>
                <th v-for="permission in permissions">{{permission | capitalizeFirstLetter}}</th>
              </tr>
              </thead>
              <tbody>
              <tr v-for="acl in acls">
                <td>
                  {{ acl.entityLabel || acl.entityId }}
                </td>
                <td>
                  {{ acl.owner.username }}
                </td>
                <td v-for="permission in permissions">
                  <div class="form-check">
                    <label class="form-check-label">
                      <input class="form-check-input" type="checkbox" value="">
                    </label>
                  </div>
                </td>
              </tr>
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
  import { mapState, mapMutations } from 'vuex'
  import { SET_SELECTED_SID, SET_SELECTED_ENTITY_TYPE } from '../store/mutations'
  import Multiselect from 'vue-multiselect'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        setSelectedSid: SET_SELECTED_SID,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE
      })
    },
    computed: {
      ...mapState(['sids', 'selectedSid', 'entityTypes', 'acls', 'selectedEntityTypeId', 'permissions']),
      selectedEntityType: {
        get () {
          return this.$store.state.selectedEntityType
        },
        set (selectedEntityType) {
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType.id)
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
