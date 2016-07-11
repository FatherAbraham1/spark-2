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

package com.bosscs.spark.mongodb.extractor;

import java.lang.reflect.InvocationTargetException;

import org.bson.BSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosscs.spark.commons.config.JobConfig;
import com.bosscs.spark.commons.exception.TransformException;
import com.bosscs.spark.mongodb.config.MongoDeepJobConfig;
import com.bosscs.spark.mongodb.utils.UtilMongoDB;

import scala.Tuple2;

/**
 * EntityRDD to interact with mongoDB
 *
 * @param <T> the type parameter
 */
public final class MongoEntityExtractor<T> extends MongoExtractor<T> {

    /**
     * The constant LOG.
     */
    private static final Logger LOG = LoggerFactory.getLogger(MongoEntityExtractor.class);
    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = -3208994171892747470L;

    /**
     * Instantiates a new Mongo entity extractor.
     *
     * @param t the t
     */
    public MongoEntityExtractor(Class<T> t) {
        super();
        this.deepJobConfig = new MongoDeepJobConfig(t);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public T transformElement(Tuple2<Object, BSONObject> tuple, JobConfig<T, ? extends JobConfig> config) {

        try {
            return (T) UtilMongoDB.getObjectFromBson(config.getEntityClass(), tuple._2());
        } catch (Exception e) {
            LOG.error("Cannot convert BSON: ", e);
            throw new TransformException("Could not transform from Bson to Entity " + e.getMessage(), e);
        }

    }

    @Override
    public Tuple2<Object, BSONObject> transformElement(T record) {
        try {
            return new Tuple2<>(null, (BSONObject) UtilMongoDB.getBsonFromObject(record));
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            LOG.error(e.getMessage());
            throw new TransformException(e.getMessage(), e);
        }
    }

}
