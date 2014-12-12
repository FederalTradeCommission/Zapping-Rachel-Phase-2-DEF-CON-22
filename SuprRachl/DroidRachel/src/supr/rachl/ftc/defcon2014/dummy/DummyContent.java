package supr.rachl.ftc.defcon2014.dummy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//Class Description:
//Defines application's functions 
//Defines connection points with CC-Server
//CC-Server not implemented for demo purposes

public class DummyContent {

    /**
     * An array of sample (dummy) items.
     */
    public static List<DummyItem> ITEMS = new ArrayList<DummyItem>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static Map<String, DummyItem> ITEM_MAP = new HashMap<String, DummyItem>();

    static {
        addItem(new DummyItem("1", "Fetch Targets", "[http://www.suprrachl-cc-server.com]/targets"));
        addItem(new DummyItem("2", "Fetch Campaign", "[http://www.suprrachl-cc-server.com]/campaign"));
        addItem(new DummyItem("3", "Run & Report", "[http://www.suprrachl-cc-server.com]/report"));   
        
    }

    private static void addItem(DummyItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class DummyItem {
        public String id;
        public String content;
        public String url;

        public DummyItem(String id, String content, String url) {
            this.id = id;
            this.content = content;
            this.url = url;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
