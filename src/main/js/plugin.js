export default ({dm5, store, axios: http, Vue}) => ({

  init () {
    store.dispatch("registerUploadHandler", {
      mimeType: "PNG;JPEG;JPG;", // mimeType or file name ending in UPPERCASE, Fixme: multiple values, e.g. PNG;JPEG;JPG;
      action: "/images/upload",
      selected: function(file, fileList) {
        console.log("[Images] upload dialog change selected for upload", fileList)
      },
      success: function(response, file, fileList) {
        this.$store.dispatch("revealTopicById", response.id)
        this.$notify({
          title: 'Image Uploaded', type: 'success'
        })
        this.$store.dispatch("closeUploadDialog")
      },
      error: function(error, file, fileList) {
        console.log("[Images] file upload error", error)
        this.$notify.error({
          title: 'Image File Upload Failed', message: 'Error: ' + JSON.stringify(error)
        })
        this.$store.dispatch("closeUploadDialog")
      }
    })
  },
  
  components: [{
    comp: require('./components/Resize-Dialog').default,
    mount: 'toolbar-left'
  }],

  storeModule: {
    name: 'images',
    module: require('./images-store').default
  },

  contextCommands: {
    topic: topic => {
      if (topic.typeUri === 'dmx.files.file') {
        // 1) Check if file topic ends on .jpg, .png oder .jpeg
        dm5.restClient.getTopic(topic.id, true).then(response => {
          var mediaType = response.children["dmx.files.media_type"].value
          console.log("[Images] file with mediaType", mediaType)
        })
        // 2) Check if user is logged in
        // if (topic.type_uri === 'dm4.files.file' && dm4c.restc.get_username()) {
        let isJpgFile = (topic.value.indexOf('.jpg') !== -1) // Fixme: Do the right thing.
        if (isJpgFile) {
          return [{
            label: 'Resize',
            handler: id => {
              store.dispatch("openResizeDialog")
              // Todo: open resize dialog
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
