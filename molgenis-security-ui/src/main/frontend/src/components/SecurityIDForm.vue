<template>
  <div>
    <b-btn v-b-modal.sidFormModal variant="success"><i class="fa fa-plus"></i></b-btn>
    <b-modal id="sidFormModal"
             :title="'SELECT_SID'|i18n"
             @ok="handleOK">
      <form>
        <b-form-radio v-model="type"
                      :options="typeOptions"></b-form-radio>
        <b-form-select :options="sidOptions" v-model="selectedSid"></b-form-select>
      </form>
    </b-modal>
  </div>
</template>

<script>
  export default {
    computed: {
      typeOptions () {
        return [{text: this.$t('ROLE'), value: 'role', disabled: this.roles.length === 0},
          {text: this.$t('USER'), value: 'user', disabled: this.users.length === 0}]
      },
      sidOptions () {
        switch (this.type) {
          case 'role':
            return this.roles.map(role => ({ text: role.label, value: role.id }))
          case 'user':
            return this.users.map(user => user.username)
          default:
            return []
        }
      },
      result () {
        if (!this.selectedSid) {
          return null
        }
        switch (this.type) {
          case 'role':
            return { authority: this.selectedSid }
          case 'user':
            return { username: this.selectedSid }
        }
      }
    },
    methods: {
      handleOK () {
        if (this.selectedSid) {
          this.submit(this.result)
        }
        this.selectedSid = undefined
      },
      selectSid () {
        if (this.roles.length) {
          this.type = 'role'
          this.selectedSid = this.roles[0].id
        } else {
          this.type = 'user'
          this.selectedSid = this.users[0].username
        }
      }
    },
    data () {
      return this.roles.length
        ? {type: 'role', selectedSid: this.roles[0].id}
        : {type: 'user', selectedSid: this.users[0].username}
    },
    watch: {
      users () {
        this.selectSid()
      },
      roles () {
        this.selectSid()
      }
    },
    props: {
      users: {type: Array},
      roles: {type: Array},
      submit: {type: Function}
    }
  }
</script>
