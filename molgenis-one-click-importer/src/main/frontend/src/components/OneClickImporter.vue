<template>
  <div class="row">
    <div class="col">
      <h1 class="text-center">One click importer</h1>
      <form v-on:submit.prevent>
        <div class="form-group">
          <label for="file-input">File</label>
          <input id="file-input" type="file" @change="setFile"/>

        </div>
        <button class="btn btn-primary" type="submit" @click="importFile">Import</button>
      </form>

    </div>
  </div>
</template>

<script>
  import { IMPORT } from '../store/actions'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        file: null
      }
    },
    methods: {
      setFile: function (event) {
        this.file = event.target.files[0]
      },
      importFile: function () {
        this.$store.dispatch(IMPORT, this.file)
      }
    }
  }
</script>
