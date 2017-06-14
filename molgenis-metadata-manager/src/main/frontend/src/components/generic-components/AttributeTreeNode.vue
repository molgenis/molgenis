<template>
  <!-- TODO: Buttons for changing order -->
  <li>
    <span v-bind:class="{ 'selected-attribute-node': attribute.selected }" @click="onAttributeSelect(attribute)">
      <i v-bind:class="['fa', isFolder ? 'fa-folder-o' : 'fa-columns']"></i> {{attribute.label}}
    </span>
    <ul v-if="isFolder">
      <attribute-tree-node v-for="child in attribute.children" :attribute="child" :onAttributeSelect="onAttributeSelect"></attribute-tree-node>
    </ul>
  </li>
</template>

<style scoped>
  li {
    list-style-type: none;
  }

  .selected-attribute-node {
    background-color: #c4e3f3;
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
