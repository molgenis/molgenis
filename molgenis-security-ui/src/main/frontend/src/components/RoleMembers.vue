<template>
  <div v-if="selectedSid && sidType === 'role'">
    <h3 class="pt-3">{{'MEMBERS' | i18n}}</h3>
    <template v-if="role && role.users !== undefined && role.groups !== undefined">
      <ul class="fa-ul" v-if="role.users.length + role.groups.length">
        <li v-for="group in groupsInRole"><i class="fa fa-users fa-li"></i>{{group.name}}
        </li>
        <li v-for="user in usersInRole"><i class="fa fa-user fa-li"></i>{{user.username}}
        </li>
      </ul>
      <p v-else>{{'NO_MEMBERS_IN_ROLE' | i18n}}</p>
    </template>
    <p v-else><i class="fa fa-spinner fa-spin"></i></p>
  </div>
</template>

<script>
  import {mapState, mapGetters} from 'vuex'

  export default {
    computed: {
      ...mapState(['selectedSid', 'sidType', 'users', 'groups']),
      ...mapGetters(['role']),
      usersInRole () {
        return this.users.filter(user => this.role.users && this.role.users.indexOf(user.username) >= 0)
      },
      groupsInRole () {
        return this.groups.filter(group => this.role.groups && this.role.groups.indexOf(group.id) >= 0)
      }
    }
  }
</script>
