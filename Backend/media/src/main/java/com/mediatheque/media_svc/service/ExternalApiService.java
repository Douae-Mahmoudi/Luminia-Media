package com.mediatheque.media_svc.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mediatheque.media_svc.dto.ExternalMediaResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class ExternalApiService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Value("${external.tvmaze.base-url:https://api.tvmaze.com}")
    private String tvmazeBaseUrl;

    @Value("${external.youtube.base-url:https://www.googleapis.com/youtube/v3}")
    private String youtubeBaseUrl;

    @Value("${external.youtube.api-key}")
    private String youtubeApiKey;

    @Value("${external.archive.base-url:https://archive.org}")
    private String archiveBaseUrl;


    @Value("${external.podcastindex.base-url:https://api.podcastindex.org/api/1.0}")
    private String podcastIndexBaseUrl;

    @Value("${external.podcastindex.api-key}")
    private String podcastIndexApiKey;

    @Value("${external.podcastindex.api-secret}")
    private String podcastIndexApiSecret;

    @Value("${external.itunes.base-url:https://itunes.apple.com}")
    private String itunesBaseUrl;


    @Value("${external.freetogame.base-url:https://www.freetogame.com/api}")
    private String freeToGameBaseUrl;

    @Value("${external.rawg.base-url:https://api.rawg.io/api}")
    private String rawgBaseUrl;

    @Value("${external.rawg.api-key}")
    private String rawgApiKey;


    @Value("${external.internetarchive.base-url:https://archive.org}")
    private String internetArchiveBaseUrl;


    public List<ExternalMediaResponse> searchFilms(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String url = String.format("%s/search/shows?q=%s", tvmazeBaseUrl, query);
            List<Map> response = restTemplate.getForObject(url, List.class);
            if (response == null) return results;

            for (Map item : response) {
                Map show = (Map) item.get("show");
                if (show == null) continue;

                Map image = (Map) show.get("image");
                String premiered = (String) show.get("premiered");

                results.add(ExternalMediaResponse.builder()
                        .title((String) show.get("name"))
                        .description(stripHtml((String) show.get("summary")))
                        .releaseYear(extractYear(premiered))
                        .genre(show.get("type") != null ? (String) show.get("type") : null)
                        .coverUrl(image != null ? (String) image.get("medium") : null)
                        .readUrl("https://www.tvmaze.com/shows/" + show.get("id"))
                        .source("TVMaze")
                        .externalId(String.valueOf(show.get("id")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur TVMaze API: {}", e.getMessage());
        }
        return results;
    }


    public List<ExternalMediaResponse> searchYoutubeMovies(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String url = String.format(
                    "%s/search?part=snippet&q=%s+full+movie&type=video" +
                            "&videoType=movie&maxResults=10&key=%s",
                    youtubeBaseUrl, query, youtubeApiKey);

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("items")) return results;

            List<Map> items = (List<Map>) response.get("items");
            for (Map item : items) {
                Map idMap   = (Map) item.get("id");
                Map snippet = (Map) item.get("snippet");
                if (idMap == null || snippet == null) continue;

                String videoId = (String) idMap.get("videoId");
                if (videoId == null) continue;

                Map thumbnails = (Map) snippet.get("thumbnails");
                Map high       = thumbnails != null ? (Map) thumbnails.get("high") : null;

                results.add(ExternalMediaResponse.builder()
                        .title((String) snippet.get("title"))
                        .author((String) snippet.get("channelTitle"))
                        .releaseYear(extractYear((String) snippet.get("publishedAt")))
                        .description((String) snippet.get("description"))
                        .coverUrl(high != null ? (String) high.get("url") : null)
                        .readUrl("https://www.youtube.com/watch?v=" + videoId)
                        .source("YouTube")
                        .externalId(videoId)
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur YouTube API: {}", e.getMessage());
        }
        return results;
    }


    public List<ExternalMediaResponse> searchArchiveMovies(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String url = String.format(
                    "%s/advancedsearch.php?q=%s+mediatype:movies" +
                            "&fl[]=identifier,title,description,year&rows=20&output=json",
                    archiveBaseUrl, query.replace(" ", "+"));

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null) return results;

            Map responseBody = (Map) response.get("response");
            if (responseBody == null) return results;

            List<Map> docs = (List<Map>) responseBody.get("docs");
            if (docs == null) return results;

            for (Map doc : docs) {
                String identifier = (String) doc.get("identifier");
                String title      = (String) doc.get("title");
                if (title == null) continue;

                Integer year = null;
                Object yearRaw = doc.get("year");
                if (yearRaw instanceof Integer) year = (Integer) yearRaw;
                else if (yearRaw instanceof String) year = extractYear((String) yearRaw);

                results.add(ExternalMediaResponse.builder()
                        .title(title)
                        .description(extractStringOrFirst(doc.get("description")))
                        .releaseYear(year)
                        .coverUrl(identifier != null
                                ? "https://archive.org/services/img/" + identifier : null)
                        .readUrl(identifier != null
                                ? "https://archive.org/details/" + identifier : null)
                        .source("Archive.org")
                        .externalId(identifier)
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur Archive.org movies API: {}", e.getMessage());
        }
        return results;
    }

    public List<ExternalMediaResponse> searchPodcasts(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            HttpHeaders headers = buildPodcastIndexHeaders();
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = String.format("%s/search/byterm?q=%s", podcastIndexBaseUrl, query);

            ResponseEntity<Map> response =
                    restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);

            if (response.getBody() == null) return results;

            List<Map> feeds = (List<Map>) response.getBody().get("feeds");
            if (feeds == null) return results;

            for (Map feed : feeds) {
                results.add(ExternalMediaResponse.builder()
                        .title((String) feed.get("title"))
                        .author((String) feed.get("author"))
                        .genre(feed.get("categories") != null
                                ? feed.get("categories").toString() : null)
                        .coverUrl((String) feed.get("artwork"))
                        .readUrl((String) feed.get("link"))
                        .source("PodcastIndex")
                        .externalId(String.valueOf(feed.get("id")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur PodcastIndex API: {} — fallback iTunes", e.getMessage());
            return searchPodcastsItunes(query);
        }
        return results;
    }


    public List<ExternalMediaResponse> searchPodcastsItunes(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String url = String.format(
                    "%s/search?term=%s&media=podcast&limit=10",
                    itunesBaseUrl, query);

            String raw = restTemplate.getForObject(url, String.class);
            if (raw == null) return results;

            Map<String, Object> response = objectMapper.readValue(raw, Map.class);
            List<Map> items = (List<Map>) response.get("results");
            if (items == null) return results;

            for (Map item : items) {
                results.add(ExternalMediaResponse.builder()
                        .title((String) item.get("collectionName"))
                        .author((String) item.get("artistName"))
                        .genre((String) item.get("primaryGenreName"))
                        .coverUrl((String) item.get("artworkUrl600"))
                        .readUrl((String) item.get("trackViewUrl"))
                        .source("iTunes")
                        .externalId(String.valueOf(item.get("collectionId")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur iTunes API: {}", e.getMessage());
        }
        return results;
    }

    public List<ExternalMediaResponse> searchGames(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String url = String.format(
                    "%s/games?key=%s&search=%s&page_size=10",
                    rawgBaseUrl, rawgApiKey, query);

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null || !response.containsKey("results")) return results;

            List<Map> items = (List<Map>) response.get("results");
            for (Map item : items) {
                String genre = null;
                List<Map> genres = (List<Map>) item.get("genres");
                if (genres != null && !genres.isEmpty()) {
                    genre = (String) genres.get(0).get("name");
                }

                String slug    = (String) item.get("slug");
                String gameUrl = slug != null ? "https://rawg.io/games/" + slug : null;

                results.add(ExternalMediaResponse.builder()
                        .title((String) item.get("name"))
                        .genre(genre)
                        .releaseYear(extractYear((String) item.get("released")))
                        .coverUrl((String) item.get("background_image"))
                        .readUrl(gameUrl)
                        .source("RAWG")
                        .externalId(String.valueOf(item.get("id")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur RAWG API: {}", e.getMessage());
        }
        return results;
    }


    public List<ExternalMediaResponse> searchFreeGames(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String url = String.format("%s/games", freeToGameBaseUrl);
            List<Map> items = restTemplate.getForObject(url, List.class);
            if (items == null) return results;

            for (Map item : items) {
                String title = (String) item.get("title");
                if (query != null && !query.isBlank()
                        && !title.toLowerCase().contains(query.toLowerCase())) continue;

                results.add(ExternalMediaResponse.builder()
                        .title(title)
                        .genre((String) item.get("genre"))
                        .description((String) item.get("short_description"))
                        .coverUrl((String) item.get("thumbnail"))
                        .readUrl((String) item.get("game_url"))
                        .source("FreeToGame")
                        .externalId(String.valueOf(item.get("id")))
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur FreeToGame API: {}", e.getMessage());
        }
        return results;
    }



    public List<ExternalMediaResponse> searchBooks(String query) {
        List<ExternalMediaResponse> results = new ArrayList<>();
        try {
            String encodedQuery = query.replace(" ", "+");
            String url = String.format(
                    "%s/advancedsearch.php?q=%s+AND+mediatype%%3Atexts&output=json&rows=20" +
                            "&fl[]=identifier,title,creator,subject,description,year",
                    internetArchiveBaseUrl, encodedQuery);

            Map response = restTemplate.getForObject(url, Map.class);
            if (response == null) return results;

            Map responseBlock = (Map) response.get("response");
            if (responseBlock == null) return results;

            List<Map> docs = (List<Map>) responseBlock.get("docs");
            if (docs == null || docs.isEmpty()) return results;

            for (Map doc : docs) {
                String identifier = (String) doc.get("identifier");
                String title      = (String) doc.get("title");
                if (title == null) continue;

                String author = extractStringOrFirst(doc.get("creator"));
                String genre  = extractStringOrFirst(doc.get("subject"));
                String desc   = extractStringOrFirst(doc.get("description"));

                Integer year = null;
                Object yearRaw = doc.get("year");
                if (yearRaw instanceof Integer) year = (Integer) yearRaw;
                else if (yearRaw instanceof String) year = extractYear((String) yearRaw);

                results.add(ExternalMediaResponse.builder()
                        .title(title)
                        .author(author)
                        .genre(genre)
                        .releaseYear(year)
                        .description(desc)
                        .coverUrl(identifier != null
                                ? "https://archive.org/services/img/" + identifier : null)
                        .readUrl(identifier != null
                                ? "https://archive.org/details/" + identifier : null)
                        .source("InternetArchive")
                        .externalId(identifier)
                        .build());
            }
        } catch (Exception e) {
            log.error("Erreur Internet Archive books API: {}", e.getMessage());
        }
        return results;
    }



    private HttpHeaders buildPodcastIndexHeaders() throws Exception {
        long epoch = Instant.now().getEpochSecond();
        String toHash = podcastIndexApiKey + podcastIndexApiSecret + epoch;

        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] hash = md.digest(toHash.getBytes(StandardCharsets.UTF_8));
        String authHash = HexFormat.of().formatHex(hash);

        HttpHeaders headers = new HttpHeaders();
        headers.set("X-Auth-Key",  podcastIndexApiKey);
        headers.set("X-Auth-Date", String.valueOf(epoch));
        headers.set("Authorization", authHash);
        headers.set("User-Agent",  "mediatheque-app/1.0");
        return headers;
    }


    @SuppressWarnings("unchecked")
    private String extractStringOrFirst(Object raw) {
        if (raw instanceof List) {
            List<String> list = (List<String>) raw;
            return list.isEmpty() ? null : list.get(0);
        } else if (raw instanceof String) {
            return (String) raw;
        }
        return null;
    }

    private String stripHtml(String html) {
        if (html == null) return null;
        return html.replaceAll("<[^>]*>", "").trim();
    }

    private Integer extractYear(String dateStr) {
        if (dateStr != null && dateStr.length() >= 4) {
            try {
                return Integer.parseInt(dateStr.substring(0, 4));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
