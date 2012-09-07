package edu.illinois.ncsa.cyberintegrator.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.multipart.FilePart;
import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
import org.apache.commons.httpclient.methods.multipart.Part;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution.State;
import edu.illinois.ncsa.cyberintegrator.domain.Submission;
import edu.illinois.ncsa.cyberintegrator.domain.Workflow;
import edu.illinois.ncsa.domain.FileDescriptor;

public class CyberintegratorServiceClient {

    private static Logger logger = LoggerFactory.getLogger(CyberintegratorServiceClient.class);

    public static String  SERVER = "";

    public static String createWorkflow(File workflowZip) {
        String workflowId = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/workflow";
            HttpPost httppost = new HttpPost(requestUrl);

            FileBody bin = new FileBody(workflowZip);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("uploadedFile", bin);

            httppost.setEntity(reqEntity);

            logger.info("executing request " + httppost.getRequestLine());

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                workflowId = httpclient.execute(httppost, responseHandler);
                logger.debug("response string" + workflowId);
            } catch (Exception e) {
                logger.error("HTTP post failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return workflowId;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static String submit(Submission submission) {
        String executionId = null;

        HttpClient httpClient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/executions";
            HttpPost httppost = new HttpPost(requestUrl);

            ObjectMapper mapper = new ObjectMapper();
            String json = mapper.writeValueAsString(submission);

            StringEntity input = new StringEntity(json);
            input.setContentType("application/json");
            httppost.setEntity(input);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {

                executionId = httpClient.execute(httppost, responseHandler);

                logger.info("Submitting ci workflow (submission): " + httppost.getRequestLine());

                logger.info("Execution id: " + executionId);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }
        } catch (JsonGenerationException e1) {
            logger.error("Can't generate Json from object", e1);
        } catch (JsonMappingException e1) {
            logger.error("Can't generate Json from object", e1);
        } catch (IOException e1) {
            logger.error("Can't generate Json from object", e1);
        } finally {
            try {
                httpClient.getConnectionManager().shutdown();
                return executionId;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static Map<String, State> getState(String executionId) {
        String responseStr = null;
        Map<String, State> states = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/state";
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                states = mapper.readValue(responseStr, new TypeReference<Map<String, State>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return states;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static List<String> getListOutput(String executionId) {
        String responseStr = null;
        List<String> result = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/outputs";
            HttpPut httpPut = new HttpPut(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpPut.getRequestLine());

            try {
                responseStr = httpclient.execute(httpPut, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                result = mapper.readValue(responseStr, new TypeReference<List<String>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return result;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static String getOutputUrl(String executionId, String outputId) {
        String responseStr = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/outputs/" + outputId;
            HttpPut httpPut = new HttpPut(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpPut.getRequestLine());

            try {
                responseStr = httpclient.execute(httpPut, responseHandler);
                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return responseStr;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static void startExecutionById(String id) {
        String responseStr = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + id + "/start";
            HttpPut httpPut = new HttpPut(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpPut.getRequestLine());

            try {
                responseStr = httpclient.execute(httpPut, responseHandler);
                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {}
        }
    }

    public static Workflow getWorkflowById(String id) {
        String responseStr = null;
        Workflow wf = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/workflows/" + id;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                wf = mapper.readValue(responseStr, Workflow.class);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return wf;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static String postWorkflow(File zipfile) throws IOException {
        org.apache.commons.httpclient.HttpClient httpclient = new org.apache.commons.httpclient.HttpClient();
        String requestUrl = SERVER + "/workflows";
        PostMethod httpPost = new PostMethod(requestUrl);

        Part[] parts = { new FilePart("workflow", zipfile) };
        httpPost.setRequestEntity(new MultipartRequestEntity(parts, httpPost.getParams()));
        int status = httpclient.executeMethod(httpPost);
        String response = httpPost.getResponseBodyAsString();
        if (status == 200) {
            return response;
        } else {
            throw (new IOException("Error uploading workflow. Status code=" + status + ", message=" + response));
        }
    }

    public static String getWorkflowJSONById(String id) {
        String responseStr = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/workflows/" + id;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return responseStr;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static FileDescriptor get(String id) {
        String responseStr = null;
        FileDescriptor fd = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/files/" + id;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                fd = mapper.readValue(responseStr, FileDescriptor.class);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return fd;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static InputStream getFile(String id) {
        HttpResponse response = null;
        InputStream is = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/files/" + id + "/file";
            HttpGet httpGet = new HttpGet(requestUrl);

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                response = httpclient.execute(httpGet);
                HttpEntity entity = response.getEntity();
                String tmpdir = System.getProperty("java.io.tmpdir");
                File tmpFile = new File(tmpdir, UUID.randomUUID().toString());
                FileOutputStream fos = new FileOutputStream(tmpFile);
                entity.writeTo(fos);
                fos.close();
                logger.debug("File is written at " + tmpFile.getAbsolutePath());
                is = new FileInputStream(tmpFile);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return is;
            } catch (Exception ignore) {}
        }
        return null;
    }

}
