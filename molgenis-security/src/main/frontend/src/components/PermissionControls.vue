<template>
  <div class="row">
    <div class="col">
      <button type="button" class="btn btn-secondary" @click="showRolePermissions">Role permissions</button>
      <button type="button" class="btn btn-secondary" @click="showPermissionHistory">Permission history</button>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'permission-controls',
    methods: {
      showRolePermissions () {
        alert('TODO navigate to role permissions screen')
      },
      showPermissionHistory () {
        alert('TODO navigate to permission history screen')
      }
    }
  }
</script>

<style scoped>
  button {
    cursor: pointer;
  }
</style>
