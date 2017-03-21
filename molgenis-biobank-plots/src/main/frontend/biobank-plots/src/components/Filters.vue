<template>
  <b-card show-header>

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
  </b-card>
</template>

<script>
  import FilterCheckbox from './FilterCheckbox'
  import {mapState} from 'vuex'

  import { GET_BIOBANKS, RESET_FILTERS_ASYNC, SET_BIOBANK } from '../store/actions'

  export default {
    name: 'filters',
    components: { FilterCheckbox },
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
          this.$store.dispatch(SET_BIOBANK, biobank)
        }
      }
    },
    created () {
      this.$store.dispatch(GET_BIOBANKS)
      this.$store.dispatch(RESET_FILTERS_ASYNC)
    },
    methods: {
      resetFilters: function () {
        this.$store.dispatch(RESET_FILTERS_ASYNC)
      }
    }
  }
</script>
