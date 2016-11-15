package edu.illinois.ncsa.domain.util;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.Persistence;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

public class DatasetUtil {
    private static Logger log = LoggerFactory.getLogger(DatasetUtil.class);

    public static boolean deleteDataset(String datasetId) {
        log.info("delete dataset by dataset id: " + datasetId);

        // Transaction t = SpringData.getTransaction();

        DatasetDao datasetDao = Persistence.getBean(DatasetDao.class);

        // find dataset
        Dataset dataset = datasetDao.findOne(datasetId);

        // get all fd from the dataset
        List<FileDescriptor> fdList = dataset.getFileDescriptors();

        // get filestorage
        // FileStorage fileStorage = SpringData.getFileStorage();
        FileStorage fileStorage = Persistence.getBean(FileStorage.class);

        FileDescriptorDao fileDescriptorDao = Persistence.getBean(FileDescriptorDao.class);

        // Delete dataset first, otherwise deleting filedescriptor(s) fails
        // Because there is a dataset table that still contains references
        datasetDao.delete(dataset);

        for (FileDescriptor fd : fdList) {
            if (fileStorage.deleteFile(fd)) {
                fileDescriptorDao.delete(fd);
            } else {
                // try {
                // t.rollback();
                // } catch (Exception e) {
                // log.error("Can't rollback when deleting dataset id:" +
                // datasetId,
                // e);
                // }
                return false;
            }
        }

        // try {
        // t.commit();
        // } catch (Exception e) {
        // log.error("Can't commit when deleting dataset id:" + datasetId, e);
        // return false;
        // }
        return true;
    }

}
