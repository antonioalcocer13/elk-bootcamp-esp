package com.alvaroagea.elk.practica5;


import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.BucketOrder;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.terms.Terms;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

final class Practica5Controller extends RecordController {

    private final Logger logger = LogManager.getLogger();


    @Override
    void index(Record record) throws IOException {
        IndexRequest indexRequest = new IndexRequest(RecordController.RECORD_INDEX)
                .source(
                        RecordController.ATHLETE_FIELD, record.getAthlete(),
                        RecordController.YEAR_FIELD, record.getYear(),
                        RecordController.CITY_FIELD, record.getCity(),
                        RecordController.SPORT_FIELD, record.getSport(),
                        RecordController.DISCIPLINE_FIELD, record.getDiscipline(),
                        RecordController.COUNTRY_FIELD, record.getCountry(),
                        RecordController.GENDER_FIELD, record.getGender(),
                        RecordController.EVENT_FIELD, record.getEvent(),
                        RecordController.MEDAL_FIELD, record.getMedal()
                );


        IndexResponse indexResponse = client.index(indexRequest, RequestOptions.DEFAULT);
        logger.debug(indexResponse.toString());

    }

    @Override
    List<OlympicWinner> getOlympicWinnerByYear() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery(RecordController.YEAR_FIELD).from("1950"))
                .sort(RecordController.YEAR_FIELD, SortOrder.DESC);
        searchSourceBuilder.aggregation(AggregationBuilders.terms("year").field(RecordController.YEAR_FIELD)
                .subAggregation(
                        AggregationBuilders
                                .terms("country")
                                .field(COUNTRY_FIELD)
                                .order(BucketOrder.aggregation("_count", false))

                )
        ).aggregation(PipelineAggregatorBuilders.avgBucket("avg_winner", "year>country._bucket_count"));
        searchRequest.source(searchSourceBuilder);
        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);


        Terms years = searchResponse.getAggregations().get("year");

        List<OlympicWinner> olympicWinners = new LinkedList<>();
        for (Terms.Bucket x : years.getBuckets()) {
            String year = x.getKeyAsString();
            Terms countries = x.getAggregations().get("country");
            String country = countries.getBuckets().get(0).getKeyAsString();
            long medals = countries.getBuckets().get(0).getDocCount();

            olympicWinners.add(new OlympicWinner(year, country, (int) medals));
        }
        return olympicWinners;
    }

    @Override
    List<OlympicAthletes> getTop10Athletes() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery(RecordController.YEAR_FIELD).from("1950"))
                .sort(RecordController.YEAR_FIELD, SortOrder.DESC);


        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("athletes")
                        .field(RecordController.ATHLETE_FIELD)
                        .order(BucketOrder.aggregation("_count", false))
                        .size(10).subAggregation(
                        AggregationBuilders.terms("country")
                                .field(RecordController.COUNTRY_FIELD)
                )
        );

        searchRequest.source(searchSourceBuilder);
        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Terms athletes = searchResponse.getAggregations().get("athletes");

        List<OlympicAthletes> olympicAthletes = new LinkedList<>();
        for (Terms.Bucket x : athletes.getBuckets()) {
            String name = x.getKeyAsString();
            long medals = x.getDocCount();
            Terms countryTerm = x.getAggregations().get("country");
            String country = countryTerm.getBuckets().get(0).getKeyAsString();
            olympicAthletes.add(new OlympicAthletes(country, name, (int) medals));

        }
        return olympicAthletes;
    }

    @Override
    List<OlympicCountry> getTop10Countries() throws IOException {
        SearchRequest searchRequest = new SearchRequest();
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(QueryBuilders.rangeQuery(RecordController.YEAR_FIELD).from("1950"))
                .sort(RecordController.YEAR_FIELD, SortOrder.DESC);


        searchSourceBuilder.aggregation(
                AggregationBuilders.terms("countries")
                        .field(RecordController.COUNTRY_FIELD)
                        .order(BucketOrder.aggregation("_count", false))
                        .size(10)
        );

        searchRequest.source(searchSourceBuilder);
        final SearchResponse searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);

        Terms athletes = searchResponse.getAggregations().get("athletes");

        List<OlympicCountry> olympicCountry = new LinkedList<>();
        for (Terms.Bucket x : athletes.getBuckets()) {
            String country = x.getKeyAsString();
            long medals = x.getDocCount();

            olympicCountry.add(new OlympicCountry("", country, (int) medals));

        }
        return olympicCountry;
    }

    @Override
    List<OlympicAthletes> getAthleteWithMoreMedalsByCountry() throws IOException {
        return new ArrayList<>();
    }
}
