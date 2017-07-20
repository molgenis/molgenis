<template>
  <table class="table table-sm" v-if="acls.length">
    <thead>
    <tr>
      <th>{{'ROW' | i18n}}</th>
      <th>{{'OWNER' | i18n}}</th>
      <th :colspan="permissions.length + 1" class="text-center">{{'PERMISSIONS' | i18n}}</th>
    </tr>
    <tr>
      <th></th>
      <th></th>
      <th class="text-center">{{'GRANTING' | i18n}}</th>
      <th v-for="permission in permissions" class="text-center">{{permission | capitalizeFirstLetter}}</th>
    </tr>
    </thead>
    <tbody>
    <tr v-for="(acl, rowIndex) in acls">
      <td>{{acl.entityLabel || acl.entityId}}</td>
      <td>{{acl.owner}}</td>
      <td v-if="acl.granting" class="text-center"><i class="fa fa-unlock"></i></td>
      <td v-else class="text-center"><i class="fa fa-lock"></i></td>
      <td v-for="permission in permissions" class="text-center">
        <div class="form-check">
          <label class="form-check-label">
            <input class="form-check-input"
                   type="checkbox"
                   :checked="acl[permission]"
                   @click="onToggle({rowIndex, aceIndex: acl.aceIndex, permission})">
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
      onPermissionClick: {type: Function},
      onSave: {type: Function}
    },
    methods: {
      onToggle ({rowIndex, aceIndex, permission}) {
        this.onPermissionClick({rowIndex, aceIndex, permission})
        this.onSave(rowIndex)
      }
    },
    filters: {
      capitalizeFirstLetter (string: string): string { return string.charAt(0).toUpperCase() + string.slice(1).toLowerCase() }
    }
  }
</script>
