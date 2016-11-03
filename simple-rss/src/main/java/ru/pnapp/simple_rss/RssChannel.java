package ru.pnapp.simple_rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.ElementList;
import org.simpleframework.xml.Namespace;
import org.simpleframework.xml.NamespaceList;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Text;

import java.util.List;

@SuppressWarnings("WeakerAccess")
@NamespaceList({
        @Namespace(reference = "http://www.w3.org/2005/Atom", prefix = "atom")
})
@Root(strict = false)
public class RssChannel {
    // Tricky part in Simple XML because the link is named twice
    @ElementList(entry = "link", inline = true, required = false)
    public List<Link> links;

    @ElementList(name = "item", inline = true, required = true)
    public List<RssItem> itemList;

    @Element
    public String title;

    @Element(name = "language", required = false)
    public String language;

    @Element(name = "ttl", required = false)
    public int ttl;

    @Element(name = "pubDate", required = false)
    public String pubDate;

    @Override
    public String toString() {
        return "Channel{" +
                "links=" + links +
                ", itemList=" + itemList +
                ", title='" + title + '\'' +
                ", language='" + language + '\'' +
                ", ttl=" + ttl +
                ", pubDate='" + pubDate + '\'' +
                '}';
    }

    public static class Link {
        @Attribute(required = false)
        public String href;

        @Attribute(required = false)
        public String rel;

        @Attribute(name = "type", required = false)
        public String contentType;

        @Text(required = false)
        public String link;
    }

}

