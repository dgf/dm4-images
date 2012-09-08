package de.deepamehta.plugins.images;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.JSONEnabled;

public class Image implements JSONEnabled {

    private final Long size;

    private final String src;

    private final String type;

    public Image(String src, String mediaType, Long size) {
        this.size = size;
        this.src = src;
        this.type = mediaType;
    }

    public Long getSize() {
        return size;
    }

    public String getSrc() {
        return src;
    }

    public String getType() {
        return type;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject image = new JSONObject();
        try {
            image.put("src", src);
            image.put("type", type);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

}
