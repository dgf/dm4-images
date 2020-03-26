const state = {
  resizeDialogVisible: false,
  topic: undefined
}

const actions = {

  openResizeDialog({state}, topic) {
    state.resizeDialogVisible = true
    state.topic = topic
  },
  closeResizeDialog() {
    state.resizeDialogVisible = false
  }

}

export default {
  state,
  actions
}
