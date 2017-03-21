<template>
  <b-card show-header show-footer>

    <div slot="header">
      <div class="row">
        <div class="col-4">
          <h3>Filters</h3>
        </div>
        <div class="col-8 text-right">
          <b-button size="sm" @click="resetFilters">Reset filters</b-button>
        </div>
      </div>
    </div>

    <form>
      <h4>Data types</h4>
      <div class="custom-controls-stacked">
        <filter-checkbox name="rnaseq" label="Transcriptome (RNAseq)"></filter-checkbox>
        <filter-checkbox name="DNAm" label="Methylome (Illumina 450K)"></filter-checkbox>
        <filter-checkbox name="DNA" label="DNA"></filter-checkbox>
        <filter-checkbox name="wbcc" label="White bloodcell count"></filter-checkbox>
        <filter-checkbox name="metabolomics" label="Metabolome (Brainshake)"></filter-checkbox>
      </div>

      <hr>
      <h4>Gender</h4>
      <div class="custom-controls-stacked">
        <filter-checkbox name="male" label="Male"></filter-checkbox>
        <filter-checkbox name="female" label="Female"></filter-checkbox>
      </div>

      <hr>
      <h4>Smoking</h4>
      <div class="custom-controls-stacked">
        <filter-checkbox name="smoking" label="Yes"></filter-checkbox>
        <filter-checkbox name="nonSmoking" label="No"></filter-checkbox>
      </div>

      <hr>
      <h4>Age</h4>
      From to

      <hr>
      <h4>Biobank</h4>
      <select v-model="selectedBiobank" class="custom-select">
        <option v-for="biobank in biobankOptions" :value='biobank.value'>{{biobank.text}}</option>
      </select>
    </form>

    <small slot="footer" class="text-muted">
      Number of samples: {{numberOfSamples}}
    </small>

  </b-card>
</template>

<script>
  import FilterCheckbox from './FilterCheckbox'
  import {mapState} from 'vuex'

  export default {
    components: { filterCheckbox: FilterCheckbox },
    computed: {
      ...mapState({
        biobankOptions: state => state.biobanks.map(biobank => ({
          text: biobank.abbr,
          value: biobank.abbr
        }))
      }),
      numberOfSamples: {
        get () {
          return this.$store.state.numberOfSamples
        }
      },
      selectedBiobank: {
        get () {
          return this.$store.state.biobank
        },
        set (biobank) {
          this.$store.dispatch('setBiobank', biobank)
        }
      }
    },
    created () {
      this.$store.dispatch('getBiobanks')
      this.$store.commit('setFilter', {name: 'DNA', value: false})
    },
    methods () {
      resetFilters: function() {
        this.$store.commit('resetFilters')
      }
    },
    name: 'filters'
  }
</script>
