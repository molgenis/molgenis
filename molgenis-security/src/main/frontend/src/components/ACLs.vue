<template>
  <table class="table table-sm" v-if="acls.length">
    <thead>
    <tr>
      <th>{{'ROW' | i18n}}</th>
      <th>{{'OWNER' | i18n}}</th>
      <th :colspan="permissions.length + 1">{{'PERMISSIONS' | i18n}}</th>
    </tr>
    <tr>
      <th></th>
      <th></th>
      <th>{{'GRANTED' | i18n}}</th>
      <th v-for="permission in permissions">{{permission | capitalizeFirstLetter}}</th>
    </tr>
    </thead>
    <tbody>
    <template v-for="acl in acls">
      <tr v-for="(ace, index) in acl.aces">
        <td>
          <span v-if="index == 0">{{acl.entityLabel || acl.entityId}}</span>
        </td>
        <td>
          <span v-if="index == 0">{{ index == 0 && acl.owner.username }}</span>
        </td>
        <td v-if="ace.granted"><i class="fa fa-check"></i></td>
        <td v-else><i class="fa fa-ban"></i></td>
        <td v-for="permission in permissions">
          <div class="form-check">
            <label class="form-check-label">
              <input class="form-check-input" type="checkbox"
                     :checked="ace.permissions.indexOf(permission) >= 0">
            </label>
          </div>
        </td>
      </tr>
    </template>
    </tbody>
  </table>
</template>

<script>
  export default {
    name: 'acls',
    props: {
      permissions: {
        type: Array
      },
      acls: {
        type: Array
      }
    },
    filters: {
      capitalizeFirstLetter (string: string): string { return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase() }
    }
  }
</script>
