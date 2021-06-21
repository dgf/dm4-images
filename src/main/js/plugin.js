export default ({dmx, store, axios: http, Vue}) => ({

  init () {
    console.log("Images plugin init...")
    store.dispatch("upload/registerUploadHandler", {
      mimeTypes: ["image/png", "image/jpg", "image/jpeg"],
      action: "/images/upload",
      selected: function(file, fileList) {
        console.log("[Images] upload dialog change selected for upload", file)
      },
      success: function(response, file, fileList) {
        this.$store.dispatch("revealTopicById", response.id)
        this.$notify({
          title: 'Image Uploaded', type: 'success'
        })
        this.$store.dispatch("upload/closeUploadDialog")
      },
      error: function(error, file, fileList) {
        console.log("[Images] file upload error", error)
        this.$notify.error({
          title: 'Image File Upload Failed', message: 'Error: ' + JSON.stringify(error)
        })
        this.$store.dispatch("upload/closeUploadDialog")
      }
    })
  },
  
  components: [{
    comp: require('./components/Resize-Dialog').default,
    mount: 'webclient'
  }],

  storeModule: {
    name: 'images',
    module: require('./images-store').default
  },

  contextCommands: {
    topic: topic => {
      if (topic.typeUri === 'dmx.files.file') {
        // Fixme: Use hasWrite access instead of relying logged in (e.g. files in a `Common` workspace)
        let isLoggedIn = (store.state.accesscontrol.username)
        let isJpgFile = (topic.value.indexOf('.jpg') !== -1) // Fixme: Do the right thing.
        let isJpegFile = (topic.value.indexOf('.jpeg') !== -1) // Fixme: Do the right thing.
        let isPngFile = (topic.value.indexOf('.png') !== -1) // Fixme: Do the right thing.
        if (isLoggedIn && (isJpgFile || isJpegFile || isPngFile)) {
          return [{
            label: 'Resize',
            handler: id => {
              store.dispatch("openResizeDialog", topic)
            }
          }]
        }
      }
    }
  }
  
  /** 
   * 
   * var selectedMaxSize = 300
    var selectedMode = "auto"

    function openResizeDialog() {
        var resizeModeMenu = dm4c.ui.menu(function(result) {
            selectedMode = result.value
        })
        resizeModeMenu.add_item({"label": "Auto", "value": "auto"})
        resizeModeMenu.add_item({"label": "Width", "value": "width"})
        resizeModeMenu.add_item({"label": "Height", "value": "height"})

        var sizeMenu = dm4c.ui.menu(function(result) {
            selectedMaxSize = result.value
        })
        sizeMenu.add_item({"label": "90px", "value": 90})
        sizeMenu.add_item({"label": "160px", "value": 160})
        sizeMenu.add_item({"label": "300px", "value": 300})
        sizeMenu.add_item({"label": "420px", "value": 420})
        sizeMenu.add_item({"label": "540px", "value": 540})
        sizeMenu.add_item({"label": "600px", "value": 600})
        sizeMenu.add_item({"label": "720px", "value": 720})
        sizeMenu.add_item({"label": "900px", "value": 900})
        sizeMenu.add_item({"label": "1000px", "value": 1000})
        sizeMenu.add_item({"label": "1200px", "value": 1200})
        sizeMenu.add_item({"label": "1400px", "value": 1400})
        sizeMenu.add_item({"label": "1600px", "value": 1600})
        sizeMenu.add_item({"label": "2000px", "value": 2000})
        //
        var labelSize = $('<span>').attr("class", "field-label").html("Max. Size<br/>")
        var fitToMode = $('<span>').attr("class", "field-label").html("Fit to<br/>")
        var dialogBody = $('<div>').append(labelSize).append(sizeMenu.dom).append('<br/>')
                .append(fitToMode).append(resizeModeMenu.dom)
        var resizeSettingsDialog = dm4c.ui.dialog({
            "id": 'resize-options',
            "width": 175,
            "title": 'Resize Options',
            "content": dialogBody,
            "button_label": "Do Resize",
            "button_handler": function(e) {
                var imageTopic = dm4c.restc.request('GET', '/images/resize/' + dm4c.selected_object.id + "/" + selectedMaxSize + "/" + selectedMode)
                dm4c.do_reveal_related_topic(imageTopic.id, "show")
                resizeSettingsDialog.close()
            },
            "auto_close": false
        })
        if (selectedMaxSize) {
            sizeMenu.select(selectedMaxSize)
        } else {
            sizeMenu.select(300)
        }
        if (selectedMode) {
            resizeModeMenu.select(selectedMode)
        } else {
            resizeModeMenu.select("auto")
        }
    }
   */

})
