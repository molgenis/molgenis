<template>
  <li>
    <span v-bind:class="{ 'selected-attribute-node': attribute.selected, 'node-content': true }" @click="onAttributeSelect(attribute)">
      <i v-bind:class="['fa', isFolder ? 'fa-folder-o' : 'fa-columns']"></i> {{attribute.label}}
    </span>
    <ul v-if="isFolder">
      <attribute-tree-node v-for="child in attribute.children" :attribute="child" :onAttributeSelect="onAttributeSelect"></attribute-tree-node>
    </ul>
  </li>
</template>

<style lang="scss">
  @import "~variables";
  @import "~mixins";

  li {
    list-style-type: none;
  }

  .node-content:hover {
    cursor: pointer;
    background-color: lighten($brand-primary, 50%);
  }

  .selected-attribute-node {
    background-color: lighten($brand-primary, 35%);
  }
</style>

<script>
  export default {
    name: 'attribute-tree-node',
    props: {
      attribute: {
        type: Object,
        required: true
      },
      onAttributeSelect: {
        type: Function,
        required: false
      }
    },
    computed: {
      isFolder: function () {
        return this.attribute.type === 'COMPOUND'
      }
    }
  }
</script>
