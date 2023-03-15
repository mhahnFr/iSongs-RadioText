package mhahnFr.iSongs.core;

import java.util.Map;

public class WebPlayerDTO {
    public static class AudioPlayerDTO {
        public static class SourceDTO {
            public String src, type;
        }

        public String name, mediaId, level2;
        public SourceDTO[] sources;
    }

    public static class PlaylistDTO {
        public static class SongDTO {
            public String stationId, id, artist, title, type, cover, hook;
            public int duration, playingMode;
            public long starttime;
            public Object url;
        }

        public String feedUrl, label, headline, url;
        public SongDTO[] data;
    }

    public static class UrlDTO {
        public String href;
    }

    public static class ShowDTO {
        public static class DataDTO {
            public static class PresenterDTO {
                public String displayname;
                public Map<String, String> avatar;
                public UrlDTO url;
            }

            public String stationid, title, subTitle, detail;
            public long starttime, endtime;
            public Map<String, String> cover;
            public UrlDTO url;
            public PresenterDTO[] presenter;
        }

        public String feedUrl;
        public DataDTO data;
    }

    public static class LinkDTO {
        public String label, classes;
        public UrlDTO url;
    }

    public static class TabDTO {
        public String rel, label, classes;
    }

    public AudioPlayerDTO audioplayer;
    public PlaylistDTO playlist;
    public ShowDTO show;
    public LinkDTO[] links;
    public TabDTO[] tabs;
}
