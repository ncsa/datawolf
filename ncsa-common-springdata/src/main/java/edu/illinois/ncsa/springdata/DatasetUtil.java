package edu.illinois.ncsa.springdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;

public class DatasetUtil {
    Logger log = LoggerFactory.getLogger(DatasetUtil.class);

    public static void deleteDataset(String datasetId) {
        // find dataset
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Dataset dataset = datasetDao.findOne(datasetId);

        // get all fd from the dataset
        List<FileDescriptor> fdList = dataset.getFileDescriptors();

        // deleta dataset
        datasetDao.delete(datasetId);

        // get filestorage
        FileDescriptorDAO fdDao = SpringData.getBean(FileDescriptorDAO.class);
        FileStorage fileStorage = SpringData.getFileStorage();

        // delete fd
        for (FileDescriptor fd : fdList) {
            // TODO: need to check whether fd is used by other dataset
            boolean success = fileStorage.deleteFile(fd);

            fdDao.delete(fd);
        }
    }

}
