
package com.example;

import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import javax.swing.JOptionPane;

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
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.sheets.v4.Sheets;
import com.google.api.services.sheets.v4.SheetsScopes;
import com.google.api.services.sheets.v4.model.ValueRange;

public class App {

    private static final String APPLICATION_NAME = "Chinese Club Attendance Tally";
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static final String TOKENS_DIRECTORY_PATH = "tokens";

    /**
     * Global instance of the scopes required by this quickstart. If modifying these
     * scopes, delete your previously saved tokens/ folder.
     */
    private static final List<String> SCOPES = Arrays.asList(
        DriveScopes.DRIVE_METADATA_READONLY,
        SheetsScopes.SPREADSHEETS_READONLY);
    private static final String CREDENTIALS_FILE_PATH = "/credentials.json";

    /**
     * Creates an authorized Credential object.
     * 
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
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow
            .Builder(HTTP_TRANSPORT, JSON_FACTORY,clientSecrets, SCOPES)
            .setDataStoreFactory(new FileDataStoreFactory(new java.io.File(TOKENS_DIRECTORY_PATH)))
            .setAccessType("offline")
            .build();
        LocalServerReceiver receiver = new LocalServerReceiver.Builder().setPort(8888).build();
        return new AuthorizationCodeInstalledApp(flow, receiver).authorize("user");
    }

    private static Sheets getSheetsService(NetHttpTransport HTTP_TRANSPORT) throws GeneralSecurityException, IOException {
        // Build a new authorized sheets client service.
        Sheets service = new Sheets.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
        return service;
    }

    private static Drive getDriveService(NetHttpTransport HTTP_TRANSPORT) throws GeneralSecurityException, IOException {
        // Builds a new authorized drive client service.
        Drive service = new Drive.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredentials(HTTP_TRANSPORT))
                .setApplicationName(APPLICATION_NAME).build();
        return service;
    }

    private static List<File> getFiles (NetHttpTransport HTTP_TRANSPORT) throws GeneralSecurityException, IOException {
        // Gets a list of all the attendance spreadsheets in the Attendance Spreadsheets folder.
        FileList result = getDriveService(HTTP_TRANSPORT).files().list()
            .setQ("parents in '10tDJRo_FL6RnPsiLHJe4OmQ-1BYZf1mg' and starred = true")
            .setSpaces("drive")
            .setFields("nextPageToken, files(id, name)")
            .execute();
        return result.getFiles();
    }

    /**
     * Creates a HashMap | key -> name of club member | value -> # of meetings attended
     * 
     * @return HashMap containing attendance tally
     * @throws IOException
     * @throws GeneralSecurityException
     */
    public static HashMap<String, Integer> getTally () throws IOException, GeneralSecurityException {
        final NetHttpTransport HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        
        HashMap<String, Integer> tally = new HashMap<>();
        List<File> files = getFiles(HTTP_TRANSPORT);

        if (files == null || files.isEmpty()) {
            System.out.println("No files found.");
        } else {
            // Iterates through each file in the correct folder and tries to get sheets responses from it.
            for (File file : files) {
                final String range = "C2:C";
                try {
                    ValueRange response = getSheetsService(HTTP_TRANSPORT).spreadsheets().values()
                        .get(file.getId(), range)
                        .execute();
                    List<List<Object>> values = response.getValues();

                    if (values == null || values.isEmpty()) {
                        System.out.println("No data found.");
                    } 
                    else {
                        for (List<Object> row : values) {
                            String s = row.get(0).toString().toLowerCase();
                            if (!tally.containsKey(s)) {
                                tally.put(s, 1);
                            } 
                            else {
                                tally.replace(s, tally.get(s) + 1);
                            }
                        }
                    }
                }
                //ignores if != sheets
                catch (com.google.api.client.googleapis.json.GoogleJsonResponseException c) {
                }
            }
        }
        return tally;
    }

    /**
     * Makes a HashSet of all the people who've attend >= 3 meetings
     * 
     * @param tally
     * @return HashSet of people to be printed out
     */
    public static HashSet<String> count(HashMap<String, Integer> tally) {
        HashSet<String> memberList = new HashSet<>();
        for (String s : tally.keySet()) {
            if (tally.get(s) >= 3) {
                memberList.add(s);
            }
        }
        return memberList;
    }

    public static void main(String[] args) throws IOException, GeneralSecurityException {

        JOptionPane.showMessageDialog(null, "<html>This application reports the students who have attended at least three Chinese Club meetings within the selected attendance spreadsheets.<br/>Within the Google Drive Folder, please <i>star</i> the Sheets you wish to include in this tally.</html>", "Welcome", JOptionPane.INFORMATION_MESSAGE);

        HashSet<String> hs = count(getTally());

        // Write names to new .txt file
        java.io.File f = new java.io.File("Attendance.txt");
        FileWriter fw = new FileWriter(f);
        for (String s : hs) {
            fw.write(s + "\n");
        }
        fw.close();

        JOptionPane.showMessageDialog(null, "A .txt file has been created in your parent folder", "Finished", JOptionPane.INFORMATION_MESSAGE);
    }
}