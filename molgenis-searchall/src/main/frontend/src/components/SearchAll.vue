<template>
  <div class="container">

    <div class="alert alert-danger" role="alert">
      <button type="button" class="close"><span aria-hidden="true">&times;</span></button>
      ---
    </div>

    <!-- Search element -->
    <div>
      <div class="col-lg-6 input-group">
        <input type="text" class="form-control"
               placeholder="---">
        <span class="input-group-btn">
          <button @click="submitQuery()" class="btn btn-secondary" disabled="true"
                  type="button">---</button>
        </span>
        <span class="input-group-btn">
          <button @click="clearQuery()" class="btn btn-secondary" disabled="true"
                  type="button">---</button>
        </span>
      </div>
    </div>
  </div>
</template>

<script>
  export default {
    name: 'search-all',
    methods: {}
  }
</script>
