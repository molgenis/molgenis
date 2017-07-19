<template>
  <ul class="list-group">
    <li v-for="sid in sids"
        class="list-group-item"
        :class="{'active': selectedSids.includes(sid)}"
        @click="toggleSid(sid)">
      {{ sid.authority }}
    </li>
  </ul>
</template>

<script>

  export default {
    name: 'sids',
    props: {
      'sids': {
        type: Array
      },
      'selectedSids': {
        type: Array
      },
      'toggleSid': {
        type: Function
      }
    }
  }
</script>
