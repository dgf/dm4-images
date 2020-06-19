# DMX Images

Adds a "Resize" command to all _Files_ of Media Type JPEG or PNG allowing users to easily create new, derived and resized versions of their graphic file.

This plugin also extends the [dmx-upload-dialog](https://github.com/mukil/dmx-upload-dialog) for handling the upload of image files into a dedicated `images` directory of the DMX Filerepo.

## Requirements

  * [DMX](http://github.com/jri/deepamehta), 5.0-beta-7 (Maintenance SNAPSHOT from: 04/04/2020).

Note: The availability of the file-upload and images resize dialog depends on a maintenance SNAPSHOT of the DMX 5.0-beta-7 release done at Apr 4, 2020 ([this one](https://download.dmx.systems/ci/dmx-5.0-SNAPSHOT_2020-04-04_9552.zip)).

## Release Notes

**1.0.0** -- Jun 19, 2020

* Adapted backend to DMX 5.0-beta-7
* Rewritten the webclient extension as vue-component
* Switched to AGPL 3.0 and added license to repository

**0.9.10** -- Jul 03, 2016

* Maintenance release fixing filepath URI encoding

**0.9.8** -- Jun 22, 2016

* Includes `Resize` command for file topics with Media Type `image/png` or `image/jpeg`
* Compatible with DeepaMehta 4.8.1

## Licensing

DMX Images is available freely under the GNU Affero General Public License, version 3.

All third party components incorporated into the DMX Images Software are licensed under the original license provided by the owner of the applicable component.

Since 0.9.8 (14 June 2016) this bundle integrates the [imgscalr library](https://github.com/thebuzzmedia/imgscalr) which was licensed under the [Apache 2 License](https://github.com/thebuzzmedia/imgscalr/blob/master/LICENSE).

## Authors

Malte Reißig, (C) 2013-2020<br/>
Danny Graf, (C) 2012-2013
