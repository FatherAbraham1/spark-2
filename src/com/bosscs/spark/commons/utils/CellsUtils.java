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

package com.bosscs.spark.commons.utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;


import org.apache.spark.sql.api.java.*;
import org.apache.spark.sql.*;

import org.apache.spark.sql.types.*;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bosscs.spark.commons.entity.Cell;
import com.bosscs.spark.commons.entity.Cells;
import com.bosscs.spark.commons.entity.IType;


/**
 * Created by Jerry Xiong on 20/02/16.
 */
public class CellsUtils {

    private static final Logger LOG = LoggerFactory.getLogger(CellsUtils.class);


    /**
     * converts from cell class to JSONObject
     *
     * @param cells the cells
     * @return json from cell
     * @throws IllegalAccessException the illegal access exception
     * @throws IllegalAccessException the instantiation exception
     * @throws IllegalAccessException the invocation target exception
     */
    public static JSONObject getJsonFromCell(Cells cells)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {

        JSONObject json = new JSONObject();
        for (Cell cell : cells) {
            if (cell.getCellValue() != null) {
                if (Collection.class.isAssignableFrom(cell.getCellValue().getClass())) {
                    Collection c = (Collection) cell.getCellValue();
                    Iterator iterator = c.iterator();
                    List innerJsonList = new ArrayList<>();

                    while (iterator.hasNext()) {
                        Object innerObject = iterator.next();
                        if(innerObject instanceof Cells){
                            innerJsonList.add(getJsonFromCell((Cells)innerObject ));
                        }else{
                            innerJsonList.add(innerObject);
                        }

                    }
                    json.put(cell.getCellName(), innerJsonList);
                } else if (Cells.class.isAssignableFrom(cell.getCellValue().getClass())) {
                    json.put(cell.getCellName(), getJsonFromCell((Cells) cell.getCellValue()));
                } else {
                    json.put(cell.getCellName(), cell.getCellValue());
                }

            }
        }

        return json;
    }

