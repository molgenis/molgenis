<template>
  <b-card show-header class="card-outline-primary">

    <div slot="header">
      <h4>Filters</h4>
    </div>

    <form class="form-horizontal">
      <div class="form-group custom-controls-stacked">
        <legend class="col-form-legend">Data type</legend>
        <filter-checkbox name="rnaseq" label="Transcriptome (RNAseq)"></filter-checkbox>
        <filter-checkbox name="DNAm" label="Methylome (Illumina 450K)"></filter-checkbox>
        <filter-checkbox name="DNA" label="DNA"></filter-checkbox>
        <filter-checkbox name="wbcc" label="White bloodcell count"></filter-checkbox>
        <filter-checkbox name="metabolomics" label="Metabolome (Brainshake)"></filter-checkbox>
      </div>

      <div class="form-group">
        <legend class="col-form-legend">Gender</legend>
        <filter-checkbox name="male" label="Male"></filter-checkbox>
        <filter-checkbox name="female" label="Female"></filter-checkbox>
      </div>

      <div class="form-group">
        <legend class="col-form-legend">Smoking</legend>
        <filter-checkbox name="smoking" label="Yes"></filter-checkbox>
        <filter-checkbox name="nonSmoking" label="No"></filter-checkbox>
      </div>

      <div class="form-group">
        <label>Biobank</label>
        <b-form-select v-model="selectedBiobank" :options="biobankOptions"></b-form-select>
      </div>

    <div>
      <button class="btn btn-info" @click.prevent="resetFilters">Reset filters</button>
    </div>
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
