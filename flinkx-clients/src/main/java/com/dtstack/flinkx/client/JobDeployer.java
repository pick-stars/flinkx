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
package com.dtstack.flinkx.client;

import com.dtstack.flinkx.options.Options;
import com.dtstack.flinkx.util.MapUtil;

import org.apache.flink.configuration.Configuration;

import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.List;
import java.util.Properties;

/**
 * @program: flinkx
 * @author: xiuzhu
 * @create: 2021/05/31
 */
public class JobDeployer {

    private List<String> programArgs;

    private Options launcherOptions;

    public JobDeployer(Options launcherOptions, List<String> programArgs) {
        this.launcherOptions = launcherOptions;
        this.programArgs = programArgs;
    }

    public Configuration getEffectiveConfiguration() {

        Configuration flinkConfig = launcherOptions.loadFlinkConfiguration();
        Configuration effectiveConfiguration = new Configuration(flinkConfig);

        try {
            String confProp = launcherOptions.getConfProp();
            if (StringUtils.isNotBlank(confProp)) {
                Properties properties = MapUtil.jsonStrToObject(confProp, Properties.class);
                properties.forEach(
                        (key, val) ->
                                effectiveConfiguration.setString(key.toString(), val.toString()));
            }
        } catch (IOException e) {
            throw new IllegalArgumentException(
                    "Illegal parameter in Flink config, Invalid confProp: "
                            + launcherOptions.getConfProp());
        }

        return effectiveConfiguration;
    }

    public List<String> getProgramArgs() {
        return programArgs;
    }

    public void setProgramArgs(List<String> programArgs) {
        this.programArgs = programArgs;
    }

    public Options getLauncherOptions() {
        return launcherOptions;
    }

    public void setLauncherOptions(Options launcherOptions) {
        this.launcherOptions = launcherOptions;
    }
}
