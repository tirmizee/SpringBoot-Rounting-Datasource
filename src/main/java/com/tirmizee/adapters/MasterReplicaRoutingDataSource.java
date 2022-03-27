package com.tirmizee.adapters;

import static  com.tirmizee.constants.DatasourceType.*;

import com.tirmizee.constants.DatasourceType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public class MasterReplicaRoutingDataSource extends AbstractRoutingDataSource {

    public static final Logger LOGGER = LoggerFactory.getLogger(MasterReplicaRoutingDataSource.class);

    @Override
    protected Object determineCurrentLookupKey() {
        DatasourceType datasourceType = TransactionSynchronizationManager.isCurrentTransactionReadOnly() ? READ_ONLY : READ_WRITE;
        LOGGER.info("current datasource : {} -> {} ", datasourceType.name());
        return datasourceType;
    }

}
