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
      <th>{{'GRANTING' | i18n}}</th>
      <th v-for="permission in permissions">{{permission | capitalizeFirstLetter}}</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(acl, rowIndex) in acls">
      <td>{{acl.entityLabel || acl.entityId}}</td>
      <td>{{acl.owner}}</td>
      <td v-if="acl.granting"><i class="fa fa-unlock"></i></td>
      <td v-else><i class="fa fa-lock"></i></td>
      <td v-for="permission in permissions">
        <div class="form-check">
          <label class="form-check-label">
            <input class="form-check-input"
                   type="checkbox"
                   :checked="acl[permission]"
                   @click="onClick({rowIndex, aceIndex: acl.aceIndex, permission})">
          </label>
        </div>
      </td>
    </tr>
    </tbody>
  </table>
</template>

<script>
  export default {
    name: 'acls',
    props: {
      permissions: {type: Array},
      acls: {type: Array},
      onClick: {type: Function}
    },
    filters: {
      capitalizeFirstLetter (string: string): string { return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase() }
    }
  }
</script>
