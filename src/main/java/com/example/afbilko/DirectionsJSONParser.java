package com.example.afbilko;

import com.google.android.gms.maps.model.LatLng;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class DirectionsJSONParser {

    public List<LatLng> parse(JSONObject jObject) {
        List<LatLng> polyPoints = new ArrayList<>();
        try {
            JSONArray jRoutes = jObject.getJSONArray("routes");
            if (jRoutes.length() > 0) {
                JSONArray jLegs = jRoutes.getJSONObject(0).getJSONArray("legs");
                for (int i = 0; i < jLegs.length(); i++) {
                    JSONArray jSteps = jLegs.getJSONObject(i).getJSONArray("steps");
                    for (int j = 0; j < jSteps.length(); j++) {
                        String encodedPolyline = jSteps.getJSONObject(j)
                                .getJSONObject("polyline")
                                .getString("points");
                        List<LatLng> decodedList = decodePoly(encodedPolyline);
                        polyPoints.addAll(decodedList);
                    }
                }
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return polyPoints;
    }

    private List<LatLng> decodePoly(String encoded) {
        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)), (((double) lng / 1E5)));
            poly.add(p);
        }
        return poly;
    }
}