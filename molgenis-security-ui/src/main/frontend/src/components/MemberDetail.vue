<template>
  <div class="container">

    <toast></toast>

    <div class="row mb-3  ">
      <div class="col">
        <nav aria-label="breadcrumb">
          <ol class="breadcrumb">
            <li class="breadcrumb-item">
              <router-link :to="{ name: 'groupOverView' }" class="text-capitalize">{{ 'security-ui-breadcrumb-groups' |
                i18n }}
              </router-link>
            </li>
            <li class="breadcrumb-item">
              <router-link :to="{ name: 'groupDetail', params: { name: groupName } }" class="text-capitalize">{{groupName}}</router-link>
            </li>
            <li class="breadcrumb-item active text-capitalize" aria-current="page">{{memberName}}</li>
          </ol>
        </nav>
      </div>
    </div>

    <div class="row mb-3  ">
      <div class="col">
        <h1>{{'security-ui-member-details-header' | i18n}}</h1>
      </div>
    </div>

    <div class="row">
      <div class="col-md-6">
        <h5 class="font-weight-light">{{'security-ui-membership-attribute-user' | i18n}}</h5>
        <h5 v-if="member" class="pl-3">{{memberName}}</h5>
      </div>
    </div>

    <hr/>

    <div class="row">
      <div class="col-md-3">
        <h5 class="font-weight-light">{{'security-ui-membership-attribute-role' | i18n}}</h5>
        <h5 v-if="member && !isEditRoleMode" class="pl-3">{{member.roleLabel}}</h5>
        <form v-else-if="isEditRoleMode">

          <div v-for="role in sortedRoles" class="form-check" >
            <input class="form-check-input" type="radio" name="roleRadio"
                   :id="role.roleName" :value="role.roleName" v-model="selectedRole" >
            <label class="form-check-label" :for="role.roleName">
              {{role.roleLabel}}
            </label>
          </div>

        </form>
      </div>
      <div v-if="!isEditRoleMode" class="col-md-9 ">
        <button
          id="edit-role-btn"
          v-if="canUpdateMember"
          class="btn btn-sm btn-outline-secondary"
          type="button"
          @click="onEditRole">
          <i class="fa fa-edit"></i>
          {{'security-ui-btn-edit' | i18n}}
        </button>
      </div>
      <div v-else class="col-md-9 ">
        <button
          id="update-cancel-btn"
          class="btn btn-sm btn-secondary"
          type="button"
          @click.prevent="isEditRoleMode = !isEditRoleMode">
          {{'security-ui-btn-cancel' | i18n}}
        </button>

        <button
          v-if="!isUpdating"
          id="update-btn"
          class="btn btn-sm btn-primary"
          type="button"
          :disabled="selectedRole === member.roleName"
          @click="onUpdateMember">
          Update role
        </button>

        <button
          v-else
          id="update-btn-saving"
          class="btn btn-sm btn-primary"
          type="button"
          disabled="disabled">
          Updating role <i class="fa fa-spinner fa-spin "></i>
        </button>
      </div>
    </div>

    <hr/>

    <template v-if="canRemoveMember">
      <button
        v-if="!isRemoving"
        id="remove-btn"
        class="btn btn-danger"
        type="button"
        @click.prevent="onRemoveMember"
        :disabled="isEditRoleMode">
        {{'security-ui-member-remove' | i18n}}
      </button>

      <button
        v-else
        id="remove-btn-removing"
        class="btn btn-danger"
        type="button"
        disabled="disabled">
        {{'security-ui-member-removing' | i18n}} <i class="fa fa-spinner fa-spin "></i>
      </button>
    </template>


  </div>
</template>

<script>
  import { mapGetters } from 'vuex'
  import Toast from './Toast'

  export default {
    name: 'MemberDetail',
    props: {
      groupName: {
        type: String,
        required: true
      },
      memberName: {
        type: String,
        required: true
      }
    },
    data () {
      return {
        isRemoving: false,
        isUpdating: false,
        isEditRoleMode: false,
        selectedRole: ''
      }
    },
    computed: {
      ...mapGetters([
        'groupRoles',
        'groupMembers',
        'groupPermissions'
      ]),
      member () {
        const members = this.groupMembers[this.groupName] || []
        return members.find(m => m.username === this.memberName)
      },
      sortedRoles () {
        if (!this.groupRoles || !this.groupRoles[this.groupName]) {
          return []
        }
        return [...this.groupRoles[this.groupName]].sort((a, b) => a.roleLabel.localeCompare(b.roleLabel))
      },
      canRemoveMember () {
        const permissions = this.groupPermissions[this.groupName] || []
        return permissions.includes('REMOVE_MEMBERSHIP')
      },
      canUpdateMember () {
        const permissions = this.groupPermissions[this.groupName] || []
        return permissions.includes('UPDATE_MEMBERSHIP')
      }
    },
    methods: {
      onEditRole () {
        this.selectedRole = this.member.roleName
        this.isEditRoleMode = !this.isEditRoleMode
      },
      onRemoveMember () {
        this.isRemoving = !this.isRemoving
        this.$store.dispatch('removeMember', {groupName: this.groupName, memberName: this.memberName})
          .then(() => {
            this.$router.push({ name: 'groupDetail', params: { name: this.groupName } })
          }, () => {
            this.isRemoving = !this.isRemoving
          })
      },
      onUpdateMember () {
        this.isUpdating = !this.isUpdating
        const updateMemberCommand = { roleName: this.selectedRole }
        this.$store.dispatch('updateMember', {groupName: this.groupName, memberName: this.memberName, updateMemberCommand})
          .then(() => {
            this.$router.push({ name: 'groupDetail', params: { name: this.groupName } })
          }, () => {
            this.isUpdating = !this.isUpdating
          })
      }
    },
    created () {
      if (!this.groupMembers[this.groupName]) {
        this.$store.dispatch('fetchGroupMembers', this.groupName)
      }
      if (!this.groupRoles[this.groupName]) {
        this.$store.dispatch('fetchGroupRoles', this.groupName)
      }
      if (!this.groupPermissions[this.groupName]) {
        this.$store.dispatch('fetchGroupPermissions', this.groupName)
      }
    },
    components: {
      Toast
    }
  }
</script>
