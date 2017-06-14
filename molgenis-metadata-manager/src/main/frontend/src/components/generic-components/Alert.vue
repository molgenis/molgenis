<template>
  <div>
    <b-alert @dismissed="onDismiss" :variant="alert.type" dismissible show>{{alert.message}}</b-alert>
  </div>
</template>

<script>
  export default {
    name: 'alert',
    props: {
      alert: {
        type: Object,
        required: true
      },
      onDismiss: {
        type: Function,
        required: true
      }
    }
  }
</script>
