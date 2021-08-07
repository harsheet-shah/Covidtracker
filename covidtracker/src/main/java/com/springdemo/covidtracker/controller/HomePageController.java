package com.springdemo.covidtracker.controller;

import com.springdemo.covidtracker.services.CovidDataService;
import models.LocationStats;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

@Controller
public class HomePageController {

    @Autowired
    CovidDataService covidDataService;

    private String timestamp;
    @GetMapping("/")
    public String homePage(Model model){
        /*
        Model is used to pass the data between java class and ui Page
         */
        List<LocationStats> allStat = covidDataService.getAllStats();
        int totalCasesReported  = allStat.stream().mapToInt(LocationStats::getLatestTotalCase).sum();
        model.addAttribute("locationStats",allStat);
        model.addAttribute("totalCasesReported",totalCasesReported);

        DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
        Date date = new Date();

        model.addAttribute("timestamp",dateFormat.format(date));

        return "home";
    }
}
