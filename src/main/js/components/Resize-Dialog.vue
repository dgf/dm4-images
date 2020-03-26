<template>

    <div v-if="dialogVisible">
        <el-dialog :close="listenClose" visible="true" title="Resize Image"
            width="30%" :show-close="false">
          <span>This is a message</span>
          <span slot="footer" class="dialog-footer">
            <el-button @click="closeDialog">Cancel</el-button>
            <el-button type="primary" @click="confirmResize">Resize</el-button>
          </span>
        </el-dialog>
    </div>

</template>

<script>
  export default {
    computed: {
      dialogVisible () {
        return this.$store.state.images.resizeDialogVisible
      }
    },
    methods: {
      confirmResize() {
        // ### do confirm
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