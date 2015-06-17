package tw.binary.dipper;

import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.googleapis.auth.oauth2.GoogleOAuthConstants;
import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.gmail.Gmail;
import com.google.api.services.gmail.model.ListThreadsResponse;
import com.google.api.services.gmail.model.Thread;

import java.io.FileReader;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

// Created by eason on 2015/4/20.
public class MyMail {

    // Check https://developers.google.com/gmail/api/auth/scopes for all available scopes
    private static final String SCOPE = "https://www.googleapis.com/auth/gmail.readonly";
    private static final String APP_NAME = "Campfire";
    // Email address of the user, or "me" can be used to represent the currently authorized user.
    private static final String USER = "magiclotw@gmail.com";
    // Path to the client_secret.json file downloaded from the Developer Console
    private static final String CLIENT_SECRET_PATH = "client_secret_789806255860-lpkqh7439j9rea3jbl50ua0gtvm23dgi.apps.googleusercontent.com.json";

    private static GoogleClientSecrets clientSecrets;

    public static void main(String[] args) throws IOException {
        HttpTransport httpTransport = new NetHttpTransport();
        JsonFactory jsonFactory = new JacksonFactory();

        clientSecrets = GoogleClientSecrets.load(jsonFactory, new FileReader(CLIENT_SECRET_PATH));

        // Allow user to authorize via url.
        GoogleAuthorizationCodeFlow flow = new GoogleAuthorizationCodeFlow.Builder(
                httpTransport, jsonFactory, clientSecrets, Arrays.asList(SCOPE))
                .setAccessType("online")
                .setApprovalPrompt("auto").build();

        String url = flow.newAuthorizationUrl().setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).build();
        System.out.println("Please open the following URL in your browser then type"
                + " the authorization Code:\n" + url);

        // Read Code entered by user.
        //BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        //String Code = br.readLine();
        String code = "Hello World!";

        // Generate Credential using retrieved Code.
        GoogleTokenResponse response = flow.newTokenRequest(code).setRedirectUri(GoogleOAuthConstants.OOB_REDIRECT_URI).execute();
        GoogleCredential credential = new GoogleCredential().setFromTokenResponse(response);

        // Create a new authorized Gmail API client
        Gmail service = new Gmail.Builder(httpTransport, jsonFactory, credential).setApplicationName(APP_NAME).build();

        // Retrieve a page of Threads; max of 100 by default.
        ListThreadsResponse threadsResponse = service.users().threads().list(USER).execute();
        List<Thread> threads = threadsResponse.getThreads();

        // Print ID of each Thread.
        for (Thread thread : threads) {
            System.out.println("Thread ID: " + thread.getId());
        }
    }
}
