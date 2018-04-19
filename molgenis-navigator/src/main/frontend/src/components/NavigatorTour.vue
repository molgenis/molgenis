<template>
  <div id="navigator-tour">
    <button class="btn btn-primary" @click="startTour()"><i class="fa fa-info-circle" aria-hidden="true"></i> Start tour
    </button>
    <v-tour name="navigatorTour" :steps="steps" :callbacks="callbacks"></v-tour>
  </div>
</template>

<script>
  import { SET_QUERY } from '../store/mutations'
  import VueTour from 'vue-tour'

  export default {
    name: 'navigator-tour',
    components: {VueTour},
    props: ['firstPackage', 'homeUrl', 'search', 'reset'],
    data () {
      return {
        callbacks: {
          onPreviousStep: this.myCustomPreviousStepCallback,
          onNextStep: this.myCustomNextStepCallback,
          onStop: this.backToHome,
          onStart: this.reset
        }
      }
    },
    computed: {
      steps () {
        return [
          {
            target: '#navigator-search-input',
            content: `Welcome to the navigator. Enter in this search field a table or folder you are looking for.`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: '#navigator-search-input',
            content: `For example "${this.packageLabel}".`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: '.text-left.table.b-table.table-bordered',
            content: `This table contains all matches with your search terms: "${this.packageLabel}".`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: 'th.sorting.text-nowrap',
            content: `This column displays the name of the table or folder.`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: 'td.text-nowrap',
            content: `Clicking on a folder opens the selected folder in the navigator.`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: 'td.text-nowrap',
            content: `Clicking on a table navigates to the selected table.`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: 'th.d-none.d-md-table-cell',
            content: `This column displays the description provided for the table of package.`,
            params: {
              placement: 'bottom'
            }
          },
          {
            target: '.breadcrumb',
            content: `This breadcrumb path shows where you are in the data structure.`,
            params: {
              placement: 'right'
            }
          },
          {
            target: '.breadcrumb a',
            content: `This home button navigates you back to the root in your navigator.`,
            params: {
              placement: 'right'
            }
          }
        ]
      },
      packageLabel () {
        return this.firstPackage ? this.firstPackage.label : ''
      },
      packageId () {
        return this.firstPackage ? this.firstPackage.id : ''
      }
    },
    methods: {
      myCustomPreviousStepCallback (currentStep) {
        if (currentStep === 1) {
          this.reset()
        } else if (currentStep === 2) {
          this.addSearchValue()
          this.search()
        }
      },
      myCustomNextStepCallback (currentStep) {
        if (currentStep === 0) {
          this.reset()
        } else if (currentStep === 1) {
          this.addSearchValue()
          this.search()
        }
      },
      addSearchValue () {
        document.querySelector('#navigator-search-input').value = this.packageLabel
        this.$store.commit(SET_QUERY, this.packageLabel)
      },
      backToHome () {
        window.location.replace(this.homeUrl)
      },
      startTour () {
        this.$tours['navigatorTour'].start()
      }
    }
  }
</script>
<style>
  #navigator-tour {
    position: absolute;
    z-index: 1;
  }

  .v-step {
    width: 20em;
  }
</style>
