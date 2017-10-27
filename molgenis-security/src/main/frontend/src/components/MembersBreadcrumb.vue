<template>
  <div class="row mb-">
    <div class="col">
      <ol class="breadcrumb">
        <li class="breadcrumb-item"><a href="#" @click.stop="doSomething">BBMRI-NL</a></li>
        <li class="breadcrumb-item active">Members</li>
      </ol>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'members-breadcrumb',
    methods: {
      doSomething: function () {
        alert('TODO do some unknown thing here')
      }
    }
  }
</script>
