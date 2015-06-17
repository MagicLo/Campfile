package tw.binary.dipper.util;

import android.content.Context;

import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.googleapis.auth.oauth2.GoogleCredential;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.InputStreamContent;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.services.storage.Storage;
import com.google.api.services.storage.StorageScopes;
import com.google.api.services.storage.model.Bucket;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class MyUtilsGcs {
    private static Properties properties;
    private static Storage storage;

    private static final String PROJECT_ID_PROPERTY = "graceful-design-89523";
    private static final String APPLICATION_NAME_PROPERTY = "Polaris";
    private static final String ACCOUNT_ID_PROPERTY = "789806255860-ufrghvsss6japmlnj0cmrsds1okp5o9c@developer.gserviceaccount.com";
    private static final String PRIVATE_KEY_PATH_PROPERTY = "gae00002-000c14c84cd6.p12";
    private static final String BUCKET_NAME = "dippertest2";

    public static void uploadFile(Context context, String filePath)
            throws Exception {

        Storage storage = getStorage(context);

        //StorageObject object = new StorageObject();
        //object.setBucket(bucketName);

        File file = new File(filePath);

        InputStream stream = new FileInputStream(file);
        try {
            String contentType = "image/jpeg";
            InputStreamContent content = new InputStreamContent(contentType, stream);
            Storage.Objects.Insert insert = storage.objects().insert(BUCKET_NAME, null, content);
            insert.setName(file.getName());
            insert.execute();
        } finally {
            stream.close();
        }
    }

    public static void downloadFile(Context context, String fileName, String destinationDirectory) throws Exception {

        File directory = new File(destinationDirectory);
        if (!directory.isDirectory()) {
            throw new Exception("Provided destinationDirectory path is not a directory");
        }
        File file = new File(directory.getAbsolutePath() + "/" + fileName);

        Storage storage = getStorage(context);

        Storage.Objects.Get get = storage.objects().get(BUCKET_NAME, fileName);

        FileOutputStream stream = new FileOutputStream(file);
        try {
            get.executeAndDownloadTo(stream);
        } finally {
            stream.close();
        }
    }

    public static void downloadMediaFile(Context context, String fileName, String destinationDirectory) throws Exception {

        File directory = new File(destinationDirectory);
        if (!directory.isDirectory()) {
            throw new Exception("Provided destinationDirectory path is not a directory");
        }
        //Local filesystem
        File file = new File(directory.getAbsolutePath() + "/" + fileName);

        Storage storage = getStorage(context);

        Storage.Objects.Get get = storage.objects().get(BUCKET_NAME, fileName);

        FileOutputStream stream = new FileOutputStream(file);
        try {
            get.executeMediaAndDownloadTo(stream);
        } finally {
            stream.close();
        }
    }

    /**
     * Deletes a file within a bucket
     *
     * @param fileName The file to delete
     * @throws Exception
     */
    public static void deleteFile(Context context, String fileName)
            throws Exception {

        Storage storage = getStorage(context);

        storage.objects().delete(BUCKET_NAME, fileName).execute();
    }

    /**
     * Creates a bucket
     *
     * @param bucketName Name of bucket to create
     * @throws Exception
     */
    public static void createBucket(Context context, String bucketName) throws Exception {

        Storage storage = getStorage(context);

        Bucket bucket = new Bucket();
        bucket.setName(bucketName);

        storage.buckets().insert(PROJECT_ID_PROPERTY, bucket).execute();
    }

    private static Storage getStorage(Context context) throws Exception {

        InputStream keyInputStream = context.getAssets().open(PRIVATE_KEY_PATH_PROPERTY);

        if (storage == null) {
            HttpTransport httpTransport = new NetHttpTransport();
            JsonFactory jsonFactory = new JacksonFactory();

            List<String> scopes = new ArrayList<String>();
            scopes.add(StorageScopes.DEVSTORAGE_READ_WRITE);
            File file = MyUtils.stream2file(keyInputStream);

            Credential credential = new GoogleCredential.Builder()
                    .setTransport(httpTransport)
                    .setJsonFactory(jsonFactory)
                    .setServiceAccountId(ACCOUNT_ID_PROPERTY)
                    .setServiceAccountPrivateKeyFromP12File(file)
                    .setServiceAccountScopes(scopes)
                    .build();

            storage = new Storage.Builder(httpTransport, jsonFactory, credential)
                    .setApplicationName(APPLICATION_NAME_PROPERTY)
                    .build();
        }

        return storage;
    }

}

