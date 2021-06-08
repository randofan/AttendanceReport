
package com.example;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

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

/**
 * Hello world!
 *
*/ 
public class App 
{

    private static final String APPLICATION_NAME = "Chinese Club Attendance Tally";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart.
     * If modifying these scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Collections.singletonList(SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * @param HTTP_TRANSPORT The network HTTP Transport.
     * @return An authorized Credential object.
     * @throws IOException If the credentials.json file cannot be found.
     */
    private static Credential getCredentials(final NetHttpTransport HTTP_TRANSPORT) throws IOException {
        // Load client secrets.
        InputStream in = App.class.getResourceAsStream(CREDENTIALS_FILE_PATH);
        if (in == null) {
            throw new FileNotFoundException("Resource not found: " + CREDENTIALS_FILE_PATH);
        }
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
                .setAccessType("offline")
                .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }
    public static void main(String... args) throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME)
                .build();
        // Build a new authorized API client service.
        
        final String spreadsheetId = "1ztBotcQlxz1n9ngpdMAddoKuaZV2348igfCZ1ltdyBM";
        final String range = "C2:C";
        
        ValueRange response = service.spreadsheets().values()
                .get(spreadsheetId, range)
                .execute();
        List<List<Object>> values = response.getValues();
        if (values == null || values.isEmpty()) {
            System.out.println("No data found.");
        } else {
            for (List row : values) {
                // Print columns A and E, which correspond to indices 0 and 4.
                System.out.printf("%s\n", row.get(0));
            }
        }
    }

    /*
    private static Sheets sheetsService;
    private static String APPLICATION_NAME = "Google Sheets Example";
    private static String SPREADSHEET_ID = "1ztBotcQlxz1n9ngpdMAddoKuaZV2348igfCZ1ltdyBM";

    private static Credential authorize() throws IOException, GeneralSecurityException {
        InputStream in = App.class.getResourceAsStream("/credentials.json");
        GoogleClientSecrets clientSecrets = GoogleClientSecrets.load(
            JacksonFactory.getDefaultInstance(), new InputStreamReader(in));

        List<String> scopes = Arrays.asList(SheetsScopes.SPREADSHEETS);

        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
            GoogleNetHttpTransport.newTrustedTransport(), JacksonFactory.getDefaultInstance(),
            clientSecrets, scopes)
            .setDataStoreFactory(new FileDataStoreFactory(new File("tokens")))
            .setAccessType("offline")
            .build();
        Credential credential = new AuthorizationCodeInstalledApp(flow, new LocalServerReceiver())
            .authorize("user");
        
        return credential;
    }

    public static Sheets getServiceSheets() throws IOException, GeneralSecurityException {
        Credential credential = authorize();
        return new Sheets.Builder(GoogleNetHttpTransport.newTrustedTransport(), 
            JacksonFactory.getDefaultInstance(), credential)
            .setApplicationName(APPLICATION_NAME)
            .build();
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {
        sheetsService = getServiceSheets();
        String range = "1/12 Chinese Club Meeting (Responses)!C2:C30";
        ValueRange responses = sheetsService.spreadsheets().values()
            .get(SPREADSHEET_ID, range)
            .execute();

        List<List<Object>> values = responses.getValues();

        if (values == null || values.isEmpty()) {
            System.out.println("No data");
        }
        else {
            for (List row : values) {
                System.out.println(row.get(0));
            }
        }
    }
    */

    /*
    public static void main(String[] args) throws FileNotFoundException {
        HashSet<String> set = count(createHash());
        for (String s : set) {
            System.out.println(s);
        }
    }
    public static HashMap<String, Integer> createHash () throws FileNotFoundException {
        HashMap<String, Integer> tally = new HashMap<>();
        File f;
        Scanner sc;
        for (int i = 1; i < 7; i++) {
            f = new File("" + i + ".txt");
            sc = new Scanner(f);
            while (sc.hasNextLine()) {
                String s = sc.nextLine().toLowerCase();
                if (!tally.containsKey(s)) {
                    tally.put(s, 1);
                }
                else {
                    tally.replace(s, tally.get(s) + 1);
                }
            }

        }
        return tally;
    }

    public static HashSet<String> count (HashMap<String, Integer> tally) {
        HashSet<String> memberList = new HashSet<>();
        for (String s : tally.keySet()) {
            if (tally.get(s) >= 3) {
                memberList.add(s);
            }
        }
        return memberList;
    }
    */
}