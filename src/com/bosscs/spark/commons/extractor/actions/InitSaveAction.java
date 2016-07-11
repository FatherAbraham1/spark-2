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

/**
 *
 */
package com.bosscs.spark.commons.extractor.actions;

import com.bosscs.spark.commons.config.ExtractorConfig;
import com.bosscs.spark.commons.querybuilder.UpdateQueryBuilder;

/**
 * Created by Jerry Xiong on 27/12/15.
 */
public class InitSaveAction<T> extends Action {

    private static final long serialVersionUID = -1270097974102584045L;

    private ExtractorConfig<T> config;

    private T first;

    private UpdateQueryBuilder queryBuilder;

    public InitSaveAction() {
        super();
    }

    public InitSaveAction(ExtractorConfig<T> config, T first, UpdateQueryBuilder queryBuilder) {
        super(ActionType.INIT_SAVE);
        this.config = config;
        this.first = first;
        this.queryBuilder = queryBuilder;
    }

    public ExtractorConfig<T> getConfig() {
        return config;
    }

    public T getFirst() {
        return first;
    }

    public UpdateQueryBuilder getQueryBuilder() {
        return queryBuilder;
    }

}
