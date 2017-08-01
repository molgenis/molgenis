<template>
  <div class="container pt-3">
    <ol class="breadcrumb">
      <li class="breadcrumb-item"><a href="/">Home</a></li>
      <li class="breadcrumb-item active">{{'PERMISSION_MANAGER' | i18n}}</li>
    </ol>
    <div class="row pt-3">
      <div class="col col-md-4">
        <h3 class="pt-3">{{'SECURITY_ID' | i18n}}</h3>
        <ul class="nav nav-tabs">
          <li class="nav-item">
            <a class="nav-link" :class="{ active: sidType === 'role' }" href="#"
               @click="sidType = 'role'">{{'ROLE' | i18n}}</a>
          </li>
          <li class="nav-item">
            <a class="nav-link" :class="{ active: sidType === 'user' }" href="#"
               @click="sidType = 'user'">{{'USER' | i18n}}</a>
          </li>
        </ul>
        <div class="tab-content">
          <div class="tab-pane active pt-3">
            <roles :roles="roles" :sidType="sidType"
                   :selectedRole="selectedRole"
                   :selectRole="selectRole" :createRole="createRole" :updateRole="updateRole"
                   :onUpdateRole="onUpdateRole" :onDeleteRole="onDeleteRole"></roles>
            <role-members v-if="sidType==='role'"></role-members>
          </div>
        </div>
      </div>
      <div class="col col-md-8">
        <role-form v-if="doCreateRole"
                   :title="$t('CREATE_ROLE')"
                   :cancel="cancelCreateRole"
                   :submit="onSaveRole"></role-form>
        <role-form v-else-if="doUpdateRole"
                   :title="$t('UPDATE_ROLE')"
                   :initialLabel="role.label"
                   :initialDescription="role.description"
                   :cancel="cancelUpdateRole"
                   :submit="onUpdateRole"></role-form>
        <div v-else-if="selectedRole">
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
    CREATE_ROLE,
    CANCEL_CREATE_ROLE, CANCEL_UPDATE_ROLE, SET_SID_TYPE, SET_SELECTED_ENTITY_TYPE, TOGGLE_PERMISSION, TOGGLE_GRANTING
  } from '../store/mutations'
  import {
    GET_ROLES,
    SELECT_ROLE,
    GET_ACLS,
    FILTER_CHANGED,
    SAVE_ACL,
    SAVE_CREATE_ROLE,
    UPDATE_ROLE,
    DELETE_ROLE
  } from '../store/actions'
  import Multiselect from 'vue-multiselect'
  import ACLs from './ACLs'
  import Roles from './Roles'
  import RoleForm from './RoleForm'
  import RoleMembers from './RoleMembers'
  import capitalizeFirstLetter from '../filters/capitalizeFirstLetter'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        createRole: CREATE_ROLE,
        cancelCreateRole: CANCEL_CREATE_ROLE,
        updateRole: UPDATE_ROLE,
        cancelUpdateRole: CANCEL_UPDATE_ROLE,
        setSidType: SET_SID_TYPE,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE,
        togglePermission: TOGGLE_PERMISSION,
        toggleGranting: TOGGLE_GRANTING
      }),
      ...mapActions({
        selectRole: SELECT_ROLE,
        filterChanged: FILTER_CHANGED,
        save: SAVE_ACL,
        onSaveRole: SAVE_CREATE_ROLE,
        onUpdateRole: UPDATE_ROLE,
        onDeleteRole: DELETE_ROLE
      }),
      onPermissionClick (args) {
        this.togglePermission(args)
        this.save(args.rowIndex)
      },
      onGrantingClick (args) {
        this.toggleGranting(args)
        this.save(args.rowIndex)
      }
    },
    computed: {
      ...mapState(['roles', 'selectedRole', 'doCreateRole', 'doUpdateRole', 'selectedSid', 'sids', 'entityTypes', 'selectedEntityTypeId', 'permissions', 'acls', 'filter']),
      ...mapGetters(['tableRows']),
      selectedEntityType: {
        get () {
          return this.$store.state.selectedEntityType
        },
        set (selectedEntityType) {
          this.setSelectedEntityType(selectedEntityType.id)
          this.$store.dispatch(GET_ACLS)
        }
      },
      role: {
        get () {
          return this.roles.find(role => role.id === this.selectedRole)
        }
      },
      sidType: {
        get () {
          return this.$store.state.sidType
        },
        set (sidType) {
          this.setSidType(sidType)
          this.$store.dispatch(GET_ROLES)
        }
      }
    },
    components: {
      Multiselect,
      Roles,
      RoleForm,
      RoleMembers,
      acls: ACLs
    },
    filters: {
      capitalizeFirstLetter
    }
  }
</script>

<style scoped>
  button:hover {
    cursor: pointer
  }
</style>
