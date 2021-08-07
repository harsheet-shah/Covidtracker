package com.springdemo.covidtracker.services;


import models.LocationStats;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

@Service // It will make spring service
public class CovidDataService {
    private  static  final String COVID_DATA="https://raw.githubusercontent.com/CSSEGISandData/COVID-19/master/csse_covid_19_data/csse_covid_19_time_series/time_series_covid19_confirmed_global.csv";
    private List<LocationStats> allStats = new ArrayList<>();

    public List<LocationStats> getAllStats() {
        return allStats;
    }

    @PostConstruct //Once executing & constructing(Instantiating) above class or service, execute this method
    @Scheduled(cron = "* * 0 * * *") // to fetch the data for given schedule
    /*
    (sec, min, hour, day, month, year)
    Above string will run every 0th hour of data
     */
    public void fetchCovidData() throws IOException, InterruptedException {
        List<LocationStats> newStat = new ArrayList<>();
        /*
        To avoid concurrency issue we are having newStat. Consider someone request,when the stats are updating.
        Though this won't completely eliminate the concurrency issue
         */
        HttpClient httpClient = HttpClient.newHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                                .uri(URI.create(COVID_DATA))
                                .build();
        HttpResponse<String> httpResponse = httpClient.send(httpRequest,HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);
        for (CSVRecord record : records) {
            LocationStats locationStats = new LocationStats();

            locationStats.setState(record.get("Province/State").length()==0?"--":record.get("Province/State"));
            locationStats.setCountry(record.get("Country/Region").length()==0?"--":record.get("Country/Region"));
            int prevDayCases = Integer.parseInt(record.get(record.size()-2));
            int currentCases = Integer.parseInt(record.get(record.size()-1));

            String changeComparedToPrevDay="";
            if(currentCases - prevDayCases >0)
                changeComparedToPrevDay = String.valueOf(currentCases-prevDayCases)+" Increased";
            else if(currentCases - prevDayCases <0)
                changeComparedToPrevDay =String.valueOf(prevDayCases-currentCases) + " Decreased";
            else
                changeComparedToPrevDay = "No change";

            locationStats.setLatestTotalCase(Integer.parseInt(record.get(record.size()-1)));
            locationStats.setStatComparedToPrevDay(changeComparedToPrevDay);
            newStat.add(locationStats);
        }
        this.allStats=newStat;
    }
}
