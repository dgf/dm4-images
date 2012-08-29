// CKEditor image browse integration, see CKEDITOR.config.filebrowserImageBrowseUrl
(function($, dm4c, ck) {

    // TODO js_utils should include this method
    function getUrlParam(param) {
        var regex = new RegExp('(?:[\?&]|&amp;)' + param + '=([^&]+)', 'i'),
            match = window.location.search.match(regex)
        return (match && match.length > 1) ? match[1] : ''
    }

    $(function() {
        var funcNum = getUrlParam('CKEditorFuncNum'),
            images = dm4c.restc.request('GET', '/images/browse'),
            $body = $(document.body)

        $.each(images, function (i, image) {
            $body.append($('<img>').attr('src', image.src))
        })

        $body.on('click', 'img', function () {
            ck.tools.callFunction(funcNum, $(this).attr('src'))
            window.close()
        })
    })
// hint: do not use the opener jQuery instance
}(jQuery, window.opener.dm4c, window.opener.CKEDITOR))
