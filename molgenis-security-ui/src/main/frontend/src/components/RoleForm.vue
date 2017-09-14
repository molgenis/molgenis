<template>
  <div>
    <h3>{{title | i18n}}</h3>
    <form v-on:submit.prevent="save({label, description})">
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
  import {mapGetters, mapMutations, mapActions} from 'vuex'
  import {SAVE_ROLE} from '../store/actions'
  import {CANCEL_EDIT_ROLE} from '../store/mutations'

  export default {
    name: 'role-form',
    data () {
      const role = this.$store.getters.role
      return {
        label: role && role.label,
        description: role && role.description
      }
    },
    computed: {
      ...mapGetters(['role']),
      title () {
        return this.role ? 'EDIT_ROLE' : 'CREATE_ROLE'
      }
    },
    methods: {
      ...mapActions({
        save: SAVE_ROLE
      }),
      ...mapMutations({
        cancel: CANCEL_EDIT_ROLE
      })
    }
  }
</script>

<style scoped>
  button:hover {
    cursor: pointer
  }
</style>
