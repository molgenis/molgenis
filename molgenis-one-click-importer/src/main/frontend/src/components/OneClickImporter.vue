<template>
  <div class="row">
    <div class="col">
      <h1>One click importer</h1>
      <hr>

      <vue-clip :options="options" :on-complete="importFile" class="uploader">

        <template slot="clip-uploader-action" scope="params">
          <div class="upload-action" :class="{'is-dragging': params.dragging}">
            <div class="dz-message">
              <h2> Click or Drag and Drop files here upload </h2>
            </div>
          </div>
        </template>

        <template slot="clip-uploader-body" scope="props">
          <div class="uploader-files">
            <div class="uploader-file" v-for="file in props.files">
              <div class="file-avatar">
                <img :src="file.dataUrl" alt="">
              </div>
              {{ file.name }}
            </div>
          </div>

            <!--<div class="file-details">-->

              <!--<div class="file-name">-->

              <!--</div>-->

              <!--<div class="file-progress">-->
                <!--<div class="progress-indicator" :style="{width: file.progress + '%'}"></div>-->
              <!--</div>-->

              <!--<div class="file-meta">-->
                <!--<div class="badge file-status"><strong>{{ file.status }}</strong></div>-->
              <!--</div>-->
            <!--</div>-->
          <!--</div>-->
        </template>

      </vue-clip>

    </div>
  </div>
</template>

<script>
  import { IMPORT } from '../store/actions'

  export default {
    name: 'one-click-importer',
    data () {
      return {
        options: {
          url: '/plugin/one-click-importer/upload',
          paramName: 'file',
          maxFiles: {
            limit: 1,
            message: 'You can only upload one file'
          },
          acceptedFiles: ['application/vnd.ms-excel', 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet']
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
<style scoped>
  body {
    display: flex;
    background: #F8F8F8;
    font-size: 16px;
  }

  .uploader {
    box-sizing: border-box;
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

  .uploader-action .dz-message {
    color: blue;
    text-align: center;
    padding: 20px 40px;
    border: 3px dashed black;
    border-radius: 4px;
    font-size: 16px;
  }

  .uploader-files {
    flex: 1;
    padding: 40px;
  }

  .progress-indicator {
    background-color: #317C9C;
    height: 20px;
  }

  .upload-action.is-dragging {
    background: #317C9C;
  }
</style>
