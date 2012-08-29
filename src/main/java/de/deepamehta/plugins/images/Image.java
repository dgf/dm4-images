package de.deepamehta.plugins.images;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

import de.deepamehta.core.JSONEnabled;

public class Image implements JSONEnabled {

    private JSONObject image = new JSONObject();

    public Image(String src) {
        try {
            image.put("src", src);
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public JSONObject toJSON() {
        return image;
    }

}
