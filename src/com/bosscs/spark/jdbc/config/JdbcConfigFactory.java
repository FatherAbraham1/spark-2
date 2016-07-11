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
package com.bosscs.spark.jdbc.config;

import com.bosscs.spark.commons.entity.Cells;
import com.bosscs.spark.commons.entity.IType;

/**
 * Class with factory method for instatiating JdbcDeepJobConfig objects.
 */
public final class JdbcConfigFactory {

    private JdbcConfigFactory() {
        throw new UnsupportedOperationException();
    }

    /**
     * Creates a new cell-based Jdbc job configuration object.
     *
     * @return a new cell-based Jdbc job configuration object.
     */
    public static JdbcDeepJobConfig<Cells> createJdbc() {
        return new JdbcDeepJobConfig(Cells.class);
    }

    /**
     * Creates a new entity-based Jdbc job configuration object.
     *
     * @param entityClass the class instance of the entity class that will be used to map db objects to Java objects.
     * @param <T>         the generic type of the entity object implementing IDeepType.
     * @return a new entity-based Jdbc job configuration object.
     */
    public static <T extends IType> JdbcDeepJobConfig<T> createJdbc(Class<T> entityClass) {
        return new JdbcDeepJobConfig<>(entityClass);
    }

}