    /**
     * converts from cell class to JSONObject
     *
     * @param Json      the json
     * @param tableName the table name
     * @return cell from json
     * @throws IllegalAccessException the illegal access exception
     * @throws IllegalAccessException the instantiation exception
     * @throws IllegalAccessException the invocation target exception
     */
    public static Cells getCellFromJson(JSONObject Json, String tableName)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {

        Cells cells = tableName != null ? new Cells(tableName) : new Cells();

        Set<String> entrySet = Json.keySet();

        for (String key : entrySet) {
            try {
                Object value = Json.get(key);

                if (List.class.isAssignableFrom(value.getClass())) {
                    List<Cells> innerCell = new ArrayList<>();
                    for (JSONObject innerBson : (List<JSONObject>) value) {
                        innerCell.add(getCellFromJson(innerBson, null));
                    }
                    cells.add(Cell.create(key, innerCell));
                } else if (JSONObject.class.isAssignableFrom(value.getClass())) {
                    Cells innerCells = getCellFromJson((JSONObject) value, null);
                    cells.add(Cell.create(key, innerCells));
                } else {
                    if (key.equalsIgnoreCase("id")) {
                        cells.add(Cell.create(key, value, true));
                    } else {
                        cells.add(Cell.create(key, value));

                    }
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | IllegalArgumentException e) {
                                LOG.error("impossible to create a java cell from Json field:"+key);
            }

        }
        return cells;
    }

    public static Cells getCellWithMapFromJson(JSONObject Json, String tableName)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {

        Cells cells = tableName != null ? new Cells(tableName) : new Cells();

        Set<String> entrySet = Json.keySet();

        for (String key : entrySet) {
            try {
                Object value = Json.get(key);

                if (List.class.isAssignableFrom(value.getClass())) {
                    List<String> innerCell = new ArrayList<>();
                    for (String innerBson : (List<String>) value) {
                        innerCell.add(innerBson);
                    }
                    cells.add(Cell.create(key, innerCell));
                } else if (JSONObject.class.isAssignableFrom(value.getClass())) {
                    Map<String, Object> map = new HashMap<>();
                    Cells innerCells = getCellFromJson((JSONObject) value, null);
                    for (Cell cell : innerCells) {
                        map.put(cell.getName(), cell.getValue());
                    }
                    cells.add(Cell.create(key, map));
                } else {
                    if (key.equalsIgnoreCase("id")) {
                        cells.add(Cell.create(key, value, true));
                    } else {
                        cells.add(Cell.create(key, value));

                    }
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | IllegalArgumentException e) {
                                LOG.error("impossible to create a java cell from Json field:"+key);
            }

        }
        return cells;
    }

    /**
     * Gets object from json.
     *
     * @param classEntity the class entity
     * @param bsonObject  the bson object
     * @return the object from json
     * @throws IllegalAccessException    the illegal access exception
     * @throws InstantiationException    the instantiation exception
     * @throws InvocationTargetException the invocation target exception
     */
    public static <T> T getObjectFromJson(Class<T> classEntity, JSONObject bsonObject)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T t = classEntity.newInstance();

        Field[] fields = AnnotationUtils.filterDeepFields(classEntity);

        Object insert = null;

        for (Field field : fields) {
            Object currentBson = null;
            Method method = null;
            try {
                method = Utils.findSetter(field.getName(), classEntity, field.getType());

                Class<?> classField = field.getType();

                currentBson = bsonObject.get(AnnotationUtils.deepFieldName(field));
                if (currentBson != null) {

                    if (Iterable.class.isAssignableFrom(classField)) {
                        Type type = field.getGenericType();

                        insert = subDocumentListCase(type, (List) bsonObject.get(AnnotationUtils.deepFieldName(field)));

                    } else if (IType.class.isAssignableFrom(classField)) {
                        insert = getObjectFromJson(classField, (JSONObject) bsonObject.get(AnnotationUtils.deepFieldName
                                (field)));
                    } else {
                        insert = currentBson;
                    }
                    method.invoke(t, insert);
                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | IllegalArgumentException e) {
                                LOG.error("impossible to create a java object from Bson field:" + field.getName() + " and type:" + field
                                        .getType() + " and value:" + t + "; bsonReceived:" + currentBson + ", bsonClassReceived:"
                                        + currentBson.getClass());

                method.invoke(t, Utils.castNumberType(insert, t.getClass()));
            }

        }

        return t;
    }

    public static <T> T getObjectWithMapFromJson(Class<T> classEntity, JSONObject bsonObject)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        T t = classEntity.newInstance();

        Field[] fields = AnnotationUtils.filterDeepFields(classEntity);

        Object insert = null;

        for (Field field : fields) {
            Object currentBson = null;
            Method method = null;
            try {
                method = Utils.findSetter(field.getName(), classEntity, field.getType());

                Class<?> classField = field.getType();

                currentBson = bsonObject.get(AnnotationUtils.deepFieldName(field));
                if (currentBson != null) {

                    if (Collection.class.isAssignableFrom(classField)) {
                        Type type = field.getGenericType();
                        List list = new ArrayList();
                        for (Object o : (List) bsonObject.get(AnnotationUtils.deepFieldName(field))) {
                            list.add((String) o);
                        }
                        insert = list;

                    } else if (IType.class.isAssignableFrom(classField)) {
                        insert = getObjectFromJson(classField, (JSONObject) bsonObject.get(AnnotationUtils.deepFieldName
                                (field)));
                    } else {
                        insert = currentBson;
                    }
                    if (insert != null) {
                        method.invoke(t, insert);
                    }

                }
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException | IllegalArgumentException e) {
                                LOG.error("impossible to create a java object from Bson field:" + field.getName() + " and type:" + field
                                        .getType() + " and value:" + t + "; bsonReceived:" + currentBson + ", bsonClassReceived:"
                                        + currentBson.getClass());
                                method.invoke(t, Utils.castNumberType(insert, t.getClass()));
            }

        }

        return t;
    }

    /**
     * Creates a SparkSQL Row object from a Stratio Cells object
     *
     * @param cells Stratio Cells object for transforming.
     * @return SparkSQL Row created from Cells.
     */
    public static Row getRowFromCells(Cells cells) {
        Object[] values = cells.getCellValues().toArray();
        return RowFactory.create(values);
    }

    /**
     * Returns a Collection of SparkSQL Row objects from a collection of Stratio Cells
     * objects
     *
     * @param cellsCol Collection of Cells for transforming
     * @return Collection of SparkSQL Row created from Cells.
     */
    public static Collection<Row> getRowsFromsCells(Collection<Cells> cellsCol) {
        Collection<Row> result = new ArrayList<>();
        for (Cells cells : cellsCol) {
            result.add(getRowFromCells(cells));
        }
        return result;
    }

    public static StructType getStructTypeFromCells(Cells cells) {
        List<StructField> fields = new ArrayList<>();
        for(Cell cell:cells.getCells()) {
            StructField field = getStructFieldFromCell(cell);
            fields.add(field);
        }
        return new StructType(fields.toArray(new StructField[0]));
    }

    private static StructField getStructFieldFromCell(Cell cell) {
        Metadata metadata = null;
        StructField field = new StructField(cell.getName(), getDataType(cell.getValue()), false,metadata);
        return field;
    }

    private static DataType getDataType(Object value) {
        Class cls = value.getClass();
        DataType dataType;
        if(cls.equals(String.class)) {
            dataType = DataTypes.StringType;
        } else if(cls.equals(Byte[].class)) {
            dataType = DataTypes.BinaryType;
        } else if(cls.equals(Boolean.class)) {
            dataType = DataTypes.BooleanType;
        } else if(cls.equals(Timestamp.class)) {
            dataType = DataTypes.TimestampType;
        } else if(cls.equals(Double.class)) {
            dataType = DataTypes.DoubleType;
        } else if(cls.equals(Float.class)) {
            dataType = DataTypes.FloatType;
        } else if(cls.equals(Byte.class)) {
            dataType = DataTypes.ByteType;
        } else if(cls.equals(Integer.class)) {
            dataType = DataTypes.IntegerType;
        } else if(cls.equals(Long.class)) {
            dataType = DataTypes.LongType;
        } else if(cls.equals(Short.class)) {
            dataType = DataTypes.ShortType;
        } else if(value instanceof List) {
            List listValue = (List)value;
            if(listValue.isEmpty()) {
                dataType = DataTypes.createArrayType(DataTypes.StringType);
            } else {
                dataType = DataTypes.createArrayType(getDataType(listValue.get(0)));
            }
        } else if(value instanceof Map) {
            Map mapValue = (Map)value;
            if(mapValue.isEmpty()) {
                dataType = DataTypes.createMapType(DataTypes.StringType, DataTypes.StringType);
            } else {
                Map.Entry entry = (Map.Entry) mapValue.entrySet().iterator().next();
                dataType = DataTypes.createMapType(getDataType(entry.getKey()), getDataType(entry.getValue()));
            }
        } else {
            dataType = DataTypes.StringType;
        }
        return dataType;
    }

    /**
     * Sub document list case.
     *
     * @param <T>        the type parameter
     * @param type       the type
     * @param jsonObject the json object
     * @return the object
     * @throws IllegalAccessException    the illegal access exception
     * @throws InstantiationException    the instantiation exception
     * @throws InvocationTargetException the invocation target exception
     */
    private static <T> Object subDocumentListCase(Type type, List<T> jsonObject)
            throws IllegalAccessException, InstantiationException, InvocationTargetException {
        ParameterizedType listType = (ParameterizedType) type;

        Class<?> listClass = (Class<?>) listType.getActualTypeArguments()[0];

        List list = new ArrayList();
        for (T t : jsonObject) {
            list.add(getObjectFromJson(listClass, (JSONObject) t));
        }

        return list;
    }
}
