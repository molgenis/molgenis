<template>
  <div class="container">
    <div class="page-header">
      <h1>Permission Manager</h1>
    </div>
    <div class="row">
      <div class="col col-md-4">
        <h3>{{'ROLE' | i18n}}</h3>
        <roles :roles="roles"
               :selectedRole="selectedRole"
               :selectRole="selectRole" :createRole="createRole" :updateRole="updateRole" :onUpdateRole="onUpdateRole" :onDeleteRole="onDeleteRole"></roles>
      </div>
      <div class="col col-md-8">
        <div v-if="doCreateRole">
          <h3>{{'CREATE_ROLE' | i18n}}</h3>
          <form v-on:submit="onSaveRole({label: roleLabel, description: roleDescription})">
            <div class="form-group">
              <label for="labelInput">Label</label>
              <input v-model="roleLabel" type="text" class="form-control" id="labelInput" placeholder="Role label"
                     required>
              <label for="descriptionInput">Description</label>
              <input v-model="roleDescription" type="text" class="form-control" id="descriptionInput"
                     placeholder="Role description">
            </div>
            <div class="float-right">
              <button type="button" class="btn btn-default" @click="cancelCreateRole()">{{'CANCEL' | i18n}}</button>
              <button type="submit" class="btn btn-primary">{{'SAVE' | i18n}}</button>
            </div>
          </form>
        </div>
        <div v-else-if="doUpdateRole">
          <h3>{{'UPDATE_ROLE' | i18n}}</h3>
          <form v-on:submit="onUpdateRole(role)">
            <div class="form-group">
              <label for="labelInput">Label</label>
              <input v-model="role.label" type="text" class="form-control" id="labelInput" placeholder="Role label"
                     required>
              <label for="descriptionInput">Description</label>
              <input v-model="role.description" type="text" class="form-control" id="descriptionInput"
                     placeholder="Role description">
            </div>
            <div class="float-right">
              <button type="button" class="btn btn-default" @click="cancelUpdateRole()">{{'CANCEL' | i18n}}</button>
              <button type="submit" class="btn btn-primary">{{'SAVE' | i18n}}</button>
            </div>
          </form>
        </div>
        <div v-else-if="selectedRole">
          <h3>{{'TABLE' | i18n}}</h3>
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" :placeholder="'SELECT_AN_ENTITY'|i18n"></multiselect>
          <div v-if="selectedEntityTypeId">
            <h3>{{'ROWS' | i18n}}</h3>
            <form>
              <label for="filter" class="sr-only">{{'FILTER_LABEL' | i18n}}:</label>
              <input type="text" class="form-control" id="filter" :placeholder="'FILTER_LABEL'|i18n"
                     @input.stop="filterChanged($event.target.value)">
            </form>
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
    SELECT_ROLE, CREATE_ROLE,
    CANCEL_CREATE_ROLE, CANCEL_UPDATE_ROLE, SET_SELECTED_ENTITY_TYPE, TOGGLE_PERMISSION, TOGGLE_GRANTING
  } from '../store/mutations'
  import { GET_ACLS, FILTER_CHANGED, SAVE_ACL, SAVE_CREATE_ROLE, UPDATE_ROLE, DELETE_ROLE } from '../store/actions'
  import Multiselect from 'vue-multiselect'
  import ACLs from './ACLs'
  import Roles from './Roles'
  import capitalizeFirstLetter from '../filters/capitalizeFirstLetter'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        selectRole: SELECT_ROLE,
        createRole: CREATE_ROLE,
        cancelCreateRole: CANCEL_CREATE_ROLE,
        updateRole: UPDATE_ROLE,
        cancelUpdateRole: CANCEL_UPDATE_ROLE,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE,
        togglePermission: TOGGLE_PERMISSION,
        toggleGranting: TOGGLE_GRANTING
      }),
      ...mapActions({
        filterChanged: FILTER_CHANGED,
        save: SAVE_ACL,
        onSave: SAVE_ACL,
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
    data: function () {
      return {
        roleId: null,
        roleLabel: null,
        roleDescription: null
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
          this.$store.commit(SET_SELECTED_ENTITY_TYPE, selectedEntityType.id)
          this.$store.dispatch(GET_ACLS)
        }
      },
      role: {
        get () {
          return Object.assign({}, this.roles.find(role => { return role.id === this.selectedRole }))
        }
      }
    },
    components: {
      Multiselect,
      Roles,
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

  .container {
    padding: 25px
  }
</style>
