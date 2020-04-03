package com.bikeshare.worldmap.web;

import com.bikeshare.worldmap.services.GoogleSheetsService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class UpdateController {

    private final GoogleSheetsService googleSheetsService;

    public UpdateController(GoogleSheetsService googleSheetsService) {
        this.googleSheetsService = googleSheetsService;
    }

    @GetMapping("/update")
    public String update() {
        googleSheetsService.getCities();
        return "Updating...";
    }
}
