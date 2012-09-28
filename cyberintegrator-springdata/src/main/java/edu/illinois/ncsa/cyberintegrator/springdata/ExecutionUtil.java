package edu.illinois.ncsa.cyberintegrator.springdata;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.springdata.DatasetUtil;
import edu.illinois.ncsa.springdata.SpringData;
import edu.illinois.ncsa.springdata.Transaction;

public class ExecutionUtil {
    private static Logger log = LoggerFactory.getLogger(ExecutionUtil.class);

    /**
     * Delete executions with related datasets except the list of dataset id
     * given
     * 
     * @param executionId
     *            execution id
     * @param exceptList
     *            list of dataset must not be deleted
     */
    public static boolean deleteExecution(String executionId, List<String> exceptList) {
        log.info("Deleting Execution: " + executionId);
        Transaction t = SpringData.getTransaction();

        // find a exection
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = exedao.findOne(executionId);

        Collection<String> datasetIds = execution.getDatasets().values();

        Iterator<String> iter = datasetIds.iterator();

        while (iter.hasNext()) {
            String id = iter.next();

            if (isExcept(exceptList, id))
                continue;

            // TODO: need to check the dataset is belong to other executions

            // delete dataset
            if (!DatasetUtil.deleteDataset(id)) {
                try {
                    t.rollback();
                } catch (Exception e) {
                    log.error("Can't rollback when deleting execution id:" + executionId, e);
                }
                return false;
            }
        }

        // delete execution
        exedao.delete(executionId);
        try {
            t.commit();
        } catch (Exception e) {
            log.error("Can't commit when deleting execution id:" + executionId, e);
        }
        return true;
    }

    private static boolean isExcept(List<String> exceptList, String id) {
        for (String e : exceptList) {
            if (id.matches(e))
                return true;
        }
        return false;
    }
}
