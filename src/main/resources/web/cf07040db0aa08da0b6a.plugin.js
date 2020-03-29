_systems_dmx_images(function(e){var t={};function i(o){if(t[o])return t[o].exports;var s=t[o]={i:o,l:!1,exports:{}};return e[o].call(s.exports,s,s.exports,i),s.l=!0,s.exports}return i.m=e,i.c=t,i.d=function(e,t,o){i.o(e,t)||Object.defineProperty(e,t,{enumerable:!0,get:o})},i.r=function(e){"undefined"!=typeof Symbol&&Symbol.toStringTag&&Object.defineProperty(e,Symbol.toStringTag,{value:"Module"}),Object.defineProperty(e,"__esModule",{value:!0})},i.t=function(e,t){if(1&t&&(e=i(e)),8&t)return e;if(4&t&&"object"==typeof e&&e&&e.__esModule)return e;var o=Object.create(null);if(i.r(o),Object.defineProperty(o,"default",{enumerable:!0,value:e}),2&t&&"string"!=typeof e)for(var s in e)i.d(o,s,function(t){return e[t]}.bind(null,s));return o},i.n=function(e){var t=e&&e.__esModule?function(){return e.default}:function(){return e};return i.d(t,"a",t),t},i.o=function(e,t){return Object.prototype.hasOwnProperty.call(e,t)},i.p="/systems.dmx.images/",i(i.s=0)}([function(e,t,i){"use strict";i.r(t),t.default=({dm5:e,store:t,axios:o,Vue:s})=>({init(){t.dispatch("registerUploadHandler",{mimeTypes:["image/png","image/jpg","image/jpeg"],action:"/images/upload",selected:function(e,t){console.log("[Images] upload dialog change selected for upload",t)},success:function(e,t,i){this.$store.dispatch("revealTopicById",e.id),this.$notify({title:"Image Uploaded",type:"success"}),this.$store.dispatch("closeUploadDialog")},error:function(e,t,i){console.log("[Images] file upload error",e),this.$notify.error({title:"Image File Upload Failed",message:"Error: "+JSON.stringify(e)}),this.$store.dispatch("closeUploadDialog")}})},components:[{comp:i(2).default,mount:"toolbar-left"}],storeModule:{name:"images",module:i(1).default},contextCommands:{topic:e=>{if("dmx.files.file"===e.typeUri){let i=t.state.accesscontrol.username,o=-1!==e.value.indexOf(".jpg"),s=-1!==e.value.indexOf(".jpeg"),l=-1!==e.value.indexOf(".png");if(i&&(o||s||l))return[{label:"Resize",handler:i=>{t.dispatch("openResizeDialog",e)}}]}}}})},function(e,t,i){"use strict";i.r(t);const o={resizeDialogVisible:!1,topic:void 0},s={openResizeDialog({state:e},t){e.resizeDialogVisible=!0,e.topic=t},closeResizeDialog(){o.resizeDialogVisible=!1}};t.default={state:o,actions:s}},function(e,t,i){"use strict";i.r(t);var o=function(e,t,i,o,s,l,a,r){var n,c="function"==typeof e?e.options:e;if(t&&(c.render=t,c.staticRenderFns=i,c._compiled=!0),o&&(c.functional=!0),l&&(c._scopeId="data-v-"+l),a?(n=function(e){(e=e||this.$vnode&&this.$vnode.ssrContext||this.parent&&this.parent.$vnode&&this.parent.$vnode.ssrContext)||"undefined"==typeof __VUE_SSR_CONTEXT__||(e=__VUE_SSR_CONTEXT__),s&&s.call(this,e),e&&e._registeredComponents&&e._registeredComponents.add(a)},c._ssrRegister=n):s&&(n=r?function(){s.call(this,this.$root.$options.shadowRoot)}:s),n)if(c.functional){c._injectStyles=n;var d=c.render;c.render=function(e,t){return n.call(t),d(e,t)}}else{var p=c.beforeCreate;c.beforeCreate=p?[].concat(p,n):[n]}return{exports:e,options:c}}({inject:{http:"axios"},data:()=>({resizeMode:"width",widthOption:300,widthOptions:[{value:90,label:"90px"},{value:160,label:"160px"},{value:300,label:"300px"},{value:540,label:"540px"},{value:720,label:"720px"},{value:900,label:"900px"},{value:1400,label:"1400px"}],marks:{90:"90px",160:"160px",300:"300px",420:"420px",540:"540px",600:"600px",720:"720px",900:"900px",1e3:"1000px",1200:"1200px",1400:"1400px",1600:"1600px",2e3:"2000px"},widthValues:[90,160,300,420,540,600,720,900,1e3,1200,1400,1600,2e3]}),computed:{dialogVisible(){return this.$store.state.images.resizeDialogVisible},file(){return this.$store.state.images.topic}},methods:{confirmResize(){console.log("[Images] Resize Parameter",this.widthOption,this.resizeMode,"Image File",this.file.value),this.http.post("/images/resize/"+this.file.id+"/"+this.widthOption+"/"+this.resizeMode).then(e=>{this.$store.dispatch("revealTopicById",e.data.id),this.$notify({title:"Image Resized",type:"success"}),this.$store.dispatch("closeResizeDialog")}).catch(e=>{console.warn("[Images] Resize operation failed",e)})},listenClose(){this.$store.dispatch("closeResizeDialog")},closeDialog(){this.$store.dispatch("closeResizeDialog")}}},(function(){var e=this,t=e.$createElement,i=e._self._c||t;return e.dialogVisible?i("el-dialog",{attrs:{visible:"true",close:e.listenClose,width:"30%","show-close":!1}},[i("h2",[e._v("Image Resize")]),e._v(" "),i("h3",[e._v("Resize Mode")]),e._v(" "),i("div",{staticClass:"block"},[i("el-radio",{attrs:{label:"width"},model:{value:e.resizeMode,callback:function(t){e.resizeMode=t},expression:"resizeMode"}},[e._v("Fit to Width")]),e._v(" "),i("el-radio",{attrs:{label:"height"},model:{value:e.resizeMode,callback:function(t){e.resizeMode=t},expression:"resizeMode"}},[e._v("Fit to Height")]),e._v(" "),i("el-radio",{attrs:{label:"auto"},model:{value:e.resizeMode,callback:function(t){e.resizeMode=t},expression:"resizeMode"}},[e._v("Auto")])],1),e._v(" "),i("h3",[e._v("Fit to")]),e._v(" "),i("div",{staticClass:"block"},[i("el-select",{attrs:{placeholder:"Select"},model:{value:e.widthOption,callback:function(t){e.widthOption=t},expression:"widthOption"}},e._l(e.widthOptions,(function(e){return i("el-option",{key:e.value,attrs:{label:e.label,value:e.value}})})),1)],1),e._v(" "),i("span",{staticClass:"dialog-footer",attrs:{slot:"footer"},slot:"footer"},[i("el-button",{on:{click:e.closeDialog}},[e._v("Cancel")]),e._v(" "),i("el-button",{attrs:{type:"primary"},on:{click:e.confirmResize}},[e._v("Resize Image")])],1)]):e._e()}),[],!1,null,null,null);t.default=o.exports}]));