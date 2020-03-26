const state = {
  resizeDialogVisible: false
}

const actions = {

  openResizeDialog() {
    state.resizeDialogVisible = true
    console.log("[Images] resizeDialog set to visible")
  },
  closeResizeDialog() {
    console.log("[Images] dispatching resizeDialog")
    state.resizeDialogVisible = false
  }

}

export default {
  state,
  actions
}
