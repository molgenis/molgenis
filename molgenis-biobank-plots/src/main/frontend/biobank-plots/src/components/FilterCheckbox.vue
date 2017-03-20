<template>
  <b-form-checkbox :checked="value" @change="setFilter" v-bind:inline="false">{{label}}</b-form-checkbox>
</template>

<script>
  export default {
    props: ['name', 'label'],
    name: 'filter-checkbox',
    computed: {
      value: function () {
        return this.$store.state[this.name]
      }
    },
    methods: {
      setFilter (checked) {
        console.log('setFilter', this.name, checked)
        this.$store.dispatch('setFilterAsync', {name: this.name, value: checked})
      }
    }
  }
</script>
