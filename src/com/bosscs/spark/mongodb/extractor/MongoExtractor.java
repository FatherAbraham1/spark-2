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

import java.io.Serializable;

import org.bson.BSONObject;

import com.mongodb.hadoop.MongoInputFormat;
import com.mongodb.hadoop.MongoOutputFormat;
import com.bosscs.spark.commons.extractor.impl.GenericHadoopExtractor;
import com.bosscs.spark.mongodb.config.MongoDeepJobConfig;

/**
 * Created by Jerry Xiong on 27/12/15.
 *
 * @param <T> the type parameter
 */
public abstract class MongoExtractor<T> extends GenericHadoopExtractor<T, MongoDeepJobConfig<T>, Object,
        BSONObject, Object, BSONObject>
        implements Serializable {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 298122755783328212L;

    /**
     * Instantiates a new Mongo extractor.
     */
    public MongoExtractor() {
        super();
        this.inputFormat = new MongoInputFormat();
        this.outputFormat = new MongoOutputFormat();
    }

}
