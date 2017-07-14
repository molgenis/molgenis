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
              <div class="file-details">

                <div class="file-name">
                  {{ file.name }}
                </div>

                <div class="file-progress">
                  <!--Don't you dare to change it back to span, it won't work!-->
                  <div class="progress-indicator" :style="{width: file.progress + '%'}"></div>
                </div>

                <div class="file-meta">
                  <div class="badge badge-default file-status"><strong>{{ file.status }}</strong></div>
                </div>
              </div>
            </div>
          </div>
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
          // FIXME: more than one file now produces error, should clear for next
          maxFiles: {
            limit: 1,
            message: 'You can only upload one file'
          },
          // This has to be a comma separated string, don't ask me why
          acceptedFiles: 'application/vnd.ms-excel,application/vnd.openxmlformats-officedocument.spreadsheetml.sheet'
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
    background: #e8e8e8;
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
  }

  .upload-action .dz-message {
    color: #94a7c2;
    text-align: center;
    padding: 20px 40px;
    border: 3px dashed #dfe8fe;
    border-radius: 4px;
    font-size: 16px;
  }

  .uploader-files {
    flex: 1;
    padding: 40px;
  }

  .progress-indicator {
    display: block;
    background-color: #00d28a;
    border-radius: 8px;
    height: 4px;
  }

  .upload-action {
    background: #f1f5ff;
    padding: 20px;
    cursor: pointer;
    transition: background 300ms ease;
  }

  .upload-action.is-dragging {
    background: #00d28a;
  }
</style>
