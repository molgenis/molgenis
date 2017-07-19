<template>
  <ul class="list-group">
    <li v-for="sid in sids"
        class="list-group-item"
        :class="{'active': selectedSid === sid.authority}"
        @click="selectSid(sid)">
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
      'selectedSid': {
        type: String
      },
      'selectSid': {
        type: Function
      }
    }
  }
</script>
