<template>
  <div class="row">
    <div class="col">
      <h1 class="text-center">One click importer</h1>
      <vue-clip :options="options" class="uploader" :on-complete="importFile">
        <template slot="clip-uploader-action">
          <div>
            <div class="dz-message"><h2> Click or Drag and Drop files here upload </h2></div>
          </div>
        </template>

        <template slot="clip-uploader-body" scope="props">
          <div v-for="file in props.files">
            <div class="file-avatar">
              <img :src="file.dataUrl" alt=""/>
            </div>
            <div class="file-details">
              <div class="file-name">
                {{ file.name }}
              </div>
              <div class="file-progress">
                <div class="progress-indicator" :style="{width: file.progress + '%'}"></div>
              </div>
              <div class="file-meta">
                <span class="file-size">{{ file.size }}</span>
                <span class="file-status">{{ file.status }}</span>
              </div>
            </div>
          </div>
        </template>

      </vue-clip>
      <!--<form v-on:submit.prevent>-->
      <!--<div class="form-group">-->
      <!--<label for="file-input">File</label>-->
      <!--<input id="file-input" type="file" @change="setFile"/>-->
      <!--</div>-->
      <!--<button class="btn btn-primary" type="submit" @click="importFile">Import</button>-->
      <!--</form>-->

    </div>
  </div>
</template>

<script>
  import Vue from 'vue'
  import VueClip from 'vue-clip'
  import { IMPORT } from '../store/actions'
  Vue.use(VueClip)
  export default {
    name: 'one-click-importer',
//    data () {
//      return {
//        file: null
//      }
//    },
    data () {
      return {
        options: {
          url: '/plugin/one-click-importer/upload',
          paramName: 'file',
          maxFiles: {
            limit: 1,
            message: 'You can only upload one file'
          }
        }
      }
    },
    methods: {
      setFile: function (event) {
        this.file = event.target.files[0]
      },
      importFile: function (file) {
        console.log(file.name)
        this.$store.dispatch(IMPORT, file)
      }
    }
  }
</script>
<style>
  html, body {
    height: 100%;
  }

  body {
    display: flex;
    justify-content: center;
    aligin-items: center;
    background: #F8F8F8;
    color: #317C9C;
    font-size: 16px;
  }

  .uploader {
    width: 400px;
    height: 550px;
    display: flex;
    border-radius: 6px;
    box-shadow: 1px 2px 19px rgba(195, 195, 195, 0.43);
    flex-direction: column-reverse;
    background: #fff;
    box-sizing: border-box;
    padding: 2em;
  }

  .progress-indicator {
    background-color: #BBCC7C;
    height: 20px;
  }
</style>
