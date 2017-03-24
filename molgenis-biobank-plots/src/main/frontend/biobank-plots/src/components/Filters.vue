<template>
  <b-card show-header class="card-outline-primary">

    <div slot="header" class="row">
      <div class="col-md-6">
        <h4>Filters</h4>
      </div>
      <div class="col-md-6">
        <button class="btn btn-info btn-sm float-md-right" @click.prevent="resetFilters">Reset</button>
      </div>
    </div>
    <form class="form-horizontal">
      <div class="form-group custom-controls-stacked">
        <legend class="col-form-legend">Data type</legend>
        <filter-checkbox name="transcriptome" label="Transcriptome (RNAseq)"></filter-checkbox>
        <filter-checkbox name="methylome" label="Methylome (Illumina 450K)"></filter-checkbox>
        <filter-checkbox name="genotypes" label="Genotypes (Imputed)"></filter-checkbox>
        <filter-checkbox name="wbcc" label="Whole bloodcell count"></filter-checkbox>
        <filter-checkbox name="metabolome" label="Metabolome (Brainshake)"></filter-checkbox>
        <filter-checkbox name="wgs" label="Whole Genome Sequencing (Illumina HiSeq)"></filter-checkbox>
      </div>

      <div class="form-group">
        <legend class="col-form-legend">Gender</legend>
        <div class="row">
          <div class="col-md-6">
            <filter-checkbox name="male" label="Male"></filter-checkbox>
          </div>
          <div class="col-md-6">
            <filter-checkbox name="female" label="Female"></filter-checkbox>
          </div>
        </div>
      </div>

      <div class="form-group">
        <legend class="col-form-legend">Smoking</legend>
        <div class="row">
          <div class="col-md-6">
            <filter-checkbox name="smoking" label="Yes"></filter-checkbox>
          </div>
          <div class="col-md-6">
            <filter-checkbox name="nonSmoking" label="No"></filter-checkbox>
          </div>
        </div>
      </div>

      <div class="form-group">
        <legend class="col-form-legend">Age</legend>
        <!--From <filter-number-input name="ageFrom" min=0 max=150></filter-number-input> to <filter-number-input name="ageFrom" min=0 max="150"></filter-number-input>-->
        <filter-checkbox name="belowTwenty" label="<20"></filter-checkbox><br/>
        <filter-checkbox name="twentyThirty" label="20-30"></filter-checkbox><br/>
        <filter-checkbox name="thirtyFourty" label="30-40"></filter-checkbox><br/>
        <filter-checkbox name="fourtyFifty" label="40-50"></filter-checkbox><br/>
        <filter-checkbox name="fiftySixty" label="50-60"></filter-checkbox><br/>
        <filter-checkbox name="sixtySeventy" label="60-70"></filter-checkbox><br/>
        <filter-checkbox name="seventyEighty" label="70-80"></filter-checkbox><br/>
        <filter-checkbox name="aboveEigthy" label=">80"></filter-checkbox>
      </div>

      <hr>
      <div class="form-group">
        <label>Biobank</label>
        <b-form-select v-model="selectedBiobank" :options="biobankOptions"></b-form-select>
      </div>
    </form>
  </b-card>
</template>

<script>
  import FilterCheckbox from './FilterCheckbox'
  import FilterNumberInput from './FilterNumberInput'
  import {mapState} from 'vuex'

  import { GET_BIOBANKS, RESET_FILTERS_ASYNC, SET_BIOBANK } from '../store/actions'

  export default {
    name: 'filters',
    components: { FilterCheckbox, FilterNumberInput },
    computed: {
      ...mapState({
        biobankOptions: (state) => [{text: 'All', value: null}, ...state.biobanks.map(biobank => ({
          text: biobank.abbr,
          value: biobank.abbr
        }))]
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
