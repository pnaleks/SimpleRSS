package ru.pnapp.simple_rss;

import org.simpleframework.xml.Attribute;
import org.simpleframework.xml.Element;
import org.simpleframework.xml.Root;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.InputStream;

@SuppressWarnings("WeakerAccess")
@Root
public class RssFeed {

    @Attribute
    public String version;

    @Element
    public RssChannel channel;

    @Override
    public String toString() {
        return "RSS{" +
                "version='" + version + '\'' +
                ", channel=" + channel +
                '}';
    }

    public static RssFeed get(InputStream inputStream) throws Exception {
        Serializer serializer = new Persister();
        return serializer.read(RssFeed.class, inputStream);
    }
}
