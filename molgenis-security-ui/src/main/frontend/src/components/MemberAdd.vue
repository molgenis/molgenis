<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <h1>Add member</h1>
      </div>
    </div>

    <div class="row">
      <div class="col-md-6">
        <form>

          <div class="form-group">
            <label for="userSelect">User</label>
            <select id="userSelect" v-model="userId" class="form-control">
              <option value="abc">ABC</option>
            </select>
          </div>

          <div class="form-group">
            <label for="roleSelect">Role in group</label>
            <select id="roleSelect" v-model="roleId" class="form-control">
              <option value="abc">ABC</option>
            </select>
          </div>

          <router-link :to="{name: 'groupDetail', params: { name: groupName }}">
            <a href="#" class="btn btn-secondary" role="button">Cancel</a>
          </router-link>

          <button
            v-if="!isAdding"
            id="create-btn"
            class="btn btn-success"
            type="submit"
            @click.prevent="onSubmit"
            :disabled="!userId && !roleId">
            Add Member
          </button>

          <button
            v-else
            id="save-btn-saving"
            class="btn btn-primary"
            type="button"
            disabled="disabled">
            Create <i class="fa fa-spinner fa-spin "></i>
          </button>

        </form>
      </div>
    </div>


  </div>
</template>

<script>
  import Toast from './Toast'

  export default {
    name: 'MemberAdd',
    props: {
      groupName: {
        type: String,
        required: false
      }
    },
    data () {
      return {
        userId: '',
        roleId: '',
        isAdding: false
      }
    },
    computed: {
    },
    methods: {
      onSubmit () {
        this.isAdding = !this.isAdding
        const addMemberCommand = { groupIdentifier: this.groupName, userId: this.userId, roleId: this.roleId }
        this.$store.dispatch('addMember', addMemberCommand)
          .then(() => {
            this.$router.push({ name: 'groupDetail', params: { name: this.groupName } })
          }, () => {
            this.isAdding = !this.isAdding
          })
      }
    },
    components: {
      Toast
    }
  }
</script>
