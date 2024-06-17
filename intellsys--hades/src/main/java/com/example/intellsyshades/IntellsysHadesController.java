package com.example.intellsyshades;

import com.example.intellsyshades.facebookads.controller.FacebookAdsController;
import com.example.intellsyshades.googleads.controller.GoogleAdsController;
import com.example.intellsyshades.youtube.controller.YoutubeController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
public class IntellsysHadesController {

    @Autowired
    private GoogleAdsController googleAdsController;

    @Autowired
    private YoutubeController youtubeController;

    @Autowired
    private FacebookAdsController facebookAdsController;

    @GetMapping("/")
    public String home() {
        return "Hello World";
    }

    @GetMapping("/public/")
    public String publicHome() {
        return "Hello World!";
    }

}
