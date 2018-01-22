<template>
  <div class="container">
    <label :for="id">{{ label }}</label>
    <!--
    /**
      For creating options that do not exist:
        - taggable = true
        - pushTags = true
        - createOption = Function
    */
    -->
    <v-select v-model="localValue"
              :options="entities"
              :filterable="true"
              :inputId="id"
              :name="id"
              :required="isRequired">

      <div slot="no-options">
        <small v-if="localValue">Option '{{ localValue }}' not found.</small>
      </div>
    </v-select>

    <small :id="id + '-description'" class="text-muted">
      {{ description }}
    </small>

  </div>
</template>

<script>
  import vSelect from 'vue-select'

  export default {
    name: 'EntitySelectComponent',
    props: {
      value: {
        type: [String, Number],
        required: false
      },
      id: {
        type: String,
        required: true
      },
      label: {
        type: String,
        required: true
      },
      description: {
        type: String,
        required: false
      },
      entities: {
        type: Array,
        required: true
      },
      state: {
        type: Object,
        required: false
      },
      isRequired: {
        type: Boolean,
        default: false
      }
    },
    data () {
      return {
        // Store a local value to prevent changing the parent state
        localValue: this.value
      }
    },
    watch: {
      localValue (value) {
        if (value) {
          // Emit value changes to the parent (form)
          this.$emit('input', value.id)
        } else {
          this.$emit('input', null)
        }
        // Emit value changes to trigger the hooks.onValueChange
        // Do not use input event for this to prevent unwanted behavior
        this.$emit('dataChange')
      }
    },
    created () {
      // If there is a value set, fetch an initial list of options
      if (this.value) {
        this.field.options(this.value).then(response => {
          this.entities = response

          // Replace localValue with the entire object so vue-select can use the label property
          this.localValue = this.entities.find(option => option.id === this.value)
        })
      }
    },
    components: {
      vSelect
    }
  }
</script>
