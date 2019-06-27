package com.bikeshare.worldmap.services;

import com.bikeshare.worldmap.model.Program;
import com.bikeshare.worldmap.repository.ProgramRepository;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.extensions.java6.auth.oauth2.AuthorizationCodeInstalledApp;
import com.google.api.client.extensions.jetty.auth.oauth2.LocalServerReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.io.*;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;

@Component
public class GoogleSheetsService {
    private static final Logger log = LoggerFactory.getLogger(GoogleSheetsService.class);
    private ProgramRepository programRepository;

    private static final String SPREADSHEET_ID = "1Gi_IXzesLdBNSuaWdw5UyQWl2vcPUJjHb7fKFWlq6vc";
    private static final String APPLICATION_NAME = "Bikeshare Worldmap";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "/tokens";
    private static final String CREDENTIALS_FILE_PATH = "/google-sheets-client-secret.json";
    private static final String RANGE = "All!A3:I10000";
    private static final int ERROR_TRIES = 5;

    @Autowired
    public GoogleSheetsService(ProgramRepository programRepository) {
        this.programRepository = programRepository;
    }

    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException, GeneralSecurityException {
        //load client secrets
        InputStream in = GoogleSheetsService.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        //build flow and trigger user auth req
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline").build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static List<List<Object>> getSheetAsList() throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
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

    private Runnable updateData() {
        return new Runnable() {
            List<List<Object>> values;
            @Override
            public void run() {
                int retry = 0;
                while (retry < ERROR_TRIES) {
                    retry++;
                    try {
                        values = getSheetAsList();
                        retry = ERROR_TRIES;
                    } catch (IOException e) {
                        log.error("IO error reading google sheets: " + e.getMessage());
                    } catch (GeneralSecurityException e) {
                        log.error("Security error reading google sheets: " + e.getMessage());
                    }
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
        };
    }

    @PostConstruct
    private void runDataUpdate() {
        log.info("Initializing Google Sheets scheduled service.");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(updateData(),0,1, TimeUnit.DAYS);
    }
}
