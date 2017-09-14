<template>
  <div v-if="selectedRole">
    <h3 class="pt-3">{{'MEMBERS' | i18n}}</h3>
    <template v-if="users && groups">
      <ul class="fa-ul" v-if="users.length + groups.length">
        <li v-for="group in groups"><i class="fa fa-users fa-li"></i>{{group}}
        </li>
        <li v-for="user in users"><i class="fa fa-user fa-li"></i>{{user}}
        </li>
      </ul>
      <p v-else>{{'NO_MEMBERS_IN_ROLE' | i18n}}</p>
    </template>
    <p v-else><i class="fa fa-spinner fa-spin"></i></p>
  </div>
</template>

<script>
  import {mapState} from 'vuex'

  export default {
    computed: {
      ...mapState(['selectedRole', 'users', 'groups'])
    }
  }
</script>
