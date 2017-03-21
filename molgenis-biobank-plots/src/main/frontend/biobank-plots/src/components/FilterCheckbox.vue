<template>
  <b-form-checkbox v-model="value">{{label}}</b-form-checkbox>
</template>

<script>
  export default {
    name: 'filter-checkbox',
    props: ['name', 'label'],
    computed: {
      value: {
        get: function () {
          return this.$store.state[this.name]
        },
        set: function (checked) {
          this.$store.dispatch('setFilterAsync', {name: this.name, value: checked})
        }
      }
    }
  }
</script>
