<template>
  <div class="dmx-images-file-renderer" v-if="infoMode">
    <div v-if="isImage">
        <img :id="topicId" :src="'/filerepo/' + filePath"/>
        <!--div class="image-caption"><div class="label">File Path</div> {{filePath}}</div-->
        <dm5-value-renderer class="details" :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dm5-value-renderer>
    </div>
    <dm5-value-renderer v-else :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dm5-value-renderer>
  </div>
  <div v-else>
    <dm5-value-renderer :noHeading="true" :object="object" :path="[]" :level="0" :context="context"></dm5-value-renderer>
  </div>
</template>

<script>
export default {

  created () {
    console.log('dmx-images-file-renderer created', this.object.children["dmx.files.media_type"].value)
  },

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
        return this.object.children["dmx.files.path"].value
    },
    mediaType () {
        return this.object.children["dmx.files.media_type"].value
    },
    isImage () {
        let mediaType = this.object.children["dmx.files.media_type"].value
        return mediaType.startsWith("image")
    }
  }
}
</script>
<style>
/** .dm5-cytoscape-renderer .dm5-detail-layer .dmx-images-file-renderer.dm5-object-renderer {
    margin-top: 0px;
} **/
.dm5-cytoscape-renderer .dm5-detail-layer .dmx-images-file-renderer img {
    max-width: 350px;
}
.dmx-images-file-renderer .image-caption {
    background-color: white;
    padding: 12px;
}
.dm5-cytoscape-renderer .dm5-detail-layer .dmx-images-file-renderer .details {
    display: none;
}
</style>