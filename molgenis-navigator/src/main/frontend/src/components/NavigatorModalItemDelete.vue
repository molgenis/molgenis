<template>
  <b-modal
    id="itemDeleteModal"
    :title="$t('delete-confirmation-title')"
    :ok-title="$t('delete-confirmation-ok-text')"
    :cancel-title="$t('delete-confirmation-cancel-text')"
    ok-variant="danger"
    cancel-variant="secondary"
    @ok="deleteSelectedItems">
    {{ 'delete-confirmation-text' | i18n }}
  </b-modal>
</template>

<script>
import { DELETE_SELECTED_ITEMS } from '../store/actions'

export default {
  name: 'NavigatorDeleteModal',
  methods: {
    deleteSelectedItems: function () {
      this.$store.dispatch(DELETE_SELECTED_ITEMS)
    }
  }
}
</script>
