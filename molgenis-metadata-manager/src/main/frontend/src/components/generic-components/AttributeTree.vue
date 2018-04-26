<template>
  <div>
    <ul class="fa-ul">
      <draggable v-model="attributes" @start="drag=true" @end="drag=false">
        <attribute-tree-node v-for="attribute in attributes" :attribute="attribute"
                             :onAttributeSelect="onAttributeSelect"></attribute-tree-node>
      </draggable>
    </ul>
  </div>
</template>

<script>
  import { UPDATE_EDITOR_ENTITY_TYPE } from '../../store/mutations'

  import AttributeTreeNode from './AttributeTreeNode'
  import draggable from 'vuedraggable'

  export default {
    name: 'attribute-tree',
    props: {
      attributeTree: {
        type: Array,
        required: true
      },
      onAttributeSelect: {
        type: Function,
        required: true
      }
    },
    computed: {
      attributes: {
        get () {
          return this.attributeTree
        },
        set (value) {
          this.$store.commit(UPDATE_EDITOR_ENTITY_TYPE, { key: 'attributes', value: value })
        }
      }
    },
    components: {
      AttributeTreeNode,
      draggable
    }
  }
</script>
