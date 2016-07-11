package com.bosscs.spark.commons.rdd;

import java.io.Serializable;
import java.util.List;

import org.apache.spark.Partition;

import com.bosscs.spark.commons.config.BaseConfig;
import com.bosscs.spark.commons.querybuilder.UpdateQueryBuilder;

/**
 * Created by Jerry Xiong on 4/12/15.
 *
 * @param <T> the type parameter
 * @param <S> the type parameter
 */
public interface IExtractor<T, S extends BaseConfig> extends Serializable, AutoCloseable {

    /**
     * Gets partitions.
     *
     * @param config the config
     * @return the partition [ ]
     */
    Partition[] getPartitions(S config);

    /**
     * Has next.
     *
     * @return the boolean
     */
    boolean hasNext();

    /**
     * Next t.
     *
     * @return the t
     */
    T next();

    /**
     * Close Reader and Writer.
     */
    void close();

    /**
     * Init iterator.
     *
     * @param dp     the dp
     * @param config the config
     */
    void initIterator(Partition dp, S config);

    /**
     * Save RDD.
     *
     * @param t the t
     */
    void saveRDD(T t);

    /**
     * Gets preferred locations.
     *
     * @param split the split
     * @return the preferred locations
     */
    List<String> getPreferredLocations(Partition split);

    /**
     * Init save.
     *
     * @param config       the config
     * @param first        the first
     * @param queryBuilder the query builder
     */
    void initSave(S config, T first, UpdateQueryBuilder queryBuilder);

}
