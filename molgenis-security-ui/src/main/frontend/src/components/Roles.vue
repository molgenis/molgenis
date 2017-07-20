<template>
  <div class="list-group">
    <a v-for="role in roles"
       class="list-group-item list-group-item-action flex-column align-items-start"
       :class="{'active': selectedRole === role.id}"
       @click="selectRole(role.id)"
       href="#">
      <div class="d-flex w-100 justify-content-between">
        <h5 class="mb-1">{{ role.label }}</h5>
      </div>
      <small v-if="role.description && selectedRole === role.id">{{ role.description }}</small>
    </a>
  </div>
</template>
"
<script>

  export default {
    name: 'roles',
    props: {
      'roles': {
        type: Array
      },
      'selectedRole': {
        type: String
      },
      'selectRole': {
        type: Function
      }
    }
  }
</script>
