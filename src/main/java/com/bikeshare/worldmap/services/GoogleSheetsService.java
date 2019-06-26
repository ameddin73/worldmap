package com.bikeshare.worldmap.services;

import com.bikeshare.worldmap.model.Program;
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

    private static final String SPREADSHEET_ID = "1Gi_IXzesLdBNSuaWdw5UyQWl2vcPUJjHb7fKFWlq6vc";
    private static final String APPLICATION_NAME = "Bikeshare Worldmap";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS);
    private static final String TOKENS_DIRECTORY_PATH = "/tokens";
    private static final String CREDENTIALS_FILE_PATH = "/google-sheets-client-secret.json";
    private static final String RANGE = "All!A3:I10000";

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

    private static Program getProgram(List<Object> objects) {
        String city = objects.get(0).toString();
        String country = objects.get(1).toString();
        String continent = objects.get(2).toString();
        String name = objects.get(3).toString();
        Integer status = Integer.valueOf(objects.get(4).toString());
        Date startDate = null;
        Date endDate = null;
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
            } else {
                endDate = new SimpleDateFormat("yyyy-MM-dd").parse(eDate);
            }
        } catch (ParseException e) {
            log.error("Error parsing date: " + e.getMessage());
        }
        String url = objects.get(7).toString();
        String latLong = objects.get(8).toString();

        String[] csv = latLong.split(",");
        Float longitude = Float.valueOf(csv[0]);
        Float latitude = Float.valueOf(csv[1]);

        return new Program(city, continent, country, endDate, latitude, longitude,
                name, startDate, status, url);
    }

    private static Runnable updateData() {
        return new Runnable() {
            List<List<Object>> values;
            List<Program> programs;
            @Override
            public void run() {
                try {
                    values = getSheetAsList();
                } catch (IOException e) {
                    log.error("IO error reading google sheets: " + e.getMessage());
                } catch (GeneralSecurityException e) {
                    log.error("Security error reading google sheets: " + e.getMessage());
                }

                for (List<Object> objects : values)
                    programs.add(getProgram(objects));

                for (Program program : programs)
                    System.out.println(program.toString());
            }
        };
    }

    @PostConstruct
    private static void runDataUpdate() {
        log.info("Initializing Google Sheets scheduled service.");
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        scheduler.scheduleAtFixedRate(updateData(),0,1, TimeUnit.DAYS);
    }
}
