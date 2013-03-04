// configure CKEditor image upload and browsing
dm4c.add_plugin('de.deepamehta.images', function () {
    CKEDITOR.config.filebrowserImageBrowseUrl = '/de.deepamehta.images/browse.html'
    CKEDITOR.config.filebrowserImageUploadUrl = '/images/upload'
    // Note: the following config is new to ckeditor as of 4.0.1.1 (in case you want to upgrade) and
    // appears in the "Bild-Info"-Tab, commonly utilized to select an already uploaded image
    // config.filebrowserImageBrowseUrl = '/de.deepamehta.images/browse.html'
    // Hint: image-upload features in CKEDITOR do neither work with "inline"-editors nor with a "Basic"-distribution
})
