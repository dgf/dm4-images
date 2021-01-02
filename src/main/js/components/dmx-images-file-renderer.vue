<template>
  <div class="dmx-images-file-renderer" v-if="infoMode">
    <div v-if="isImage">
        <img :id="topicId" @onload="imageLoaded" :src="'/filerepo/' + filePath"/>
        <!--div class="image-caption"><div class="label">File Path</div> {{filePath}}</div-->
        <dmx-value-renderer class="details" :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dmx-value-renderer>
    </div>
    <div v-else-if="isVideo">
        <video :id="topicId" controls muted>
            <source :src="'/filerepo/' + filePath"/ :type="mediaType" />
            <p>Your browser doesn't support HTML5 video. Here is a <a :href="'/filerepo/' + filePath">link to the video</a> instead.</p>
        </video>
        <dmx-value-renderer class="details" :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dmx-value-renderer>
    </div>
    <dmx-value-renderer v-else :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dmx-value-renderer>
  </div>
  <div v-else>
    <dmx-value-renderer :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dmx-value-renderer>
  </div>
</template>

<script>
export default {

  props: ["object", "context"],

  computed: {
    mode () {
        return this.context.mode
    },
    topicId () {
        return this.object.id
    },
    infoMode () {
        return this.mode === "info"
    },
    filePath () {
        return encodeURIComponent(this.object.children["dmx.files.path"].value)
    },
    isImage () {
        return this.mediaType.startsWith("image/")
    },
    mediaType () {
        if (typeof this.object.children["dmx.files.media_type"] !== "undefined") {
            return this.object.children["dmx.files.media_type"].value
        }
        return ""
    },
    isVideo () {
        return this.mediaType.startsWith("video/")
    }
  },

  methods: {
    imageLoaded() {
      console.log("imageLoaded", this)
      this.context.updated()
    }
  }
}
</script>
<style>
/** .dmx-cytoscape-renderer .dmx-detail-layer .dmx-images-file-renderer.dmx-object-renderer {
    margin-top: 0px;
} **/
.dmx-cytoscape-renderer .dmx-detail-layer .dmx-images-file-renderer img {
    max-width: 350px;
}
.dmx-images-file-renderer .image-caption {
    background-color: white;
    padding: 12px;
}
.dmx-cytoscape-renderer .dmx-detail-layer .dmx-images-file-renderer .details {
    display: none;
}
</style>