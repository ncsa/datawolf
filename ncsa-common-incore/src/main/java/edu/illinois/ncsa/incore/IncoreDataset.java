package edu.illinois.ncsa.incore;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;

public class IncoreDataset {

    // Datasets API
    public static final String  DATASETS_ENDPOINT       = "data/api/datasets";
    public static final String  CREATE_DATASET          = "ingest-dataset";
    public static final String  ADD_DATASET_FILES       = "ingest-multi-files";
    public static final String  UPDATE                  = "update";
    public static final String  LIST                    = "list";

    // Dataset Attributes
    public static final String  MAEVIZ_MAPPING          = "maevizMapping";
    public static final String  SCHEMA                  = "schema";
    public static final String  FILENAME                = "filename";
    public static final String  LOCATION                = "location";
    public static final String  DATA_URL                = "dataURL";
    public static final String  TITLE                   = "title";
    public static final String  TYPE                    = "type";
    public static final String  DESCRIPTION             = "description";
    public static final String  ENTITY_ID               = "id";
    private static final String FEATURE_TYPE_NAME       = "featuretypeName";
    public static final String  HTML_A_TAG_PATTERN      = "(?i)<a([^>]+)>(.+?)</a>";
    public static final String  HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

    // TODO pull out response keys as static strings

    public static Dataset getDataset(JsonObject datasetProperties, Person creator) {
        // Dataset Properties
        String datasetId = datasetProperties.get(ENTITY_ID).getAsString();
        String title = datasetProperties.get(TITLE).getAsString();
        String description = datasetProperties.get(TYPE).getAsString();

        // FileDescriptor Properties
        JsonArray fileDescriptors = datasetProperties.get("fileDescriptors").getAsJsonArray();

        // Create dataset
        Dataset dataset = new Dataset();
        dataset.setCreator(creator);
        dataset.setId(datasetId);
        dataset.setDescription(description);
        dataset.setTitle(title);

        for (int index = 0; index < fileDescriptors.size(); index++) {
            JsonObject fileDescriptor = fileDescriptors.get(index).getAsJsonObject();

            FileDescriptor fd = new FileDescriptor();
            fd.setId(fileDescriptor.get(ENTITY_ID).getAsString());
            fd.setDataURL(fileDescriptor.get(DATA_URL).getAsString());
            if (fileDescriptor.has(FILENAME)) {
                if (!fileDescriptor.get(FILENAME).isJsonNull()) {
                    fd.setFilename(fileDescriptor.get(FILENAME).getAsString());
                }
            }
            dataset.addFileDescriptor(fd);
        }

        return dataset;

    }

}
