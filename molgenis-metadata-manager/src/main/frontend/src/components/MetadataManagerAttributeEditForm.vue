<template>
  <div class="row">
    <div class="col-md-3 attribute-tree">
      <strong>Attributes</strong>
      <button @click="addAttribute" class="btn btn-primary btn-sm float-right">Add attribute</button>

      <hr>

      <attribute-tree :selectedAttribute="selectedAttribute" :attributes="attributeTree"
                      :onAttributeSelect="onAttributeSelect"></attribute-tree>

      <!--<p v-if="editorEntityType.parent !== undefined">-->
      <!--Parent attributes from <strong>{{editorEntityType.parent.label}}:</strong><br>-->
      <!--<span v-for="attribute in editorEntityType.parent.attributes">{{attribute.label}}</span>-->
      <!--</p>-->
    </div>
  </div>
</template>

<style>
  /*screen-md border on inner column when columns aligned next to each other*/
  @media (min-width: 768px) {
    .col-md-3.attribute-tree {
      border-right: solid black thin;
    }
  }
</style>

<script>
  import AttributeTree from './generic-components/AttributeTree'
  import { mapState, mapGetters } from 'vuex'
  import { SET_SELECTED_ATTRIBUTE_ID } from '../store/mutations'

  export default {
    name: 'metadata-manager-attribute-edit-form',
    methods: {
      onAttributeSelect (value) {
        this.$store.commit(SET_SELECTED_ATTRIBUTE_ID, value.id)
        this.$router.push({path: '/' + this.$route.params.entityTypeID + '/' + value.id})
      },
      addAttribute: () => alert('Not yet implemented :)')
    },
    computed: {
      ...mapState(['editorEntityType']),
      ...mapGetters({
        selectedAttribute: 'getSelectedAttribute',
        attributeTree: 'getAttributeTree'
      })
    },
    watch: {
      editorEntityType: function () {
        this.selectedAttribute = null
      }
    },
    components: {
      AttributeTree
    }
  }
</script>
