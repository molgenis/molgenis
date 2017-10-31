<template>
  <div class="card border-0">
    <div class="card-block">
      <div class="row">
        <div class="col text-center">
          <i :class="['fa', type === 'user' ? 'fa-user' : 'fa-users', 'fa-4x']" aria-hidden="true"
             @click="editMember(id)"></i>
          <p>{{ label }}<br>({{ role }})</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
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
        type: String,
        required: true
      }
    },
    computed: {...mapGetters(['contextId'])},
    methods: {
      editMember (id) {
        this.$router.push({name: 'edit', params: {groupId: this.contextId, membershipId: id}})
      }
    }
  }
</script>

<style scoped>
  i.fa {
    cursor: pointer;
  }
</style>
