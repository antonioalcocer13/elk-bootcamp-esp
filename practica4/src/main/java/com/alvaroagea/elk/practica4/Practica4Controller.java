package com.alvaroagea.elk.practica4;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.TermQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.time.Instant;
import java.util.*;

final class Practica4Controller extends EventController {

    private final Logger logger = LogManager.getLogger();

    @Override
    public Optional<List<Event>> last(String id, int limit) {

        List<Event> result = new LinkedList<>();
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        final TermQueryBuilder termQueryBuilder = QueryBuilders.termQuery(EventController.ID_FIELD, id);


        sourceBuilder.query(termQueryBuilder)
                .sort(EventController.T1_FIELD, SortOrder.DESC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        Collections.reverse(result);
        return Optional.of(result);
    }

    @Override
    public Optional<List<Event>> last(String id, List<String> tags, int limit, Instant before) {
        List<Event> result = new LinkedList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(EventController.ID_FIELD, id));

        for (String t : tags) {
            boolQueryBuilder = boolQueryBuilder.should(
                    QueryBuilders.termQuery(EventController.TAG_FIELD, t));
        }

        if (before != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T2_FIELD).to(before));
        }

        boolQueryBuilder = boolQueryBuilder.minimumShouldMatch(1);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder)
                .sort(EventController.T2_FIELD, SortOrder.DESC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        Collections.reverse(result);
        return Optional.of(result);
    }

    @Override
    public Optional<List<Event>> lastDistinct(String id, List<String> tags, int limit, Instant before) {
        List<Event> result = new LinkedList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(EventController.ID_FIELD, id));

        for (String t : tags) {
            boolQueryBuilder = boolQueryBuilder.mustNot(
                    QueryBuilders.termQuery(EventController.TAG_FIELD, t));
        }

        if (before != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T2_FIELD).to(before));
        }


        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder)
                .sort(EventController.T2_FIELD, SortOrder.DESC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        Collections.reverse(result);
        return Optional.of(result);
    }

    @Override
    public Optional<List<Event>> search(String id, List<String> tags, Instant before, Instant after, int limit) {
        List<Event> result = new LinkedList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(EventController.ID_FIELD, id));

        for (String t : tags) {
            boolQueryBuilder = boolQueryBuilder.should(
                    QueryBuilders.termQuery(EventController.TAG_FIELD, t));
        }

        if (before != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T2_FIELD).to(before));
        }

        if (after != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T1_FIELD).from(after));
        }

        boolQueryBuilder = boolQueryBuilder.minimumShouldMatch(1);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder)
                .sort(EventController.T2_FIELD, SortOrder.ASC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    @Override
    public Optional<List<Event>> searchDistinct(String id, List<String> tags, Instant before, Instant after, int limit) {
        List<Event> result = new LinkedList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(EventController.ID_FIELD, id));

        for (String t : tags) {
            boolQueryBuilder = boolQueryBuilder.mustNot(
                    QueryBuilders.termQuery(EventController.TAG_FIELD, t));
        }

        if (before != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T2_FIELD).to(before));
        }

        if (after != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T1_FIELD).from(after));
        }


        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder)
                .sort(EventController.T2_FIELD, SortOrder.ASC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    @Override
    public Optional<List<Event>> first(String id, List<String> tags, int limit, Instant after) {
        List<Event> result = new LinkedList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(EventController.ID_FIELD, id));

        for (String t : tags) {
            boolQueryBuilder = boolQueryBuilder.should(
                    QueryBuilders.termQuery(EventController.TAG_FIELD, t));
        }

        if (after != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T1_FIELD).from(after));
        }

        boolQueryBuilder = boolQueryBuilder.minimumShouldMatch(1);

        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder)
                .sort(EventController.T2_FIELD, SortOrder.ASC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }

    @Override
    public Optional<List<Event>> firstDistinct(String id, List<String> tags, int limit, Instant after) {
        List<Event> result = new LinkedList<>();

        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery(EventController.ID_FIELD, id));

        for (String t : tags) {
            boolQueryBuilder = boolQueryBuilder.mustNot(
                    QueryBuilders.termQuery(EventController.TAG_FIELD, t));
        }

        if (after != null) {
            boolQueryBuilder = boolQueryBuilder.filter(
                    QueryBuilders.rangeQuery(EventController.T1_FIELD).from(after));
        }


        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(boolQueryBuilder)
                .sort(EventController.T2_FIELD, SortOrder.ASC).size(limit);

        SearchRequest searchRequest = new SearchRequest(EventController.EVENT_INDEX)
                .source(sourceBuilder);

        try {
            SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);

            response.getHits().iterator().forEachRemaining(it -> {
                Map<String, Object> source = it.getSourceAsMap();
                result.add(
                        new Event(
                                source.get(EventController.ID_FIELD).toString(),
                                source.get(EventController.TAG_FIELD).toString(),
                                Instant.parse(source.get(EventController.T1_FIELD).toString()),
                                Instant.parse(source.get(EventController.T2_FIELD).toString())
                        )
                );
            });

        } catch (IOException e) {
            return Optional.empty();
        }
        return Optional.of(result);
    }
}
