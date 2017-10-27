<template>
  <div class="row">
    <div class="col">
      <div class="row" v-for="memberRow in memberRows">
        <div class="col-4" v-for="member in memberRow">
          <members-grid-body-card :type="usersGroupsMap[member.id].type" :id="member.id"
                                  :label="usersGroupsMap[member.id].label"
                                  :role="roleMap[member.role]"></members-grid-body-card>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import MembersGridBodyCard from './MembersGridBodyCard'
  import {mapState, mapGetters} from 'vuex'

  export default {
    name: 'members-grid-body',
    computed: {
      ...mapGetters({usersGroups: 'getUsersGroups'}),
      ...mapState(['roles', 'members']),
      usersGroupsMap: function () {
        return this.usersGroups.reduce(function (map, obj) {
          map[obj.id] = obj
          return map
        }, {})
      },
      roleMap: function () {
        return this.roles.reduce(function (map, obj) {
          map[obj.id] = obj
          return map
        }, {})
      },
      memberRows: function () {
        let memberRows = []
        const size = 3
        for (let i = 0;
             i < this.members.length;
             i += size
        ) {
          memberRows.push(this.members.slice(i, i + size))
        }
        return memberRows
      }
    },
    components: {
      MembersGridBodyCard
    }
  }
</script>
