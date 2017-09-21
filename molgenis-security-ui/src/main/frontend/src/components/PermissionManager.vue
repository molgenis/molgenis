<template>
  <div class="container pt-3">
    <ol class="breadcrumb">
      <li class="breadcrumb-item"><a href="/">Home</a></li>
      <li class="breadcrumb-item active">{{'PERMISSION_MANAGER' | i18n}}</li>
    </ol>
    <div class="row pt-3" v-if="editRole">
      <div class="col col-md-12">
        <role-form></role-form>
      </div>
    </div>
    <div class="row pt-3" v-else>
      <div class="col col-md-4">
        <roles></roles>
      </div>
      <div class="col col-md-8">
        <div v-if="selectedSid">
          <h3>{{'TABLE' | i18n}}</h3>
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" :placeholder="'SELECT_AN_ENTITY'|i18n"></multiselect>
          <div v-if="selectedEntityTypeId">
            <h3 class="pt-3">{{'ROWS' | i18n}}</h3>
            <form @submit.prevent>
              <label for="filter" class="sr-only">{{'FILTER_LABEL' | i18n}}:</label>
              <input type="text" class="form-control" id="filter" :placeholder="'FILTER_LABEL'|i18n"
                     @input="filterChanged($event.target.value)" :value="filter">
            </form>
            <h3 class="pt-3">{{'PERMISSIONS' | i18n}}</h3>
            <acls :permissions="permissions" :acls="tableRows" :onPermissionClick="onPermissionClick"
                  :onGrantingClick="onGrantingClick"></acls>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="vue-multiselect/dist/vue-multiselect.min.css"></style>

<script>
  import { mapState, mapGetters, mapMutations, mapActions } from 'vuex'
  import {
    SET_SELECTED_ENTITY_TYPE,
    TOGGLE_PERMISSION,
    TOGGLE_GRANTING
  } from '../store/mutations'
  import {
    GET_ACLS,
    FILTER_CHANGED,
    SAVE_ACL
  } from '../store/actions'
  import Multiselect from 'vue-multiselect'
  import ACLs from './ACLs'
  import Roles from './Roles'
  import RoleForm from './RoleForm'
  import RoleMembers from './RoleMembers'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE,
        togglePermission: TOGGLE_PERMISSION,
        toggleGranting: TOGGLE_GRANTING
      }),
      ...mapActions({
        getAcls: GET_ACLS,
        filterChanged: FILTER_CHANGED,
        save: SAVE_ACL
      }),
      onPermissionClick (args) {
        this.togglePermission(args)
        this.save(this.rows[args.rowIndex].acl)
      },
      onGrantingClick (args) {
        this.toggleGranting(args)
        this.save(this.rows[args.rowIndex].acl)
      },
      reset () {
        this.setSelectedEntityType(null)
      }
    },
    computed: {
      ...mapState(['selectedSid', 'entityTypes', 'selectedEntityTypeId', 'permissions', 'rows', 'filter', 'editRole']),
      ...mapGetters(['tableRows', 'role']),
      selectedEntityType: {
        get () {
          return this.$store.state.selectedEntityType
        },
        set (selectedEntityType) {
          this.setSelectedEntityType(selectedEntityType.id)
          this.getAcls()
        }
      }
    },
    created () {
      this.reset()
    },
    watch: {
      '$route': 'reset'
    },
    components: {
      Multiselect,
      Roles,
      RoleForm,
      RoleMembers,
      acls: ACLs
    }
  }
</script>

<style scoped>
  button:hover {
    cursor: pointer
  }
</style>
