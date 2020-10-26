package com.alvaroagea.elk.practica3;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;

import java.io.IOException;
import java.time.Instant;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

final class Practica3Controller extends Controller {

    private final Logger logger = LogManager.getLogger();
    @Override
    void index(Message message) throws IOException {
        IndexRequest indexRequest = new IndexRequest(Controller.MESSAGE_INDEX)
                .source(Controller.AUTHOR_FIELD, message.getAuthor(),
                        Controller.TIME_FIELD, message.getTime(),
                        Controller.MESSAGE_FIELD, message.getMessage());

        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        logger.debug(indexResponse.toString());
    }


    @Override
    List<Message> searchMessage(String message) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.matchQuery(Controller.MESSAGE_FIELD, message));
        SearchRequest searchRequest = new SearchRequest(Controller.MESSAGE_INDEX)
                .source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<Message> messages = new LinkedList<>();
        response.getHits().iterator().forEachRemaining(it -> {
            Map<String, Object> source = it.getSourceAsMap();
            messages.add(
                    new Message(
                            Instant.parse(source.get(Controller.TIME_FIELD).toString()),
                            source.get(Controller.AUTHOR_FIELD).toString(),
                            source.get(Controller.MESSAGE_FIELD).toString()
                    )
            );
        });
        return messages;
    }

    @Override
    List<Message> searchAuthor(String author) throws IOException {
        SearchSourceBuilder sourceBuilder = new SearchSourceBuilder();
        sourceBuilder.query(QueryBuilders.wildcardQuery(Controller.AUTHOR_FIELD, author));
        SearchRequest searchRequest = new SearchRequest(Controller.MESSAGE_INDEX)
                .source(sourceBuilder);
        SearchResponse response = client.search(searchRequest, RequestOptions.DEFAULT);
        List<Message> messages = new LinkedList<>();
        response.getHits().iterator().forEachRemaining(it -> {
            Map<String, Object> source = it.getSourceAsMap();
            messages.add(
                    new Message(
                            Instant.parse(source.get(Controller.TIME_FIELD).toString()),
                            source.get(Controller.AUTHOR_FIELD).toString(),
                            source.get(Controller.MESSAGE_FIELD).toString()
                    )
            );
        });
        return messages;
    }
}
