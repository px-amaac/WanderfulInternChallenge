package com.doubleacoding.wanderfulinternchallenge;

import android.util.JsonReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Aaron McIntyre on 4/6/2014.
 */
public class PlacesParser {

        public List<HashMap<String, String>> readStream(InputStream in) throws IOException {
            JsonReader reader = new JsonReader(new InputStreamReader(in, "UTF-8"));
            try {
                return readInput(reader);
            }finally{
                reader.close();
            }
        }

        private List<HashMap<String, String>> readInput(JsonReader reader) throws IOException {
            List<HashMap<String, String>> items = new ArrayList<HashMap<String, String>>();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                if (name.equals("results")) {
                    reader.beginArray();
                    while (reader.hasNext()) {
                        items.add(readItems(reader));
                    }
                    reader.endArray();
                } else
                    reader.skipValue();
            }
            reader.endObject();
            return items;
        }

        //builds structure of each list item.
        /*lat
        * lng
        * name
        * vicinity
        * reference should not be displayed but used to update the detail view with another call.*/
        private HashMap<String, String> readItems(JsonReader reader) throws IOException {
            final String[] names = {"geometry", GeofenceUtils.KEY_NAME, "reference", GeofenceUtils.KEY_NOTIFICATION_TEXT};
            HashMap<String, String> data = new HashMap<String, String>();
            reader.beginObject();
            while (reader.hasNext()) {
                String name = reader.nextName();
                //geometry hides the locations.
                if (name.equals(names[0])) {
                    reader.beginObject();
                    if (reader.nextName().equals("location")) {
                        reader.beginObject();
                        while (reader.hasNext()) {
                            String locName = reader.nextName();
                            if (locName.equals("lat")) {
                                data.put("lat", reader.nextString());
                            } else if (locName.equals("lng")) {
                                data.put("lng", reader.nextString());
                            } else
                                reader.skipValue();
                        }
                        reader.endObject();
                    }
                    reader.endObject();
                }
                //name
                else if (name.equals(names[1])) {
                    data.put(names[1], reader.nextString());
                }
                //reference
                else if (name.equals(names[2])) {
                    data.put(names[2], reader.nextString());
                }
                //vicinity
                else if (name.equals(names[3])) {
                    data.put(names[3], reader.nextString());
                } else
                    reader.skipValue();
            }
            reader.endObject();
            return data;
        }
}

