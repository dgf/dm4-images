// configure CKEditor image upload and browsing
dm4c.add_plugin('de.deepamehta.images', function () {
    // config lines are the same / work with "image2" plugin as well
    CKEDITOR.config.filebrowserImageBrowseUrl = '/de.deepamehta.images/browse.html'
    CKEDITOR.config.filebrowserImageUploadUrl = '/images/upload'

    function doResize() {
        var imageTopic = dm4c.restc.request('GET', '/images/resize/' + dm4c.selected_object.id + "/300")
        dm4c.show_topic(new Topic(imageTopic), "show", undefined, true)
        // dm4c.ui.open_dialog()
    }

    dm4c.add_listener('topic_commands', function (topic) {
        // Note: create permission now managed by core
        var commands = []
        if (topic.type_uri === 'dm4.files.file' && dm4c.restc.get_username()) {
            if (topic.childs["dm4.files.media_type"].value === "image/jpeg" ||
                topic.childs["dm4.files.media_type"].value === "image/png")
            commands.push({is_separator: true, context: 'context-menu'})
            commands.push({
                label: 'Resize Image',
                handler: doResize,
                context: ['context-menu', 'detail-panel-show']
            })
        }
        return commands
    })

})
