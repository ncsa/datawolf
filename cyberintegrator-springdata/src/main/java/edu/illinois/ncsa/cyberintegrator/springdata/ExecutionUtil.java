package edu.illinois.ncsa.cyberintegrator.springdata;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import edu.illinois.ncsa.cyberintegrator.domain.Execution;
import edu.illinois.ncsa.springdata.DatasetDAO;
import edu.illinois.ncsa.springdata.DatasetUtil;
import edu.illinois.ncsa.springdata.SpringData;

public class ExecutionUtil {
    public static void deleteExecution(String executionId, List<String> exceptList) {
        // find a exection
        ExecutionDAO exedao = SpringData.getBean(ExecutionDAO.class);
        Execution execution = exedao.findOne(executionId);

        Collection<String> datasetIds = execution.getDatasets().values();

        // delete execution
        exedao.delete(executionId);

        DatasetDAO datasetDao = SpringData.getBean(DatasetDAO.class);

        Iterator<String> iter = datasetIds.iterator();

        while (iter.hasNext()) {
            String id = iter.next();

            if (isExcept(exceptList, id))
                continue;

            // TODO: need to check the dataset is belong to other executions

            // delete dataset
            DatasetUtil.deleteDataset(id);
        }
    }

    private static boolean isExcept(List<String> exceptList, String id) {
        for (String e : exceptList) {
            if (id.matches(e))
                return true;
        }
        return false;
    }
}
