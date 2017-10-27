<template>
  <div class="row" v-if="updatedMember && roles">
    <div class="col">
      <form @submit.prevent="onSubmit">
        <div class="form-group">
          <label for="memberSelect">User or group</label>
          <select v-model="updatedMember.id" class="form-control" id="memberSelect" required disabled>
            <option value="group0">Authenticated user</option>
            <option value="group1">Anonyous users</option>
            <option value="group2">BBMRI-ERIC directory</option>
            <option value="user0">David van Enckevort</option>
            <option value="user1">Morris Swertz</option>
            <option value="user2">Mariska Slofstra</option>
            <option value="user3">Marieke Bijlsma</option>
            <option value="user4">Remco den Ouden</option>
          </select>
        </div>
        <div class="form-group">
          <label for="roleSelect">Role</label>
          <select v-model="updatedMember.role" class="form-control" id="roleSelect" required>
            <option v-for="role in roles" :value="role.id">{{ role.label }}</option>
          </select>
        </div>
        <div class="form-group">
          <label for="fromDate">From</label>
          <input v-model="updatedMember.from" type="datetime-local" class="form-control" id="fromDate" required>
        </div>
        <div :class="['form-group', untilDateBeforeFromDateError ? 'has-danger' : '']">
          <label for="untilDate">Until</label>
          <input v-model="updatedMember.until" type="datetime-local" class="form-control" id="untilDate">
          <div v-if="untilDateBeforeFromDateError" class="form-control-feedback">
            Until date must be after the from date
          </div>
        </div>
        <button type="submit" class="btn btn-success">Save</button>
        <button v-if="isDeletable" type="button" class="btn btn-danger" @click="removeMember">Remove</button>
      </form>
    </div>
  </div>
</template>

<script>
  import {GET_MEMBER, UPDATE_MEMBER, DELETE_MEMBER, GET_ROLES} from '../store/actions'
  import {mapGetters} from 'vuex'
  import moment from 'moment'

  export default {
    name: 'member-edit',
    data: function () {
      return {
        updatedMember: null,
        untilDateBeforeFromDateError: false
      }
    },
    computed: {
      ...mapGetters({
        member: 'getMember',
        roles: 'getRoles'
      }),
      isDeletable: function () {
        return this.updatedMember && (this.updatedMember.id !== 'group0' && this.updatedMember.id !== 'group1')
      }
    },
    methods: {
      fetchData: function () {
        this.$store.dispatch(GET_ROLES)
        this.$store.dispatch(GET_MEMBER, this.$route.params.id)
      },
      onSubmit: function () {
        if (this.updatedMember.until && moment(this.updatedMember.until, 'YYYY-MM-DD[T]HH:mm').isBefore(moment(this.updatedMember.from, 'YYYY-MM-DD[T]HH:mm'))) {
          this.untilDateBeforeFromDateError = true
        } else {
          this.$store.dispatch(UPDATE_MEMBER, this.updatedMember).then(() => this.$router.go(-1))
        }
      },
      removeMember: function () {
        this.$store.dispatch(DELETE_MEMBER, this.updatedMember).then(() => this.$router.go(-1))
      }
    },
    created() {
      this.fetchData()
    },
    watch: {
      '$route': 'fetchData',
      member: function () {
        this.updatedMember = JSON.parse(JSON.stringify(this.member))
      }
    }
  }
</script>

<style scoped>
  button {
    cursor: pointer;
  }
</style>
