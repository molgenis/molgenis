<template>
  <b-form-checkbox :checked="value" @change="setFilter">{{label}}</b-form-checkbox>
</template>

<script>
  export default {
    name: 'filter-checkbox',
    props: ['name', 'label'],
    computed: {
      value: function () {
        return this.$store.state[this.name]
      }
    },
    methods: {
      setFilter (checked) {
        this.$store.dispatch('setFilterAsync', {name: this.name, value: checked})
      }
    }
  }
</script>
