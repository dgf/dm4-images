// configure CKEditor image upload and browsing
dm4c.add_plugin('de.deepamehta.images', function () {
    // config lines are the same / work with "image2" plugin as well
    CKEDITOR.config.filebrowserImageBrowseUrl = '/de.deepamehta.images/browse.html'
    CKEDITOR.config.filebrowserImageUploadUrl = '/images/upload'
})
