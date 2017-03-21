<template>
  <form>
    <h1>Filter</h1>
    <h4>Data types</h4>
    <div class="custom-controls-stacked">
      <filter-checkbox name="rnaseq" label="RNASeq"></filter-checkbox>
      <filter-checkbox name="DNAm" label="DNAm"></filter-checkbox>
      <filter-checkbox name="DNA" label="DNA"></filter-checkbox>
      <filter-checkbox name="wbcc" label="White bloodcell count"></filter-checkbox>
      <filter-checkbox name="metabolomics" label="Metabolomics"></filter-checkbox>
    </div>
    <h4>Gender</h4>
    <div class="custom-controls-stacked">
      <filter-checkbox name="male" label="Male"></filter-checkbox>
      <filter-checkbox name="female" label="Female"></filter-checkbox>
    </div>
    <h4>Smoking</h4>
    <div class="custom-controls-stacked">
      <filter-checkbox name="smoking" label="Yes"></filter-checkbox>
      <filter-checkbox name="nonSmoking" label="No"></filter-checkbox>
    </div>
    <h4>Age</h4>
    From to
    <h4>Biobank</h4>
    <select v-model="selectedBiobank" class="mb-3">
      <option v-for="biobank in biobankOptions" :value='biobank.value'>{{biobank.text}}</option>
    </select>
  </form>
</template>

<script>
  import FilterCheckbox from './FilterCheckbox'
  import {mapState} from 'vuex'

  export default {
    self: this,
    components: { filterCheckbox: FilterCheckbox },
    computed: {
      ...mapState({
        biobankOptions: state => state.biobanks.map(biobank => ({
          text: biobank.abbr,
          value: biobank.abbr
        }))
      }),
      selectedBiobank: {
        get () {
          return this.$store.state.biobank
        },
        set (biobank) {
          this.$store.commit('setFilter', {name: 'biobank', value: biobank})
        }
      }
    },
    created () {
      this.$store.dispatch('getBiobanks')
      this.$store.commit('setFilter', {name: 'DNA', value: false})
    },
    name: 'filters'
  }
</script>
