/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.dtstack.flinkx.connector.kingbase.sink;

import org.apache.flink.table.types.logical.RowType;

import com.dtstack.flinkx.connector.jdbc.sink.JdbcOutputFormat;
import com.dtstack.flinkx.connector.kingbase.converter.KingbaseRawTypeConverter;
import com.dtstack.flinkx.connector.kingbase.util.KingbaseUtils;
import com.dtstack.flinkx.util.TableUtil;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

/**
 * @description:
 * @program: flinkx-all
 * @author: lany
 * @create: 2021/05/13 20:10
 */
public class KingbaseOutputFormat extends JdbcOutputFormat {

    protected static final long serialVersionUID = 2L;


    @Override
    protected void openInternal(int taskNumber, int numTasks) {
        super.openInternal(taskNumber, numTasks);
        // create row converter
        RowType rowType =
                TableUtil.createRowType(columnNameList, columnTypeList, KingbaseRawTypeConverter::apply);
        setRowConverter(rowConverter ==null ? jdbcDialect.getColumnConverter(rowType) : rowConverter);
    }

    /**
     * override reason: The Kingbase meta-database is case sensitive。
     * If you use a lowercase table name, it will not be able to query the table metadata.
     * so we convert the table and schema name to uppercase.
     */
    @Override
    protected Pair<List<String>, List<String>> getTableMetaData() {
        return KingbaseUtils.getTableMetaData(jdbcConf.getSchema(),
                jdbcConf.getTable(),
                dbConn);
    }
}
