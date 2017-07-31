<template>
  <div>
    <div class="row" v-if="sidType == 'role'">
      <div class="col-12">
        <button type="button" class="btn btn-success" @click="createRole()"><i class="fa fa-plus"></i></button>
        <button v-if="selectedRole" type="button" class="btn btn-default" @click="updateRole(selectedRole)"><i class="fa fa-pencil"></i></button>
        <button v-if="selectedRole" type="button" class="btn btn-danger" @click="onDeleteRole(selectedRole)"><i class="fa fa-trash"></i></button>
      </div>
    </div>
    <div class="row">
      <div class="col-12 mt-2">
        <div class="list-group">
          <a v-for="role in roles"
             class="list-group-item list-group-item-action flex-column align-items-start"
             :class="{'active': selectedRole === (sidType == 'role' ? role.id : role.username)}"
             @click="selectRole(sidType == 'role' ? role.id : role.username)"
             href="#">
            <div class="d-flex w-100 justify-content-between">
              <h5 class="mb-1">{{ sidType == 'role' ? role.label : role.username }}</h5>
            </div>
            <small v-if="role.description && selectedRole === (sidType == 'role' ? role.id : role.username)">
              {{ role.description }}
            </small>
          </a>
        </div>
      </div>
    </div>
  </div>
</template>

<script>

  export default {
    name: 'roles',
    props: {
      'sidType': {
        type: String
      },
      'roles': {
        type: Array
      },
      'selectedRole': {
        type: String
      },
      'selectRole': {
        type: Function
      },
      'doCreateRole': {
        type: Boolean
      },
      'doUpdateRole': {
        type: Boolean
      },
      'createRole': {
        type: Function
      },
      'updateRole': {
        type: Function
      },
      'onDeleteRole': {
        type: Function
      }
    }
  }
</script>

<style scoped>
  button:hover {
    cursor: pointer
  }
</style>
