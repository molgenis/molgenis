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
        <p>Sort by join date <i :class="['fa', sort === 'ascending' ? 'fa-caret-down' : 'fa-caret-up']"
                                @click="toggleSort"></i></p>
      </div>
      <div class="offset-4 col-4">
        <p>{{nrMembers}} {{$t('member', {count: nrMembers}) }}, {{nrGroups}} {{$t('group', {count: nrGroups})}}</p>
      </div>
    </div>
  </div>
</template>

<script>
  import {SET_FILTER, SET_SORT} from '../store/mutations'
  import { mapState, mapGetters, mapMutations } from 'vuex'

  export default {
    name: 'members-grid-controls',
    computed: {
      ...mapState(['sort', 'filter']),
      ...mapGetters(['nrMembers', 'nrGroups', 'contextId']),
      query: {
        get () {
          return this.filter
        },
        set (filter) {
          this.setFilter(filter)
        }
      }
    },
    methods: {
      ...mapMutations({'setSort': SET_SORT, 'setFilter': SET_FILTER}),
      toggleSort: function () {
        this.setSort(this.sort === 'ascending' ? 'descending' : 'ascending')
      },
      addMembers () {
        this.$router.push(`/${this.contextId}/create`)
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
