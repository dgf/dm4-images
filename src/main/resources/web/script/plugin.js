// configure CKEditor image upload and browsing
dm4c.add_plugin('de.deepamehta.images', function () {
    CKEDITOR.config.filebrowserImageBrowseUrl = '/de.deepamehta.images/browse.html'
    CKEDITOR.config.filebrowserImageUploadUrl = '/images/upload'
})
