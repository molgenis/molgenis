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
            <flat-pickr v-model="from" id="fromDate" :required="true" :config="{allowInput: true}"></flat-pickr>
          </div>
          <div class="form-group" :class="{'has-danger': untilDateBeforeFromDateError}" v-show="!groupSelected">
            <label for="untilDate">Until</label>
            <flat-pickr v-model="until" id="untilDate" :required="false" :config="{allowInput: true}"></flat-pickr>
            <div v-if="untilDateBeforeFromDateError" class="form-control-feedback">
              Until date must be after the from date
            </div>
          </div>
        </div>
        <button type="submit" class="btn btn-success" :disabled="untilDateBeforeFromDateError">Save</button>
        <button v-if="edit" type="button" class="btn btn-danger" @click="onRemove">Remove</button>
      </form>
    </div>
  </div>
</template>

<script>
  import { CREATE_MEMBER, DELETE_MEMBER, UPDATE_GROUP_ROLE, DELETE_GROUP_ROLE } from '../store/actions'
  import { mapGetters, mapState, mapActions } from 'vuex'
  import moment from 'moment'
  import _ from 'lodash'
  import FlatPickr from 'vue-flatpickr-component'
  import 'flatpickr/dist/flatpickr.css'

  export default {
    name: 'member-create',
    created () {
      if (this.member) {
        this.edit = true
        this.type = this.member.type
        this.id = this.member.id
        this.label = this.member.label
        this.role = this.member.role.id
        if (this.member.from) {
          this.from = moment.max(moment(this.member.from), moment()).startOf('day').format('YYYY-MM-DD')
        }
        if (this.member.until) {
          this.until = moment(this.member.until).startOf('day').format('YYYY-MM-DD')
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
      ...mapActions({
        createMember: CREATE_MEMBER,
        deleteMember: DELETE_MEMBER,
        updateGroupRole: UPDATE_GROUP_ROLE,
        deleteGroupRole: DELETE_GROUP_ROLE
      }),
      onRemove () {
        if (this.groupSelected) {
          this.deleteGroupRole({
            groupId: this.id,
            roleId: this.role
          }).then(() => this.$router.go(-1))
        } else {
          this.deleteMember({userId: this.id, groupId: this.role}).then(() => this.$router.go(-1))
        }
      },
      onSubmit () {
        if (this.untilDateBeforeFromDateError) {
          return
        }
        if (this.groupSelected) {
          this.updateGroupRole({
            groupId: this.id,
            roleId: this.role
          }).then(() => this.$router.go(-1))
        } else {
          const mutation = {
            userId: this.id,
            groupId: this.role,
            start: this.from,
            stop: this.until || null
          }
          this.createMember(mutation).then(() => this.$router.go(-1))
        }
      }
    },
    components: {
      FlatPickr
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
