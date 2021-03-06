package edu.illinois.ncsa.datawolf.service.client;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntity;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import edu.illinois.ncsa.datawolf.domain.Execution;
import edu.illinois.ncsa.datawolf.domain.Execution.State;
import edu.illinois.ncsa.datawolf.domain.Submission;
import edu.illinois.ncsa.datawolf.domain.Workflow;
import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.Person;

public class DataWolfServiceClient {

    private static Logger logger = LoggerFactory.getLogger(DataWolfServiceClient.class);

    public static String  SERVER = "";

    public static String createDataset(File file, String email) {
        String workflowId = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/datasets";
            HttpPost httppost = new HttpPost(requestUrl);

            FileBody bin = new FileBody(file);
            StringBody useremail = new StringBody(email);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("uploadedFile", bin);
            reqEntity.addPart("useremail", useremail);

            httppost.setEntity(reqEntity);

            logger.info("executing request " + httppost.getRequestLine());

            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            try {
                workflowId = httpclient.execute(httppost, responseHandler);
                logger.debug("response string" + workflowId);
            } catch (Exception e) {
                logger.error("HTTP post failed", e);
            }

        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return workflowId;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static String createWorkflow(File workflowZip) {
        String workflowId = null;

        HttpClient httpclient = new DefaultHttpClient();

        try {
            String requestUrl = SERVER + "/workflows";
            HttpPost httppost = new HttpPost(requestUrl);

            FileBody bin = new FileBody(workflowZip);

            MultipartEntity reqEntity = new MultipartEntity(HttpMultipartMode.BROWSER_COMPATIBLE);
            reqEntity.addPart("workflow", bin);

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

    public static Workflow getWorkflow(String id) {
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

    public static Execution getExecution(String executionId) {
        String responseStr = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.debug("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(responseStr, Execution.class);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {}
        }
        return null;
    }

    /**
     * 
     * @param email
     *            email address of the creator of the execution
     * @return
     *         list of executions
     */
    public static List<Execution> getExecutionsByEmail(String email) {
        if (email == null)
            return null;
        String responseStr = null;
        List<Execution> executions = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions?email=" + email;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.debug("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                executions = mapper.readValue(responseStr, new TypeReference<List<Execution>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return executions;
            } catch (Exception ignore) {}
        }
        return null;
    }

    /**
     * 
     * @param email
     *            email address of the creator of the execution
     * @param size
     *            number of executions in a page
     * @param page
     *            page number
     * @return
     *         list of executions
     */
    public static List<Execution> getExecutionsByEmail(String email, int size, int page) {
        if (email == null)
            return null;
        String responseStr = null;
        List<Execution> executions = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions?email=" + email + "&size=" + size + "&page=" + page;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.debug("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                executions = mapper.readValue(responseStr, new TypeReference<List<Execution>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return executions;
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

    public static void cancel(String executionId) {
        String responseStr = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/cancel";
            HttpPut httpPut = new HttpPut(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpPut.getRequestLine());

            try {
                responseStr = httpclient.execute(httpPut, responseHandler);
                logger.debug("Response String: " + responseStr);
                // ObjectMapper mapper = new ObjectMapper();
                // states = mapper.readValue(responseStr, new
                // TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {}
        }
    }

    public static void cancel(String executionId, List<String> stepIds) {
        String responseStr = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/cancel";
            if (stepIds != null) {
                if (!stepIds.isEmpty()) {
                    requestUrl += "?stepids=";
                    for (String stepId : stepIds) {
                        requestUrl += stepId + ";";
                    }
                    requestUrl = requestUrl.substring(0, requestUrl.length() - 1);
                }
            }

            HttpPut httpPut = new HttpPut(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpPut.getRequestLine());

            try {
                responseStr = httpclient.execute(httpPut, responseHandler);
                logger.debug("Response String: " + responseStr);
                // ObjectMapper mapper = new ObjectMapper();
                // states = mapper.readValue(responseStr, new
                // TypeReference<Map<String, String>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {}
        }
    }

    public static void startExecution(String executionId) {
        String responseStr = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/start";
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

    public static String postWorkflow(File zipfile) throws IOException {
        HttpClient httpclient = new DefaultHttpClient();
        String requestUrl = SERVER + "/workflows";
        HttpPost httpPost = new HttpPost(requestUrl);

        MultipartEntity entity = new MultipartEntity();
        entity.addPart("workflow", new FileBody(zipfile));
        httpPost.setEntity(entity);

        try {
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            return httpclient.execute(httpPost, responseHandler);
        } finally {
            httpclient.getConnectionManager().shutdown();
        }
    }

    public static String getWorkflowJson(String id) {
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

    public static boolean checkHpcFile(String executionId, String filename) {
        boolean check = false;
        String responseStr = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/executions/" + executionId + "/checkhpcfile?file=" + filename;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
                check = false;
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                if ("YES".equals(responseStr.trim().toUpperCase()))
                    check = true;
            } catch (Exception ignore) {}
        }
        return check;
    }

    public static boolean checkHpcFeil(String executionId) {
        return checkHpcFile(executionId, "error.rlt");
    }

    public static String createPerson(String firstName, String lastName, String email) {
        HttpClient httpclient = new DefaultHttpClient();
        String responseStr = null;
        try {
            String requestUrl = SERVER + "/persons?firstname=" + URLEncoder.encode(firstName, "UTF-8") + "&lastname=" + URLEncoder.encode(lastName, "UTF-8") + "&email="
                    + URLEncoder.encode(email, "UTF-8");
            HttpPost httpPost = new HttpPost(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpPost.getRequestLine());

            try {
                responseStr = httpclient.execute(httpPost, responseHandler);
                logger.debug("Response String: " + responseStr);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } catch (UnsupportedEncodingException e1) {
            logger.error("Can't URL encode", e1);
            return null;
        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return responseStr;
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static Person getPersonByEmail(String email) {
        String responseStr = null;
        List<Person> persons = null;

        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/persons?email=" + email;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.info("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                persons = mapper.readValue(responseStr, new TypeReference<List<Person>>() {});
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
                return persons.get(0);
            } catch (Exception ignore) {}
        }
        return null;
    }

    public static Dataset getDataset(String datasetId) {
        String responseStr = null;
        HttpClient httpclient = new DefaultHttpClient();
        try {
            String requestUrl = SERVER + "/datasets/" + datasetId;
            HttpGet httpGet = new HttpGet(requestUrl);

            ResponseHandler<String> responseHandler = new BasicResponseHandler();

            logger.debug("executing request " + httpGet.getRequestLine());

            try {
                responseStr = httpclient.execute(httpGet, responseHandler);
                logger.debug("Response String: " + responseStr);
                ObjectMapper mapper = new ObjectMapper();
                return mapper.readValue(responseStr, Dataset.class);
            } catch (Exception e) {
                logger.error("HTTP get failed", e);
            }

        } finally {
            try {
                httpclient.getConnectionManager().shutdown();
            } catch (Exception ignore) {}
        }
        return null;
    }

}
