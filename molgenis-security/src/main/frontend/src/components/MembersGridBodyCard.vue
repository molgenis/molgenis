<template>
  <div class="card border-0">
    <div class="card-block">
      <div class="row">
        <div class="col text-center">
          <i :class="['fa', type === 'user' ? 'fa-user' : 'fa-users', 'fa-4x']" aria-hidden="true"
             @click="editMember(type, id)"></i>
          <p>{{ label }}<br>({{ role.label }})</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
  import {mapState} from 'vuex'

  export default {
    name: 'members-grid-body-card',
    props: {
      type: {
        type: String,
        required: true,
        validator(type) {
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
        type: Object,
        required: true
      }
    },
    computed: {
      ...mapState(['roles', 'usersGroups'])
    },
    methods: {
      editMember(type, id) {
        this.$router.push('/edit/' + type + '/' + id)
      }
    }
  }
</script>

<style scoped>
  i.fa {
    cursor: pointer;
  }
</style>
