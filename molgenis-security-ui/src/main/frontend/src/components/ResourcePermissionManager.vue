<template>
  <div class="pt-3">
    <ol class="breadcrumb">
      <li class="breadcrumb-item"><a href="/">Home</a></li>
      <li class="breadcrumb-item active">{{'RESOURCE_PERMISSION_MANAGER' | i18n}}</li>
    </ol>
    <div class="row pt-3" v-if="acl">
      <div class="col col-md-12">
      <h3><span class="badge badge-primary">{{$route.params.entityType}}</span> {{$route.params.id}}</h3>
      <p><router-link :to="parentLink" v-if="acl.parent">Go to parent</router-link></p>
      <p>Owner: {{acl.owner.username}}</p>
      <!--<code v-if="acl">{{acl | json}}</code>-->
      <table class="table table-striped table-condensed">
        <thead>
        <tr>
          <th>Security ID</th>
          <th>granting</th>
          <th v-for="permission in permissions" class="text-center">{{permission}}</th>
        </tr>
        </thead>
        <tbody>
        <tr v-for="(entry, aceIndex) in acl.entries">
          <td v-if="entry.securityId.authority"><i class="fa fa-users"></i> {{entry.securityId.authority}}</td>
          <td v-else><i class="fa fa-user"></i> {{entry.securityId.username}}</td>
          <td>{{entry.granting}}</td>
          <td v-for="permission in permissions" class="text-center">
            <div class="form-check">
              <label class="form-check-label">
                <input class="form-check-input"
                       type="checkbox"
                       :checked="entry.permissions.indexOf(permission) >= 0"
                       @click="toggle(aceIndex, permission)">
              </label>
            </div>
          </td>
        </tr>
        </tbody>
      </table>
      </div>
      <sidform v-if="showAddButton"
               :roles="rolesOptions"
               :users="usersOptions"
               :submit="formSubmit"></sidform>
    </div>

  </div>
</template>

<script>
  import { mapActions, mapMutations, mapState } from 'vuex'
  import {GET_ACL, SAVE_ACL, GET_ROLES} from '../store/actions'
  import {TOGGLE_PERMISSION_IN_ACL, ADD_ACL_ENTRY} from '../store/mutations'
  import SecurityIDForm from './SecurityIDForm'

  export default {
    name: 'resource-permission-manager',
    methods: {
      ...mapActions({
        getRoles: GET_ROLES,
        getAcl: GET_ACL,
        saveAcl: SAVE_ACL
      }),
      ...mapMutations({
        togglePermissionInACL: TOGGLE_PERMISSION_IN_ACL,
        addAclEntry: ADD_ACL_ENTRY
      }),
      toggle (aceIndex, permission) {
        this.togglePermissionInACL({aceIndex, permission})
        this.saveAcl(this.acl)
      },
      formSubmit (selectedSid) {
        this.addAclEntry(selectedSid)
        this.saveAcl(this.acl)
      }
    },
    computed: {
      ...mapState(['acl', 'permissions', 'roles', 'users']),
      parentLink () {
        const {entityTypeId, entityId} = this.acl.parent.entityIdentity
        return `/resource/${entityTypeId}/${entityId}`
      },
      rolesOptions () {
        if (!this.acl) {
          return []
        }
        return (this.roles || []).filter(role => this.acl.entries.map(entry => entry.securityId.authority).indexOf(role.id) === -1)
      },
      usersOptions () {
        if (!this.acl) {
          return []
        }
        return (this.users || []).filter(user => this.acl.entries.map(entry => entry.securityId.username).indexOf(user.username) === -1)
      },
      showAddButton () {
        return this.rolesOptions.length + this.usersOptions.length > 0
      }
    },
    mounted () {
      this.getRoles()
      this.getAcl()
    },
    components: {
      'sidform': SecurityIDForm
    },
    watch: {
      '$route' () {
        this.getAcl()
      }
    },
    filters: {
      json (object) {
        return JSON.stringify(object, null, 2)
      }
    }
  }
</script>
