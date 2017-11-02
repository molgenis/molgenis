<template>
  <div class="card border-0">
    <div class="card-block">
      <div class="row">
        <div class="col text-center">
          <i :class="['fa', 'fa-4x', type === 'user' ? 'fa-user' : 'fa-users', { future }]" aria-hidden="true"
             @click="editMember"></i>
          <p>{{ label }}<br>({{ role.label }})</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import moment from 'moment'
  import { mapGetters } from 'vuex'

  export default {
    name: 'members-grid-body-card',
    props: {
      type: {
        type: String,
        required: true,
        validator (type) {
          return ['user', 'group'].indexOf(type) >= 0
        }
      },
      id: {
        type: String,
        required: true
      },
      label: {
        type: String,
        required: true
      },
      role: {
        id: {
          type: String,
          required: true
        }
      },
      from: {
        type: String,
        required: false
      }
    },
    computed: {
      ...mapGetters(['contextId']),
      future () {
        return this.from && moment(this.from).isAfter(moment())
      }
    },
    methods: {
      editMember () {
        this.$router.push({name: 'edit', params: {groupId: this.contextId, membershipId: this.id}})
      }
    }
  }
</script>

<style scoped>
  i.fa {
    cursor: pointer;
  }

  i.future {
    color: #aaa;
  }
</style>
