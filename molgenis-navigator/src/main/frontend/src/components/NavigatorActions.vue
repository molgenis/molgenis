<template>
  <div class="row">
    <div class="col">
      <NavigatorActionsCreateEdit class="button-last"/>
      <NavigatorActionsClipboard class="button-last"/>
      <NavigatorActionsTransfer class="button-last"/>
      <div class="float-right">
        <NavigatorActionsDelete/>
      </div>
    </div>
    <NavigatorModalPackageCreate/>
    <NavigatorModalPackageUpdate/>
    <NavigatorModalResourceDelete/>
  </div>
</template>

<script>
import NavigatorActionsCreateEdit from './NavigatorActionsCreateEdit'
import NavigatorActionsTransfer from './NavigatorActionsTransfer'
import NavigatorActionsClipboard from './NavigatorActionsClipboard'
import NavigatorActionsDelete from './NavigatorActionsDelete'
import NavigatorModalPackageCreate from './NavigatorModalPackageCreate'
import NavigatorModalPackageUpdate from './NavigatorModalPackageUpdate'
import NavigatorModalResourceDelete from './NavigatorModalResourceDelete'

export default {
  name: 'NavigatorActions',
  components: {
    NavigatorActionsCreateEdit,
    NavigatorActionsTransfer,
    NavigatorActionsClipboard,
    NavigatorActionsDelete,
    NavigatorModalPackageCreate,
    NavigatorModalPackageUpdate,
    NavigatorModalResourceDelete
  }
}
</script>

<style scoped>
  .button-last {
    margin-right: 1rem;
  }
</style>
