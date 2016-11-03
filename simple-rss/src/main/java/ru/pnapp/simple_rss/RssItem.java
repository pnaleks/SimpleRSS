package ru.pnapp.simple_rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;

@SuppressWarnings("WeakerAccess")
@Root(name = "item", strict = false)
public class RssItem {
    @Element(name = "title", required = true)
    public String title;//The title of the item.	Venice Film Festival Tries to Quit Sinking
    @Element(name = "link", required = true)
    public String link;//The URL of the item.	http://www.nytimes.com/2002/09/07/movies/07FEST.html
    @Element(name = "description", required = false)
    public String description;//The item synopsis.	Some of the most heated chatter at the Venice Film Festival this week was about the way that the arrival of the stars at the Palazzo del Cinema was being staged.
    @Element(name = "author", required = false)
    public String author;//Email address of the author of the item. More.	oprah@oxygen.net
    @Element(name = "category", required = false)
    public String category;//Includes the item in one or more categories. More.	Simpsons Characters
    @Element(name = "comments", required = false)
    public String comments;//URL of a page for comments relating to the item. More.	http://www.myblog.org/cgi-local/mt/mt-comments.cgi?entry_id=290
    @Element(name = "enclosure", required = false)
    public Enclosure enclosure;//	Describes a media object that is attached to the item. More.	<enclosure url="http://live.curry.com/mp3/celebritySCms.mp3" length="1069871" type="audio/mpeg"/>
    @Element(name = "guid", required = false)
    public String guid;//A string that uniquely identifies the item. More.	<guid isPermaLink="true">http://inessential.com/2002/09/01.php#a2</guid>
    @Element(name = "pubDate", required = false)
    public String pubDate;//	Indicates when the item was published. More.	Sun, 19 May 2002 15:21:36 GMT
    @Element(name = "source", required = false)
    public String source;//	The RSS channel that the item came from. More.

    @Override
    public String toString() {
        return "Item{" +
                "title='" + title + '\'' +
                ", link='" + link + '\'' +
                ", description='" + description + '\'' +
                ", author='" + author + '\'' +
                ", category='" + category + '\'' +
                ", comments='" + comments + '\'' +
                ", enclosure='" + enclosure + '\'' +
                ", guid='" + guid + '\'' +
                ", pubDate='" + pubDate + '\'' +
                ", source='" + source + '\'' +
                '}';
    }

    @Root(name = "enclosure", strict = false)
    public static class Enclosure {
        @Attribute(name = "url", required = true)
        public String url; // Says where the enclosure is located. The url must be an http url.
        @Attribute(name = "length", required = false)
        public int length; // Says how big it is in bytes.
        @Attribute(name = "type", required = true)
        public String type; // Says what its type is, a standard MIME type.

        @Override
        public String toString() {
            return "Enclosure{url='" + url + "', length='" + length + "', type='" + type + "'}";
        }
    }
}
