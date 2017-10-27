<template>
  <div>
    <div class="row">
      <div class="col-4 form-inline">
        <input v-model="query" class="form-control" type="search" placeholder="Find a member ...">
      </div>
      <div class="offset-8 col-4">
        <button type="button" class="btn btn-success" @click="addMembers">Add members</button>
      </div>
    </div>
    <div class="row mt-1">
      <div class="col-4">
        <p>Sort by join data <i :class="['fa', sort === 'ascending' ? 'fa-caret-down' : 'fa-caret-up']"
                                @click="toggleSort"></i></p>
      </div>
      <div class="offset-4 col-4">
        <p>{{nrMembers}} member{{nrMembers !== 1 ? 's' : ''}}, {{nrGroups}} group{{nrGroups !== 1 ? 's' : ''}}</p>
      </div>
    </div>
  </div>
</template>

<script>
  import _ from 'lodash'
  import {QUERY_MEMBERS} from '../store/actions'
  import {SET_FILTER, SET_SORT} from '../store/mutations'

  export default {
    name: 'members-grid-controls',
    computed: {
      query: {
        get () {
          return this.$store.state.query
        },
        set (query) {
          this.$store.commit(SET_FILTER, query)
          this.submitQuery()
        }
      },
      sort: {
        get () {
          return this.$store.state.sort
        },
        set (sort) {
          this.$store.commit(SET_SORT, sort)
          this.submitQuery()
        }
      },
      nrMembers: function () {
        return this.$store.state.members.filter(member => this.$store.state.usersGroups.find(userGroup => userGroup.id === member.id && userGroup.type === 'user')).length
      },
      nrGroups: function () {
        return this.$store.state.members.filter(member => this.$store.state.usersGroups.find(userGroup => userGroup.id === member.id && userGroup.type === 'group')).length
      }
    },
    methods: {
      submitQuery: _.throttle(function () {
        this.$store.dispatch(QUERY_MEMBERS, {query: this.$store.state.query, sort: this.$store.state.sort})
      }),
      toggleSort: function () {
        this.sort = this.sort === 'ascending' ? 'descending' : 'ascending'
      },
      addMembers () {
        this.$router.push('/create')
      }
    }
  }
</script>

<style scoped>
  i {
    cursor: pointer;
  }

  button {
    cursor: pointer;
  }
</style>
