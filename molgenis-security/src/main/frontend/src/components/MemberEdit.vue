<template>
  <div class="row">
    <div class="col">
      <form @submit.prevent="onSubmit">
        <div class="form-group" v-if="edit">
          <label for="memberSelect">{{type | upperFirst}}</label>
          <p class="form-control-static">{{label}}</p>
        </div>
        <div class="form-group" v-else>
          <label for="memberSelect">User or Group</label>
          <select v-model="id" class="form-control" id="memberSelect" required>
            <optgroup label="Groups">
              <option v-for="role in unassignedGroups" :value="role.id">{{ role.label }}</option>
            </optgroup>
            <optgroup label="Users">
              <option v-for="user in unassignedUsers" :value="user.id">{{ user.label }}</option>
            </optgroup>
          </select>
        </div>
        <div class="form-group">
          <label for="roleSelect">Role</label>
          <select v-model="role" class="form-control" id="roleSelect" required>
            <option v-for="role in roleOptions" :value="role.id">{{ role.label }}</option>
          </select>
        </div>
        <div v-show="!groupSelected">
          <div class="form-group">
            <label for="fromDate">From</label>
            <input v-model="from" type="date" class="form-control" id="fromDate" required>
          </div>
          <div class="form-group" :class="{'has-danger': untilDateBeforeFromDateError}" v-show="!groupSelected">
            <label for="untilDate">Until</label>
            <input v-model="until" type="date" class="form-control" id="untilDate">
            <div v-if="untilDateBeforeFromDateError" class="form-control-feedback">
              Until date must be after the from date
            </div>
          </div>
        </div>
        <button type="submit" class="btn btn-success" :disabled="untilDateBeforeFromDateError">Save</button>
        <button v-if="edit" type="button" class="btn btn-danger" @click="deleteMember">Remove</button>
      </form>
    </div>
  </div>
</template>

<script>
  import { CREATE_MEMBER, DELETE_MEMBER } from '../store/actions'
  import { mapGetters, mapState, mapActions } from 'vuex'
  import moment from 'moment'
  import _ from 'lodash'

  export default {
    name: 'member-create',
    created () {
      if (this.member) {
        this.edit = true
        this.type = this.member.type
        this.id = this.member.id
        this.label = this.member.label
        this.role = this.member.role.id
//        this.from = this.member.from TODO: update if in future
        if (this.member.until) {
          this.until = this.member.until.substring(0, 10)
        }
      }
    },
    data: function () {
      return {
        id: null,
        role: null,
        from: moment().format('YYYY-MM-DD'),
        until: null,
        edit: false
      }
    },
    computed: {
      ...mapState(['users']),
      ...mapGetters(['members', 'unassignedUsers', 'unassignedGroups', 'context', 'member', 'roles', 'context']),
      roleOptions () { return this.groupSelected ? this.roles : this.context.children },
      groupSelected () { return this.type === 'group' || this.unassignedGroups.some(role => role.id === this.id) },
      untilDate () { return this.until && moment(this.until, 'YYYY-MM-DD') },
      fromDate () { return this.from && moment(this.from, 'YYYY-MM-DD') },
      untilDateBeforeFromDateError () {
        return !!this.untilDate && this.untilDate.isSameOrBefore(this.fromDate)
      }
    },
    methods: {
      ...mapActions({createMember: CREATE_MEMBER, deleteMember: DELETE_MEMBER}),
      onSubmit () {
        this.createMember({
          id: this.id,
          role: this.role,
          until: this.until,
          from: this.from
        }).then(() => this.$router.go(-1))
      }
    },
    filters: {
      upperFirst: _.upperFirst
    }
  }
</script>

<style scoped>
  button {
    cursor: pointer;
  }
</style>
