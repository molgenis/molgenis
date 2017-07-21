<template>
  <div class="container pt-3">
    <ol class="breadcrumb">
      <li class="breadcrumb-item"><a href="/">Home</a></li>
      <li class="breadcrumb-item active">{{'PERMISSION_MANAGER' | i18n}}</li>
    </ol>
    <div class="row pt-3">
      <div class="col col-md-4">
        <h3>{{'ROLE' | i18n}}</h3>
        <roles :roles="roles"
               :selectedRole="selectedRole"
               :selectRole="selectRole" :createRole="createRole"></roles>
        <div v-if="selectedRole">
          <h3 class="pt-3">{{'MEMBERS' | i18n}}</h3>
          <template v-if="users && groups">
            <ul class="fa-ul" v-if="users || groups">
              <li v-for="group in groups"><i class="fa fa-users fa-li"></i>{{group}}
              </li>
              <li v-for="user in users"><i class="fa fa-user fa-li"></i>{{user}}
              </li>
            </ul>
            <p v-else>{{'NO_MEMBERS_IN_ROLE' | i18n}}</p>
          </template>
          <p v-else><i class="fa fa-spinner fa-spin"></i></p>
        </div>
      </div>
      <div class="col col-md-8">
        <div v-if="doCreateRole">
          <form v-on:submit="onSaveRole({label: roleLabel, description: roleDescription})">
            <div class="form-group">
              <label for="labelInput">{{'LABEL' | i18n}}</label>
              <input v-model="roleLabel" type="text" class="form-control" id="labelInput"
                     :placeholder="'ROLE_LABEL' | i18n"
                     required>
              <label for="descriptionInput">{{'DESCRIPTION' | i18n}}</label>
              <input v-model="roleDescription" type="text" class="form-control" id="descriptionInput"
                     :placeholder="'ROLE_DESCRIPTION' | i18n">
            </div>
            <div class="float-right">
              <button type="button" class="btn btn-default" @click="cancelCreateRole()">{{'CANCEL' | i18n}}</button>
              <button type="submit" class="btn btn-primary">{{'SAVE' | i18n}}</button>
            </div>
          </form>
        </div>
        <div v-else-if="selectedRole">
          <h3>{{'TABLE' | i18n}}</h3>
          <multiselect v-model="selectedEntityType" :options="entityTypes" label="label"
                       selectLabel="" deselectLabel="" :placeholder="'SELECT_AN_ENTITY'|i18n"></multiselect>
          <h3 class="pt-3">{{'ROWS' | i18n}}</h3>
          <div v-if="selectedEntityTypeId">
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
    CREATE_ROLE,
    CANCEL_CREATE_ROLE, SET_SELECTED_ENTITY_TYPE, TOGGLE_PERMISSION, TOGGLE_GRANTING
  } from '../store/mutations'
  import { SELECT_ROLE, GET_ACLS, FILTER_CHANGED, SAVE_ACL, SAVE_CREATE_ROLE } from '../store/actions'
  import Multiselect from 'vue-multiselect'
  import ACLs from './ACLs'
  import Roles from './Roles'
  import capitalizeFirstLetter from '../filters/capitalizeFirstLetter'

  export default {
    name: 'permission-manager',
    methods: {
      ...mapMutations({
        createRole: CREATE_ROLE,
        cancelCreateRole: CANCEL_CREATE_ROLE,
        setSelectedEntityType: SET_SELECTED_ENTITY_TYPE,
        togglePermission: TOGGLE_PERMISSION,
        toggleGranting: TOGGLE_GRANTING
      }),
      ...mapActions({
        selectRole: SELECT_ROLE,
        filterChanged: FILTER_CHANGED,
        save: SAVE_ACL,
        onSaveRole: SAVE_CREATE_ROLE
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
        roleLabel: null,
        roleDescription: null
      }
    },
    computed: {
      ...mapState(['roles', 'selectedRole', 'doCreateRole', 'sids', 'entityTypes', 'selectedEntityTypeId',
        'acls', 'filter', 'users', 'groups', 'permissions']),
      ...mapGetters(['tableRows']),
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
</style>
