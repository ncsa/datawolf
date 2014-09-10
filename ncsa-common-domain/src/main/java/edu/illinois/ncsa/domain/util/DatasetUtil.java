package edu.illinois.ncsa.domain.util;

import java.util.List;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;
import edu.illinois.ncsa.domain.FileStorage;
import edu.illinois.ncsa.domain.dao.DatasetDao;
import edu.illinois.ncsa.domain.dao.FileDescriptorDao;

public class DatasetUtil {
    private static Logger            log = LoggerFactory.getLogger(DatasetUtil.class);

    @Inject
    private static DatasetDao        datasetDao;

    @Inject
    private static FileDescriptorDao fileDescriptorDao;

    @Inject
    private static FileStorage       fileStorage;

    // TODO: assuming that there is only 1 file for dataset
    public static boolean deleteDataset(String datasetId) {
        log.info("delete dataset by dataset id: " + datasetId);

        // Transaction t = SpringData.getTransaction();

        // find dataset
        Dataset dataset = datasetDao.findOne(datasetId);

        // get all fd from the dataset
        List<FileDescriptor> fdList = dataset.getFileDescriptors();

        // get filestorage
        // FileStorage fileStorage = SpringData.getFileStorage();

        FileDescriptor fd = fdList.get(0);

        // delete fd

        // TODO: need to check whether fd is used by other dataset
        if (fileStorage.deleteFile(fd)) {
            fileDescriptorDao.delete(fd);
        } else {
            // try {
            // t.rollback();
            // } catch (Exception e) {
            // log.error("Can't rollback when deleting dataset id:" + datasetId,
// e);
            // }
            return false;
        }

        // deleta dataset
        datasetDao.delete(dataset);

        // try {
        // t.commit();
        // } catch (Exception e) {
        // log.error("Can't commit when deleting dataset id:" + datasetId, e);
        // return false;
        // }
        return true;
    }

}
