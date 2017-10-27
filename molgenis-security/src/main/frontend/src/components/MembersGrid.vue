<template>
  <div class="row mb-2">
    <div class="col">
      <members-grid-controls></members-grid-controls>
      <members-grid-body></members-grid-body>
    </div>
  </div>
</template>

<script>
  import MembersGridControls from './MembersGridControls'
  import MembersGridBody from './MembersGridBody'

  export default {
    name: 'members-grid',
    components: {
      MembersGridControls,
      MembersGridBody
    }
  }
</script>
