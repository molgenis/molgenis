<template>
  <b-form-input v-model.number="value" type="number" :min="min" :max="max"></b-form-input>
</template>

<script>
  export default {
    name: 'filter-text-input',
    props: ['name', 'min', 'max'],
    computed: {
      value: {
        get: function () {
          return this.$store.state[this.name]
        },
        set: function (value) {
          this.$store.dispatch('setFilterAsync', {name: this.name, value})
        }
      }
    }
  }
</script>
