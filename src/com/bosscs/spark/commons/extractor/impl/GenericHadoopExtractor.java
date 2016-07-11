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

package com.bosscs.spark.commons.extractor.impl;

import static com.bosscs.spark.commons.utils.Utils.initConfig;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.JobContext;
import org.apache.hadoop.mapreduce.JobID;
import org.apache.hadoop.mapreduce.OutputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.RecordWriter;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.log4j.Logger;
import org.apache.spark.Partition;
import org.apache.spark.rdd.NewHadoopPartition;

import com.bosscs.spark.commons.config.BaseConfig;
import com.bosscs.spark.commons.config.JobConfig;
import com.bosscs.spark.commons.config.ExtractorConfig;
import com.bosscs.spark.commons.config.HadoopConfig;
import com.bosscs.spark.commons.exception.GenericException;
import com.bosscs.spark.commons.querybuilder.UpdateQueryBuilder;
import com.bosscs.spark.commons.rdd.IExtractor;
import com.bosscs.spark.commons.utils.SparkHadoopMapReduceUtil;

import scala.Tuple2;

/**
 * Created by Jerry Xiong on 26/12/15.
 */
public abstract class GenericHadoopExtractor<T, S extends BaseConfig, K, V, kOut, vOut> implements IExtractor<T, S> {

    protected HadoopConfig deepJobConfig;

    protected transient RecordReader<K, V> reader;

    protected transient RecordWriter<kOut, vOut> writer;

    protected transient InputFormat<K, V> inputFormat;

    protected transient OutputFormat<kOut, vOut> outputFormat;

    protected transient String jobTrackerId;

    protected transient TaskAttemptContext hadoopAttemptContext;

    protected boolean havePair = false;

    protected boolean finished = false;

    protected transient JobID jobId = null;

    private static final Logger LOG = Logger.getLogger(GenericHadoopExtractor.class);

    {
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMddHHmm");
        jobTrackerId = formatter.format(new Date());

    }

    @Override
    public List<String> getPreferredLocations(Partition split) {
        return null;
    }

    @Override
    public Partition[] getPartitions(S config) {


        int id = config.getRddId();

        jobId = new JobID(jobTrackerId, id);



        Configuration conf = getHadoopConfig(config);

        JobContext jobContext = SparkHadoopMapReduceUtil.newJobContext(conf, jobId);

        try {
            List<InputSplit> splits = inputFormat.getSplits(jobContext);

            Partition[] partitions = new Partition[(splits.size())];
            for (int i = 0; i < splits.size(); i++) {
                partitions[i] = new NewHadoopPartition(id, i, splits.get(i));
            }

            return partitions;

        } catch (IOException | InterruptedException | RuntimeException e) {
            LOG.error("Impossible to calculate partitions " + e.getMessage());
            throw new GenericException("Impossible to calculate partitions ", e);
        }

    }

    @Override
    public boolean hasNext() {
        if (!finished && !havePair) {
            try {
                finished = !reader.nextKeyValue();
            } catch (IOException | InterruptedException e) {
                LOG.error("Impossible to get hasNext " + e.getMessage());
                throw new GenericException("Impossible to get hasNext ", e);
            }
            havePair = !finished;

        }
        return !finished;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new java.util.NoSuchElementException("End of stream");
        }
        havePair = false;

        Tuple2<K, V> tuple = null;
        try {
            return (T) transformElement(new Tuple2<>(reader.getCurrentKey(), reader.getCurrentValue()),
                    deepJobConfig);
        } catch (IOException | InterruptedException e) {
            LOG.error("Impossible to get next value " + e.getMessage());
            throw new GenericException("Impossible to get next value ", e);
        }
    }

    @Override
    public void close() {
        try {
            if (reader != null) {
                reader.close();
            }
            if (writer != null) {
                writer.close(hadoopAttemptContext);
            }
        } catch (IOException | InterruptedException e) {
            LOG.error("Impossible to close RecordReader " + e.getMessage());
            throw new GenericException("Impossible to close RecordReader ", e);
        }
    }

    private Configuration getHadoopConfig(S config) {

        deepJobConfig = initConfig(config, deepJobConfig);

        return deepJobConfig.getHadoopConfiguration();
    }

    public abstract T transformElement(Tuple2<K, V> tuple, JobConfig<T, ? extends JobConfig> config);

    @Override
    public void saveRDD(T t) {
        Tuple2<kOut, vOut> tuple = transformElement(t);
        try {
            writer.write(tuple._1(), tuple._2());

        } catch (IOException | InterruptedException e) {
            LOG.error("Impossible to saveRDD " + e.getMessage());
            throw new GenericException("Impossible to saveRDD ", e);
        }
        return;
    }

    @Override
    public void initSave(S config, T first, UpdateQueryBuilder queryBuilder) {
        int id = config.getRddId();

        int partitionIndex = config.getPartitionId();

        TaskAttemptID attemptId = SparkHadoopMapReduceUtil
                .newTaskAttemptID(jobTrackerId, id, true, partitionIndex, 0);

        Configuration configuration = getHadoopConfig(config);


        hadoopAttemptContext = SparkHadoopMapReduceUtil
                .newTaskAttemptContext(configuration,
                        attemptId);
        try {
            writer = outputFormat.getRecordWriter(hadoopAttemptContext);
        } catch (IOException | InterruptedException e) {
            throw new GenericException(e);
        }
    }

    @Override
    public void initIterator(Partition dp, S config) {

        int id = config.getRddId();

        NewHadoopPartition split = (NewHadoopPartition) dp;

        TaskAttemptID attemptId = SparkHadoopMapReduceUtil
                .newTaskAttemptID(jobTrackerId, id, true, split.index(), 0);

        Configuration configuration = getHadoopConfig(config);

        TaskAttemptContext hadoopAttemptContext = SparkHadoopMapReduceUtil
                .newTaskAttemptContext(configuration, attemptId);

        try {
            reader = inputFormat.createRecordReader(split.serializableHadoopSplit().value(), hadoopAttemptContext);
            reader.initialize(split.serializableHadoopSplit().value(), hadoopAttemptContext);
        } catch (IOException | InterruptedException e) {
            throw new GenericException(e);
        }
    }

    public abstract Tuple2<kOut, vOut> transformElement(T record);
}
