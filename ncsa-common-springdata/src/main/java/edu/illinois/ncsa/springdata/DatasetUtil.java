package edu.illinois.ncsa.springdata;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.domain.Dataset;
import edu.illinois.ncsa.domain.FileDescriptor;

public class DatasetUtil {
    private static Logger log = LoggerFactory.getLogger(DatasetUtil.class);

    // TODO: assuming that there is only 1 file for dataset
    public static boolean deleteDataset(String datasetId) {
        log.info("delete dataset by dataset id: " + datasetId);

        Transaction t = SpringData.getTransaction();

        // find dataset
        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);
        Dataset dataset = datasetDao.findOne(datasetId);

        // get all fd from the dataset
        List<FileDescriptor> fdList = dataset.getFileDescriptors();

        // get filestorage
        FileDescriptorDAO fdDao = SpringData.getBean(FileDescriptorDAO.class);
        FileStorage fileStorage = SpringData.getFileStorage();

        FileDescriptor fd = fdList.get(0);

        // delete fd

        // TODO: need to check whether fd is used by other dataset
        if (fileStorage.deleteFile(fd)) {
            fdDao.delete(fd);
        } else {
            try {
                t.rollback();
            } catch (Exception e) {
                log.error("Can't rollback when deleting dataset id:" + datasetId, e);
            }
            return false;
        }

        // deleta dataset
        datasetDao.delete(datasetId);

        try {
            t.commit();
        } catch (Exception e) {
            log.error("Can't commit when deleting dataset id:" + datasetId, e);
            return false;
        }
        return true;
    }

}
