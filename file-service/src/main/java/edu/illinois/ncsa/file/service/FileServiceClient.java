package edu.illinois.ncsa.file.service;

import java.io.File;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

public class FileServiceClient {
    public static String SERVER     = "";
    HttpClient           httpclient = new DefaultHttpClient();

    public String post(File file) {
        String responseStr = null;
        try {
            String requestUrl = SERVER + "/files";
            HttpPost httppost = new HttpPost(requestUrl);

            FileBody bin = new FileBody(file);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("file", bin);

            httppost.setEntity(reqEntity);

            System.out.println("executing request " + httppost.getRequestLine());

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            try {
                responseStr = httpclient.execute(httppost, responseHandler);
                System.out.println(responseStr);
            } catch (Exception e) {
                System.out.println("HTTP post failed");
                System.out.println(e.getMessage());
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return responseStr;
            } catch (Exception ignore) {}
        }
        return responseStr;
    }

}
