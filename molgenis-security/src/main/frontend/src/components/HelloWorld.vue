<template>
  <div class="row">
    <div class="col text-center">
      <h1>This is the MOLGENIS HelloWorld Component</h1>
      <ul class="list-group">
        <li v-for="sid in sids"
            class="list-group-item"
            :class="{'selected': selectedSid && (sid.authority === selectedSid.authority)}"
            @click="setSelectedSid(sid)">
          {{ sid.authority }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script>
  import { mapState, mapMutations } from 'vuex'
  import { SET_SELECTED_SID } from '../store/mutations'

  export default {
    name: 'hello-world',
    methods: {
      ...mapMutations({'setSelectedSid': SET_SELECTED_SID})
    },
    computed: {
      ...mapState(['sids', 'selectedSid'])
    }
  }
</script>

<style scoped="local">
  li.selected {
    background-color: red
  }
</style>
