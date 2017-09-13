package edu.illinois.ncsa.incore;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.Person;

public class IncoreDataset {

    // Datasets API
    public static final String  DATASETS_ENDPOINT       = "repo/api/datasets";
    public static final String  LIST                    = "list";

    // Dataset Attributes
    public static final String  MAEVIZ_MAPPING          = "maevizMapping";
    public static final String  SCHEMA                  = "schema";
    public static final String  FILENAME                = "filename";
    public static final String  LOCATION                = "location";
    public static final String  NAME                    = "name";
    public static final String  TYPE_ID                 = "typeId";
    public static final String  DESCRIPTION             = "description";
    public static final String  DATASET_ID              = "datasetId";
    private static final String FEATURE_TYPE_NAME       = "featuretypeName";
    public static final String  HTML_A_TAG_PATTERN      = "(?i)<a([^>]+)>(.+?)</a>";
    public static final String  HTML_A_HREF_TAG_PATTERN = "\\s*(?i)href\\s*=\\s*(\"([^\"]*\")|'[^']*'|([^'\">\\s]+))";

    // TODO pull out response keys as static strings

    public static Dataset getDataset(JsonObject datasetProperties, Person creator) {
        // Dataset Properties
        String datasetId = null;
        String description = null;
        String title = null;

        // FileDescriptor Properties
        String filename = null;
        String dataURL = null;

        JsonElement elem = datasetProperties.get(DATASET_ID);
        JsonObject datasetIdObj = datasetProperties.get(DATASET_ID).getAsJsonObject();
        datasetId = datasetIdObj.get(DESCRIPTION).getAsString();

        title = datasetProperties.get(NAME).getAsString();
        description = datasetProperties.get(TYPE_ID).getAsString();

        // Create dataset
        Dataset dataset = new Dataset();
        dataset.setCreator(creator);
        dataset.setId(datasetId);
        dataset.setDescription(description);
        dataset.setTitle(title);

        dataURL = datasetIdObj.get(LOCATION).getAsString();
        if (datasetProperties.has(FILENAME)) {
            filename = datasetProperties.get(FILENAME).getAsString();
            filename = datasetProperties.get(FEATURE_TYPE_NAME).getAsString();
        } else {
            // Should we log a warning?
            filename = ""; //$NON-NLS-1$
        }

        FileDescriptor fd = new FileDescriptor();
        fd.setDataURL(dataURL);
        fd.setFilename(filename);

        dataset.addFileDescriptor(fd);

        return dataset;

    }

}
