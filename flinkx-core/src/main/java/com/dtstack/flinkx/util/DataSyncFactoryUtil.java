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

package com.dtstack.flinkx.util;

import com.dtstack.flinkx.classloader.ClassLoaderManager;
import com.dtstack.flinkx.conf.FlinkxCommonConf;
import com.dtstack.flinkx.conf.MetricParam;
import com.dtstack.flinkx.conf.SyncConf;
import com.dtstack.flinkx.enums.OperatorType;
import com.dtstack.flinkx.metrics.CustomReporter;
import com.dtstack.flinkx.sink.SinkFactory;
import com.dtstack.flinkx.source.SourceFactory;
import com.dtstack.flinkx.throwable.FlinkxRuntimeException;

import org.apache.flink.api.common.functions.RuntimeContext;
import org.apache.flink.configuration.ConfigUtils;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Set;

/**
 * @program: flinkx
 * @author: wuren
 * @create: 2021/04/27
 */
public class DataSyncFactoryUtil {

    public static SourceFactory discoverSource(SyncConf config, StreamExecutionEnvironment env) {
        try {
            String pluginName = config.getJob().getReader().getName();
            String pluginClassName = PluginUtil.getPluginClassName(pluginName, OperatorType.source);
            Set<URL> urlList =
                    PluginUtil.getJarFileDirPath(pluginName, config.getPluginRoot(), null);
            urlList.addAll(
                    PluginUtil.getJarFileDirPath(
                            PluginUtil.FORMATS_SUFFIX, config.getPluginRoot(), null));
            ConfigUtils.encodeCollectionToConfig(
                    (Configuration)
                            ReflectionUtils.getDeclaredMethod(env, "getConfiguration").invoke(env),
                    PipelineOptions.JARS,
                    urlList,
                    URL::toString);
            ConfigUtils.encodeCollectionToConfig(
                    (Configuration)
                            ReflectionUtils.getDeclaredMethod(env, "getConfiguration").invoke(env),
                    PipelineOptions.CLASSPATHS,
                    urlList,
                    URL::toString);

            return ClassLoaderManager.newInstance(
                    urlList,
                    cl -> {
                        Class<?> clazz = cl.loadClass(pluginClassName);
                        Constructor<?> constructor =
                                clazz.getConstructor(
                                        SyncConf.class, StreamExecutionEnvironment.class);
                        return (SourceFactory) constructor.newInstance(config, env);
                    });
        } catch (Exception e) {
            throw new FlinkxRuntimeException(e);
        }
    }

    public static CustomReporter discoverMetric(
            FlinkxCommonConf flinkxCommonConf,
            RuntimeContext context,
            boolean makeTaskFailedWhenReportFailed) {
        try {
            String pluginName = flinkxCommonConf.getMetricPluginName();
            String pluginClassName = PluginUtil.getPluginClassName(pluginName, OperatorType.metric);
            Set<URL> urlList =
                    PluginUtil.getJarFileDirPath(
                            pluginName, flinkxCommonConf.getMetricPluginRoot(), null);
            MetricParam metricParam =
                    new MetricParam(
                            context,
                            makeTaskFailedWhenReportFailed,
                            flinkxCommonConf.getMetricProps());
            return ClassLoaderManager.newInstance(
                    urlList,
                    cl -> {
                        Class<?> clazz = cl.loadClass(pluginClassName);
                        Constructor constructor = clazz.getConstructor(MetricParam.class);
                        return (CustomReporter) constructor.newInstance(metricParam);
                    });
        } catch (Exception e) {
            throw new FlinkxRuntimeException(e);
        }
    }

    public static SinkFactory discoverSink(SyncConf config) {
        try {
            String pluginName = config.getJob().getContent().get(0).getWriter().getName();
            String pluginClassName = PluginUtil.getPluginClassName(pluginName, OperatorType.sink);
            Set<URL> urlList =
                    PluginUtil.getJarFileDirPath(pluginName, config.getPluginRoot(), null);
            urlList.addAll(
                    PluginUtil.getJarFileDirPath(
                            PluginUtil.FORMATS_SUFFIX, config.getPluginRoot(), null));

            return ClassLoaderManager.newInstance(
                    urlList,
                    cl -> {
                        Class<?> clazz = cl.loadClass(pluginClassName);
                        Constructor<?> constructor = clazz.getConstructor(SyncConf.class);
                        return (SinkFactory) constructor.newInstance(config);
                    });
        } catch (Exception e) {
            throw new FlinkxRuntimeException(e);
        }
    }
}
