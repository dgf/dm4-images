<template>

    <el-dialog :visible="dialogVisible" class="dmx-images" @close="listenClose" width="400px" title="Image Resize">
      <div class="block">
        <el-radio v-model="resizeMode" label="width">Fit to Width</el-radio>
        <el-radio v-model="resizeMode" label="height">Fit to Height</el-radio>
        <el-radio v-model="resizeMode" label="auto">Auto</el-radio>
      </div>
      <div class="block">
        <label>Size</label>
        <el-select v-model="widthOption" placeholder="Select">
          <el-option v-for="item in widthOptions"
            :key="item.value"
            :label="item.label"
            :value="item.value">
          </el-option>
        </el-select>
      </div>
      <span slot="footer" class="dialog-footer">
        <el-button @click="closeDialog">Cancel</el-button>
        <el-button type="primary" @click="confirmResize">Resize Image</el-button>
      </span>
    </el-dialog>

</template>

<script>
  export default {
    inject: {
      http: 'axios'
    },
    data () {
        return {
            resizeMode: "width",
            widthOption: 300,
            widthOptions: [
                {
                    value: 90,
                    label: "90px"
                },
                {
                    value: 160,
                    label: "160px"
                },
                {
                    value: 300,
                    label: "300px"
                },
                {
                    value: 540,
                    label: "540px"
                },
                {
                    value: 720,
                    label: "720px"
                },
                {
                    value: 900,
                    label: "900px"
                },
                {
                    value: 1400,
                    label: "1400px"
                }
            ],
            marks: {
                90: "90px",
                160: "160px",
                300: "300px", 
                420: "420px",
                540: "540px",
                600: "600px",
                720: "720px",
                900: "900px",
                1000: "1000px",
                1200: "1200px",
                1400: "1400px",
                1600: "1600px",
                2000: "2000px"
            },
            widthValues: [ 90, 160, 300, 420, 540, 600, 720, 900, 1000, 1200, 1400, 1600, 2000 ]
        }
    },
    computed: {
      dialogVisible () {
        return this.$store.state.images.resizeDialogVisible
      },
      file () {
        return this.$store.state.images.topic
      }
    },
    methods: {
      confirmResize() {
        // ### do resize
        console.log("[Images] Resize Parameter", this.widthOption, this.resizeMode, "Image File", this.file.value)
        this.http.post('/images/resize/' + this.file.id + '/' + this.widthOption + '/' + this.resizeMode)
        .then(response => {
            this.$store.dispatch("revealTopicById", response.data.id) // fixme: find name of action to reveal topic with the relating assoc
            this.$notify({
              title: 'Image Resized', type: 'success'
            })
            this.$store.dispatch("closeResizeDialog")
        }).catch(response => {
            console.warn("[Images] Resize operation failed", response)
        })
      },
      listenClose() {
        this.$store.dispatch("closeResizeDialog")
      },
      closeDialog() {
        this.$store.dispatch("closeResizeDialog")
      }
    }
  }
</script>
<style>
.dmx-images .block {
    margin-top: 1em;
}
.dmx-images .el-dialog__body {
    padding-top: 0px !important;
}
.dmx-images .el-dialog__title {
    font-size: 1.2em !important;
    font-weight: 700;
}
.dmx-images .el-dialog__body h3 {
    margin-top: 1em;
    margin-bottom: .5em;
}
</style>