/*
 * Copyright 2016, Jerry Xiong, BOSSCS
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.bosscs.spark.mongodb.extractor;

import com.mongodb.DBObject;
import com.bosscs.spark.commons.entity.Cells;
import com.bosscs.spark.mongodb.config.MongoDeepJobConfig;

/**
 * Created by Jerry Xiong on 12/02/16.
 */
public class MongoNativeDBObjectExtractor extends MongoNativeExtractor<DBObject, MongoDeepJobConfig<DBObject>> {

    /**
     * The constant serialVersionUID.
     */
    private static final long serialVersionUID = 4437817175333677270L;


    public MongoNativeDBObjectExtractor(Class<DBObject> dBObject) {
        this.mongoDeepJobConfig = new MongoDeepJobConfig<>(dBObject);
    }
    /**
     * Instantiates a new Mongo native dB object extractor.
     */
    public MongoNativeDBObjectExtractor() {
        this.mongoDeepJobConfig = new MongoDeepJobConfig<>(DBObject.class);
    }

    @Override
    protected DBObject transformElement(DBObject entity) {
        return entity;
    }
}
