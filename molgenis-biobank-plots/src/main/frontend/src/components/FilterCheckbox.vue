<template>
  <b-form-checkbox v-model="value" class="form-control-sm">{{label}}</b-form-checkbox>
</template>

<script>
  import { SET_FILTER_ASYNC } from '../store/actions'

  export default {
    name: 'filter-checkbox',
    props: ['name', 'label'],
    computed: {
      value: {
        get: function () {
          return this.$store.state[this.name]
        },
        set: function (checked) {
          this.$store.dispatch(SET_FILTER_ASYNC, {name: this.name, value: checked})
        }
      }
    }
  }
</script>
