<template>
  <div>
    <h3>{{title}}</h3>
    <form v-on:submit="submit({label, description})">
      <div class="form-group">
        <label for="labelInput">{{'LABEL' | i18n}}</label>
        <input v-model="label" class="form-control" id="labelInput"
               :placeholder="'ROLE_LABEL' | i18n"
               required>
        <label for="descriptionInput">{{'DESCRIPTION' | i18n}}</label>
        <input v-model="description" class="form-control" id="descriptionInput"
               :placeholder="'ROLE_DESCRIPTION' | i18n">
      </div>
      <div class="float-right">
        <button type="button" class="btn btn-default" @click="cancel()">{{'CANCEL' | i18n}}</button>
        <button class="btn btn-primary">{{'SAVE' | i18n}}</button>
      </div>
    </form>
  </div>
</template>

<script>
  export default {
    name: 'role-form',
    data () {
      return {
        label: this.initialLabel,
        description: this.initialDescription
      }
    },
    props: {
      initialLabel: {type: String},
      initialDescription: {type: String},
      title: {type: String},
      submit: {type: Function},
      cancel: {type: Function}
    }
  }
</script>

<style scoped>
  button:hover {
    cursor: pointer
  }
</style>
