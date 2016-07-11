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

package com.bosscs.spark.jdbc.utils;

import com.bosscs.spark.commons.config.JobConfig;
import com.bosscs.spark.commons.entity.Cell;
import com.bosscs.spark.commons.entity.Cells;
import com.bosscs.spark.commons.utils.AnnotationUtils;
import com.bosscs.spark.commons.utils.Utils;
import com.bosscs.spark.jdbc.config.IJdbcDeepJobConfig;
import com.bosscs.spark.jdbc.config.JdbcDeepJobConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Utils for transforming Jdbc row data structures into Cells and Entities.
 */
public class UtilJdbc {

    private static final Logger LOG = LoggerFactory.getLogger(UtilJdbc.class);

    /**
     * Returns a Entity from a Jdbc row represented as a map.
     * @param classEntity Entity.
     * @param row Jdbc row represented as a Map.
     * @param config JDBC Deep Job configuration.
     * @param <T> Entity class.
     * @return Entity from a Jdbc row represented as a map.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static <T, S extends JobConfig> T getObjectFromRow(Class<T> classEntity, Map<String, Object> row, JobConfig<T, S> config) throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T t = classEntity.newInstance();
        Field[] fields = AnnotationUtils.filterDeepFields(classEntity);
        for (Field field : fields) {
            Object currentRow = null;
            Method method = null;
            Class<?> classField = field.getType();
            try {
                method = Utils.findSetter(field.getName(), classEntity, field.getType());

                currentRow = row.get(AnnotationUtils.deepFieldName(field));

                if (currentRow != null) {
                    method.invoke(t, currentRow);
                }
            } catch (IllegalAccessException | InvocationTargetException | IllegalArgumentException e) {
                LOG.error("impossible to create a java object from column:" + field.getName() + " and type:"
                        + field.getType() + " and value:" + t + "; recordReceived:" + currentRow);

                method.invoke(t, Utils.castNumberType(currentRow, classField));
            }
        }
        return t;
    }

    /**
     * Returns a JDBC row data structure from a Deep Entity.
     * @param entity Deep entity.
     * @param <T> Deep entity type.
     * @return JDBC row data structure from a Deep Entity.
     * @throws IllegalAccessException
     * @throws InstantiationException
     * @throws InvocationTargetException
     */
    public static <T> Map<String, Object> getRowFromObject(T entity) throws IllegalAccessException, InstantiationException,
            InvocationTargetException {
        Field[] fields = AnnotationUtils.filterDeepFields(entity.getClass());

        Map<String, Object> row = new HashMap<>();

        for (Field field : fields) {
            Method method = Utils.findGetter(field.getName(), entity.getClass());
            Object object = method.invoke(entity);
            if (object != null) {
                row.put(AnnotationUtils.deepFieldName(field), object);
            }
        }
        return row;
    }

    /**
     * Returns a Cells object from a JDBC row data structure.
     * @param row JDBC row data structure as a Map.
     * @param config JDBC Deep Job config.
     * @return Cells object from a JDBC row data structure.
     */
    public static<T extends JobConfig> Cells getCellsFromObject(Map<String, Object> row, JobConfig<Cells, T> config) {
        Cells result = new Cells(config.getCatalog() + "." + config.getTable());
        for(Map.Entry<String, Object> entry:row.entrySet()) {
            Cell cell = Cell.create(entry.getKey(), entry.getValue());
            result.add(cell);
        }
        return result;
    }

    /**
     * Returns a JDBC row data structure from a Cells object.
     * @param cells Cells object carrying information.
     * @return JDBC row data structure from a Cells object.
     */
    public static Map<String, Object> getObjectFromCells(Cells cells) {
        Map<String, Object> result = new HashMap<>();
        for(Cell cell:cells.getCells()) {
            result.put(cell.getName(), cell.getValue());
        }
        return result;
    }
}
