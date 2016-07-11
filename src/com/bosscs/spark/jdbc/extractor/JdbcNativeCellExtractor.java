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

import com.bosscs.spark.commons.entity.Cells;
import com.bosscs.spark.jdbc.config.JdbcDeepJobConfig;
import com.bosscs.spark.jdbc.utils.UtilJdbc;

import java.util.Map;

/**
 * Implementation of JdbcExtractor for Cells objects.
 */
public class JdbcNativeCellExtractor extends JdbcNativeExtractor<Cells, JdbcDeepJobConfig<Cells>> {

    private static final long serialVersionUID = 5796562363902015583L;

    /**
     * Default constructor.
     */
    public JdbcNativeCellExtractor() {
        this.jdbcDeepJobConfig = new JdbcDeepJobConfig<>(Cells.class);
    }

    public JdbcNativeCellExtractor(Class<Cells> cellsClass) {
        this.jdbcDeepJobConfig = new JdbcDeepJobConfig<>(Cells.class);
    }

    /**
     * Transforms a database row represented as a Map into a Cells object.
     * @param entity Database row represented as a Map of column name:column value.
     * @return Cells object with database row data.
     */
    @Override
    protected Cells transformElement(Map<String, Object> entity) {
        return UtilJdbc.getCellsFromObject(entity, jdbcDeepJobConfig);
    }

    /**
     * Transforms a Cells object into a database row represented as a Map.
     * @param cells Cells data object.
     * @return Database row represented as a Map of column name:column value.
     */
    @Override
    protected Map<String, Object> transformElement(Cells cells) {
        return UtilJdbc.getObjectFromCells(cells);
    }

}
