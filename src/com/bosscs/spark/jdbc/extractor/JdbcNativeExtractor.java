/*
 * Copyright 2016, Jerry Xiong, BOSSCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.bosscs.spark.jdbc.extractor;

import static com.bosscs.spark.commons.utils.Utils.initConfig;

import com.bosscs.spark.commons.config.BaseConfig;
import com.bosscs.spark.commons.exception.GenericException;
import com.bosscs.spark.commons.querybuilder.UpdateQueryBuilder;
import com.bosscs.spark.commons.rdd.IExtractor;
import com.bosscs.spark.jdbc.config.JdbcDeepJobConfig;
import com.bosscs.spark.jdbc.reader.IJdbcReader;
import com.bosscs.spark.jdbc.reader.JdbcReader;
import com.bosscs.spark.jdbc.writer.JdbcWriter;
import org.apache.spark.Partition;
import org.apache.spark.rdd.JdbcPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * Abstract class of Jdbc native extractor.
 */
public abstract class JdbcNativeExtractor<T, S extends BaseConfig> implements IExtractor<T, S> {

    private static final long serialVersionUID = -298383130965427783L;

    private static final Logger LOG = LoggerFactory.getLogger(JdbcNativeExtractor.class);

    /**
     * Jdbc Deep Job configuration.
     */
    protected JdbcDeepJobConfig<T> jdbcDeepJobConfig;

    /**
     * Jdbc reader
     */
    protected IJdbcReader jdbcReader;

    /**
     * Jdbc writer
     */
    protected JdbcWriter<T> jdbcWriter;

    /**
     * {@inheritDoc}
     */
    @Override
    public Partition[] getPartitions(S config) {
        jdbcDeepJobConfig = initConfig(config, jdbcDeepJobConfig);

        int upperBound = jdbcDeepJobConfig.getUpperBound();
        int lowerBound = jdbcDeepJobConfig.getLowerBound();
        int numPartitions = jdbcDeepJobConfig.getNumPartitions();
        int length = 1 + upperBound - lowerBound;
        Partition [] result = new Partition[numPartitions];
        for(int i=0; i<numPartitions; i++) {
            int start = lowerBound + lowerBound + ((i * length) / numPartitions);
            int end = lowerBound + (((i + 1) * length) / numPartitions) - 1;
            result[i] = new JdbcPartition(i, start, end);
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasNext() {
        try {
            return jdbcReader.hasNext();
        } catch (SQLException e) {
            throw new GenericException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T next() {
        try {
            return transformElement(jdbcReader.next());
        } catch (SQLException e) {
            throw new GenericException(e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        if(jdbcReader != null) {
            try {
                jdbcReader.close();
            } catch(Exception e) {
                LOG.error("Unable to close jdbcReader", e);
            }
        }
        if(jdbcWriter != null) {
            try {
                jdbcWriter.close();
            } catch(Exception e) {
                LOG.error("Unable to close jdbcWriter", e);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initIterator(Partition dp, S config) {
        jdbcDeepJobConfig = initConfig(config, jdbcDeepJobConfig);
        this.jdbcReader = new JdbcReader(jdbcDeepJobConfig);
        try {
            this.jdbcReader.init(dp);
        } catch(Exception e) {
            throw new GenericException("Unable to initialize JdbcReader", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void saveRDD(T t) {
        try {
            this.jdbcWriter.save(transformElement(t));
        } catch(Exception e) {
            throw new GenericException("Error while writing row", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getPreferredLocations(Partition split) {
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void initSave(S config, T first, UpdateQueryBuilder queryBuilder) {

        jdbcDeepJobConfig = initConfig(config, jdbcDeepJobConfig);

        try {
            this.jdbcWriter = new JdbcWriter<>(jdbcDeepJobConfig);
        } catch(Exception e) {
            throw new GenericException(e);
        }
    }


    protected abstract T transformElement(Map<String, Object> entity);

    protected abstract Map<String, Object> transformElement(T entity);
}
