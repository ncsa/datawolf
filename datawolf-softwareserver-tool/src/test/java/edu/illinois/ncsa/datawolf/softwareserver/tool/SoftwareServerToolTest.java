package edu.illinois.ncsa.datawolf.softwareserver.tool;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.HttpClientBuilder;

public class SoftwareServerToolTest {
    private String  host           = "http://localhost:8182";
    private String  application    = "ImageMagick";
    private String  targetMimeType = "image/gif";

    private MimeMap mimeMap        = new MimeMap();

    // @Test
    public void testConvertToHTML() throws Exception {
        String fileLocation = "src/test/resources/stdout.txt";
        targetMimeType = "text/html";
        application = "txt2html";
        // InputStream is = new URL(fileUrl).openStream();

        File tmpFolder = File.createTempFile("nara", ".cbi");
        tmpFolder.delete();
        tmpFolder.mkdirs();

        File sourceFile;
        File targetFile;
        try {
            sourceFile = File.createTempFile("source", ".txt", tmpFolder);
            writeFile(new FileInputStream(fileLocation), sourceFile);
            targetFile = File.createTempFile("target", "." + mimeMap.getFileExtension(targetMimeType), tmpFolder);

            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient client = builder.build();

            String taskUri = getTaskURI();
            System.out.println("task uri = " + taskUri);
            HttpPost httpPost = new HttpPost(taskUri);

            FileBody fileBody = new FileBody(sourceFile);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addPart("file", fileBody);

            httpPost.setEntity(entityBuilder.build());
            BasicResponseHandler responseHandler = new BasicResponseHandler();
            String response = client.execute(httpPost, responseHandler);

            parseResult(response, targetFile);

            System.out.println("content: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // @Test
    public void testApplications() throws Exception {
        System.out.println("software");
        HttpClientBuilder builder = HttpClientBuilder.create();
        HttpClient client = builder.build();
        HttpGet httpGet = new HttpGet("http://localhost:8182/software");
        BasicResponseHandler responseHandler = new BasicResponseHandler();

        String responseStr = client.execute(httpGet, responseHandler);

        String[] apps = responseStr.split("\n");
        System.out.println(apps.length);
        Arrays.sort(apps);
        Pattern htmltag = Pattern.compile("<a\\b[^>]*href=\"[^>]*>(.*?)</a>");
        Pattern link = Pattern.compile("href=\"[^>]*\">");
        for (String app : apps) {
            Matcher tagmatch = htmltag.matcher(app);
            while (tagmatch.find()) {
                try {
                    Matcher matcher = link.matcher(tagmatch.group());
                    if (matcher.find()) {
                        String foundlink = matcher.group().replaceFirst("href=\"", "").replaceFirst("\">", "").replaceFirst("\"[\\s]?target=\"[a-zA-Z_0-9]*", "");
                        System.out.println(foundlink);
                    }
                } catch (Exception e) {

                }
            }
        }
    }

    // @Test
    public void testFileConversion() throws Exception {
        String fileLocation = "src/test/resources/ergo-splash.png";
        // InputStream is = new URL(fileUrl).openStream();

        File tmpFolder = File.createTempFile("nara", ".cbi");
        tmpFolder.delete();
        tmpFolder.mkdirs();

        File sourceFile;
        File targetFile;
        try {
            sourceFile = File.createTempFile("source", ".png", tmpFolder);
            writeFile(new FileInputStream(fileLocation), sourceFile);
            targetFile = File.createTempFile("target", "." + mimeMap.getFileExtension(targetMimeType), tmpFolder);

            HttpClientBuilder builder = HttpClientBuilder.create();
            HttpClient client = builder.build();

            String taskUri = getTaskURI();
            System.out.println("task uri = " + taskUri);
            HttpPost httpPost = new HttpPost(taskUri);

            FileBody fileBody = new FileBody(sourceFile);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create();
            entityBuilder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
            entityBuilder.addPart("file", fileBody);

            httpPost.setEntity(entityBuilder.build());
            BasicResponseHandler responseHandler = new BasicResponseHandler();
            String response = client.execute(httpPost, responseHandler);

            parseResult(response, targetFile);

            System.out.println("content: " + response);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void parseResult(String response, File targetFile) throws Exception {
        Pattern patternTag = Pattern.compile(SoftwareServer.HTML_A_TAG_PATTERN);
        Pattern patternLink = Pattern.compile(SoftwareServer.HTML_A_HREF_TAG_PATTERN);
        Matcher matcherTag = patternTag.matcher(response);

        while (matcherTag.find()) {
            String href = matcherTag.group(1);
            Matcher matcherLink = patternLink.matcher(href);
            while (matcherLink.find()) {
                String link = matcherLink.group(1);

                InputStream is = retrieveFile(link);
                writeFile(is, targetFile);
            }
        }
    }

    private void writeFile(InputStream is, File file) {
        OutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(file);
            int read = 0;
            byte[] buffer = new byte[10240];
            while ((read = is.read(buffer)) != -1) {
                outputStream.write(buffer, 0, read);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (outputStream != null) {
                try {
                    outputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

    }

    private InputStream retrieveFile(String link) {
        InputStream is = null;
        URL url;
        boolean success = false;
        int numRetries = 0;
        while (numRetries < SoftwareServer.ATTEMPTS && !success) {
            try {
                url = new URL(link);
                URLConnection con = url.openConnection();
                is = con.getInputStream();
                success = true;
            } catch (MalformedURLException e) {
                // something went wrong with the URL
                return is;
            } catch (IOException e) {
                // File is not there yet, wait
                try {
                    Thread.sleep(SoftwareServer.WAIT_TIME_MS);
                    numRetries++;
                } catch (InterruptedException e1) {
                    // do nothing
                }
            }
        }

        return is;
    }

    private String getTaskURI() {

        String taskUri = host;
        if (!taskUri.endsWith("/")) {
            taskUri = taskUri.concat("/");
        }

        String mimeExt = mimeMap.getFileExtension(targetMimeType);

        taskUri += SoftwareServer.SOFTWARE + "/" + application + SoftwareServer.TASK_CONVERT + "/" + mimeExt;
        return taskUri;
    }
}
