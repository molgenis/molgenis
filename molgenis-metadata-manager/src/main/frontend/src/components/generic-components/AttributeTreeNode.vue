<template>
  <!-- TODO: Buttons for changing order -->
  <li>
    <span v-bind:class="{ 'selected-attribute-node': attribute.selected }" @click="onAttributeSelect(attribute)">
      <i v-bind:class="['fa', isFolder ? 'fa-folder-o' : 'fa-columns']"></i> {{attribute.label}}
      <div v-if="attribute.selected" class="btn-group float-right" role="group" aria-label="Basic example">
        <button @click="" class="btn btn-secondary btn-sm"><i class="fa fa-chevron-up"></i></button>
        <button class="btn btn-secondary btn-sm"><i class="fa fa-chevron-down"></i></button>
      </div>
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

  li:hover {
    cursor: pointer;
  }

  .selected-attribute-node {
    background-color: lighten($teal, 20%);
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
