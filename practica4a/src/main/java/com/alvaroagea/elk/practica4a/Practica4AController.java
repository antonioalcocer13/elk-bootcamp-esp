package com.alvaroagea.elk.practica4a;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.*;

final class Practica4AController extends ShakespeareController {

    private final Logger logger = LogManager.getLogger();

    @Override
    List<ShakespeareEntry> search(String text) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.disMaxQuery()
                .add(QueryBuilders.matchQuery(ShakespeareController.PLAY_NAME_FIELD, text))
                .add(QueryBuilders.matchQuery(ShakespeareController.SPEAKER_FIELD, text))
                .add(QueryBuilders.matchQuery(ShakespeareController.TEXT_ENTRY_FIELD, text))
                .tieBreaker(0.7f)).size(100)

        ;
        SearchRequest searchRequest = new SearchRequest(ShakespeareController.SHAKESPEARE_INDEX)
                .source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<ShakespeareEntry> messages = new LinkedList<>();
        response.getHits().iterator().forEachRemaining(it -> {
            Map<String, Object> source = it.getSourceAsMap();
            messages.add(
                    new ShakespeareEntry(
                            Integer.parseInt(source.get(ShakespeareController.LINE_ID_FIELD).toString()),
                            source.get(ShakespeareController.PLAY_NAME_FIELD).toString(),
                            Integer.parseInt(source.get(ShakespeareController.SPEECH_NUMBER_FIELD).toString()),
                            source.get(ShakespeareController.LINE_NUMBER_FIELD).toString(),
                            source.get(ShakespeareController.SPEAKER_FIELD).toString(),
                            source.get(ShakespeareController.TEXT_ENTRY_FIELD).toString(),
                            it.getScore())
            );
        });
        return messages;
    }

    @Override
    List<ShakespeareEntry> get(String id) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();

        int linea = Integer.parseInt(id);

        final RangeQueryBuilder rangeQuery = QueryBuilders
                .rangeQuery(ShakespeareController.LINE_ID_FIELD).gte(linea - 5).lte(linea + 5);

        sourceBuilder.query(rangeQuery)
                .sort(ShakespeareController.LINE_ID_FIELD, SortOrder.ASC).size(11);
        SearchRequest searchRequest = new SearchRequest(ShakespeareController.SHAKESPEARE_INDEX)
                .source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<ShakespeareEntry> messages = new LinkedList<>();
        response.getHits().iterator().forEachRemaining(it -> {
            Map<String, Object> source = it.getSourceAsMap();
            messages.add(
                    new ShakespeareEntry(
                            Integer.parseInt(source.get(ShakespeareController.LINE_ID_FIELD).toString()),
                            source.get(ShakespeareController.PLAY_NAME_FIELD).toString(),
                            Integer.parseInt(source.get(ShakespeareController.SPEECH_NUMBER_FIELD).toString()),
                            source.get(ShakespeareController.LINE_NUMBER_FIELD).toString(),
                            source.get(ShakespeareController.SPEAKER_FIELD).toString(),
                            source.get(ShakespeareController.TEXT_ENTRY_FIELD).toString(),
                            it.getScore())
            );
        });
        return messages;
    }

    @Override
    List<ShakespeareEntry> query(String query) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.queryStringQuery(query));

        SearchRequest searchRequest = new SearchRequest(ShakespeareController.SHAKESPEARE_INDEX)
                .source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<ShakespeareEntry> messages = new LinkedList<>();
        response.getHits().iterator().forEachRemaining(it -> {
            Map<String, Object> source = it.getSourceAsMap();
            messages.add(
                    new ShakespeareEntry(
                            Integer.parseInt(source.get(ShakespeareController.LINE_ID_FIELD).toString()),
                            source.get(ShakespeareController.PLAY_NAME_FIELD).toString(),
                            Integer.parseInt(source.get(ShakespeareController.SPEECH_NUMBER_FIELD).toString()),
                            source.get(ShakespeareController.LINE_NUMBER_FIELD).toString(),
                            source.get(ShakespeareController.SPEAKER_FIELD).toString(),
                            source.get(ShakespeareController.TEXT_ENTRY_FIELD).toString(),
                            it.getScore())
            );
        });
        return messages;    }
}