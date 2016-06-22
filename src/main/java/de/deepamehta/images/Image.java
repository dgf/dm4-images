package de.deepamehta.images;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.JSONEnabled;

public class Image implements JSONEnabled {

    private final Long size;

    private final String src;

    private final String type;
    
    private final String name;
    
    public Image(String src, String mediaType, Long size, String fileName) {
        this.size = size;
        this.src = src;
        this.type = mediaType;
        this.name = fileName;
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

    public String getName() {
        return name;
    }

    @Override
    public JSONObject toJSON() {
        JSONObject image = new JSONObject();
        try {
            image.put("src", src);
            image.put("type", type);
            image.put("size", size);
            image.put("name", name);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
        return image;
    }

}
