# DeepaMehta 4 Images

Enables the browsing and upload of images while editing HTML content in [CKEditor](http://ckeditor.com/).

Note: An editor can just include graphics which reside in the designated "images" folder of the respective file-repository.

## Requirements

  * [DeepaMehta 4](http://github.com/jri/deepamehta) 4.7

## Usage

This plugin activates the [Upload](http://docs.cksource.com/CKEditor_3.x/Users_Guide/Rich_Text/Images#Upload) tab
and the [Browse Server](http://docs.cksource.com/CKEditor_3.x/Users_Guide/Rich_Text/Images#Link) button of CKEditor.

Note: As of DeepaMehta 4.7 there may be many file-repositories, one for each workspace. If configured like that, the dm4-images plugin always works with the workspace currently selected in the _Workspace_ menu (in the dm4-webclient).

![upload tab](https://github.com/dgf/dm4-images/raw/master/doc/upload.png)

Note: Every (via ck-editor) uploaded file is stored in the *images* directory.

![file topics map](https://github.com/dgf/dm4-images/raw/master/doc/screenshot.png)

This directory is also the base of the image browser that pops up after a click on *Browse Server*

![browse server](https://github.com/dgf/dm4-images/raw/master/doc/browse.png)


**Authors**

Danny Graf and Malte Reißig 2012-2015

