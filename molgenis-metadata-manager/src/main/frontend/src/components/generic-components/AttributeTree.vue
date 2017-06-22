<template>
  <div>
    <ul class="fa-ul">
      <attribute-tree-node v-for="attribute in attributes" :attribute="attribute" :onAttributeSelect="onAttributeSelect"></attribute-tree-node>
    </ul>
  </div>
</template>

<script>
  import AttributeTreeNode from './AttributeTreeNode'

  export default {
    name: 'attribute-tree',
    props: {
      attributes: {
        type: Array,
        required: true
      },
      onAttributeSelect: {
        type: Function,
        required: true
      }
    },
    components: {
      AttributeTreeNode
    }
  }
</script>
