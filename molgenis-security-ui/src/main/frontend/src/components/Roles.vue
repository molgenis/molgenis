<template>
  <div>
    <h3>{{'SECURITY_ID' | i18n}}</h3>
    <ul class="nav nav-tabs">
      <li class="nav-item" v-for="type in ['role', 'user']">
        <a class="nav-link" :class="{ active: sidType === type }" href="#"
           @click="setSidType(type)">{{type | toUpper | i18n}}</a>
      </li>
    </ul>
    <div class="tab-content">
      <div class="tab-pane active pt-3">
        <div class="row" v-if="sidType === 'role'">
          <div class="col-12">
            <button type="button" class="btn btn-success" @click="createRole()"><i class="fa fa-plus"></i></button>
            <button v-if="selectedSid" type="button" class="btn btn-default" @click="editRole"><i class="fa fa-pencil"></i></button>
            <button v-if="selectedSid" type="button" class="btn btn-danger" @click="deleteRole"><i class="fa fa-trash"></i></button>
          </div>
        </div>
        <div class="row">
          <div class="col-12 mt-2">
            <div class="list-group">
              <a v-for="item in items"
                 class="list-group-item list-group-item-action flex-column align-items-start"
                 :class="{active: item.active}"
                 @click="selectItem(item.sid)"
                 href="#">
                <div class="d-flex w-100 justify-content-between">
                  <h5 class="mb-1">{{ item.title }}</h5>
                </div>
                <small v-if="item.description">{{ item.description }}</small>
              </a>
            </div>
          </div>
        </div>
        <role-members v-if="sidType==='role'"></role-members>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapState, mapMutations, mapActions} from 'vuex'
  import RoleMembers from './RoleMembers'

  import {
    EDIT_ROLE,
    SET_SID_TYPE,
    SET_SELECTED_SID
  } from '../store/mutations'

  import {
    DELETE_ROLE,
    SELECT_USER,
    SELECT_ROLE
  } from '../store/actions'

  import {toUpper} from '../filters/text'

  export default {
    name: 'roles',
    computed: {
      ...mapState(['roles', 'users', 'selectedSid', 'sidType']),
      items () {
        if (this.sidType === 'role') {
          return this.roles.map(role => ({
            active: role.id === this.selectedSid,
            title: role.label,
            sid: role.id,
            description: role.description
          }))
        }
        return this.users.map(user => ({
          active: user.username === this.selectedSid,
          title: user.username,
          sid: user.username,
          description: user.emailAddress
        }))
      }
    },
    methods: {
      selectItem (sid) {
        if (this.sidType === 'role') {
          this.selectRole(sid)
        } else {
          this.selectUser(sid)
        }
      },
      createRole () {
        this.setSelectedSid(null)
        this.editRole()
      },
      ...mapMutations({
        editRole: EDIT_ROLE,
        setSelectedSid: SET_SELECTED_SID,
        setSidType: SET_SID_TYPE
      }),
      ...mapActions({
        deleteRole: DELETE_ROLE,
        selectRole: SELECT_ROLE,
        selectUser: SELECT_USER
      })
    },
    components: {
      RoleMembers
    },
    filters: {
      toUpper
    }
  }
</script>

<style scoped>
  button:hover {
    cursor: pointer
  }
</style>
