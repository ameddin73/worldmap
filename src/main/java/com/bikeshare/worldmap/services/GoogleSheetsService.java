package com.bikeshare.worldmap.services;

import com.bikeshare.worldmap.model.Program;
import com.bikeshare.worldmap.repository.ProgramRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Component
public class GoogleSheetsService {
    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsService.class);
    private ProgramRepository programRepository;

    private static final String SPREADSHEET_ID = "1Gi_IXzesLdBNSuaWdw5UyQWl2vcPUJjHb7fKFWlq6vc";
    private static final String APPLICATION_NAME = "Bikeshare Worldmap";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String GOOGLE_SERVICE_KEY = "google-service-key.json";
    private static final String RANGE = "All!A3:I10000";
    private static final int ERROR_TRIES = 3;

    public GoogleSheetsService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    public void getCities() {
        List<List<Object>> values = new ArrayList<>();
        int retry = 0;
        while (retry < ERROR_TRIES) {
            retry++;
            try {
                log.info("Reading from Google sheets...");
                values = getSheetAsList();
                break;
            } catch (IOException e) {
                log.error("IO error reading google sheets: " + e.getMessage());
            } catch (GeneralSecurityException | ClassNotFoundException e) {
                log.error("Security error reading google sheets: " + e.getMessage());
            }
        }

        if (retry == ERROR_TRIES && CollectionUtils.isEmpty(values)) {
            log.warn("Unable to update from google sheets.");
        }

        for (List<Object> objects : values) {
            retry = 0;
            while (retry < ERROR_TRIES) {
                retry++;
                try {
                    saveProgram(objects);
                    retry = ERROR_TRIES;
                } catch (Exception e) {
                    log.error("Error saving program: " + e.getClass().getCanonicalName() + " [" + e.getMessage() + "]");
                }
            }
        }
    }

    private static Credential getCredentials() throws IOException, ClassNotFoundException {
        InputStream inputStream = (new ClassPathResource(GOOGLE_SERVICE_KEY)).getInputStream();
        return GoogleCredential.fromStream(inputStream).createScoped(SCOPES);
    }

    private static List<List<Object>> getSheetAsList() throws IOException, GeneralSecurityException, ClassNotFoundException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials())
                .setApplicationName(APPLICATION_NAME).build();
        ValueRange response = service.spreadsheets().values().get(SPREADSHEET_ID,RANGE).execute();
        return response.getValues();
    }

    private void saveProgram(List<Object> objects) {
        String city = null;
        String country = null;
        String continent = null;
        String name = null;
        Integer status = null;
        Date startDate = null;
        Date endDate = null;
        String url = null;
        Float longitude = null;
        Float latitude = null;
        try {
            city = objects.get(0).toString();
            country = objects.get(1).toString();
            continent = objects.get(2).toString();
            name = objects.get(3).toString();
            status = Integer.valueOf(objects.get(4).toString());
            startDate = null;
            endDate = null;
            try {
                String sDate = objects.get(5).toString();
                String eDate = objects.get(6).toString();
                if (sDate.length() == 4) {
                    startDate = new SimpleDateFormat("yyyy").parse(sDate);
                } else {
                    startDate = new SimpleDateFormat("yyyy-MM-dd").parse(sDate);
                }
                if (eDate.length() == 4) {
                    endDate = new SimpleDateFormat("yyyy").parse(eDate);
                } else if (eDate.length() != 0){
                    endDate = new SimpleDateFormat("yyyy-MM-dd").parse(eDate);
                }
            } catch (ParseException e) {
                log.error("Error parsing date: " + e.getMessage());
                System.out.println(name + "was fucky...");
            }
            url = objects.get(7).toString();

            String latLong = objects.get(8).toString();
            if (latLong.length() != 0) {
                try {
                    String[] csv = latLong.split(",");
                    longitude = Float.valueOf(csv[0]);
                    latitude = Float.valueOf(csv[1]);
                } catch (Exception e) {
                    log.error("Error parsing latitude or longitude: " + e.getMessage());
                }
            }
        } catch (IndexOutOfBoundsException e) {
            log.error("Error saving program " + name + ": " + e.getClass().getCanonicalName() + ": " + e.getMessage());
        }

        programRepository.save(new Program(city, continent, country, endDate, latitude, longitude,
                name, startDate, status, url));
    }
}
