
/*
 * Copyright (c) 2022.  Arrowyi. All rights reserved
 * email : arrowyi@gmail.com
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package indi.arrowyi.autoconfig.configmanager;


class ConfigFlyweight {

    private AutoConfig.Type type;
    private ConfigAccessor configAccessor;
    private DefaultValueLoader defaultValueLoader;

    ConfigFlyweight(AutoConfig.Type type, ConfigAccessor configAccessor, DefaultValueLoader defaultValueLoader) {
        this.type = type;
        this.configAccessor = configAccessor;
        this.defaultValueLoader = defaultValueLoader;
    }

    AutoConfig.Type getType() {
        return type;
    }

    ConfigAccessor getConfigAccessor() {
        return configAccessor;
    }

    DefaultValueLoader getDefaultValueLoader() {
        return defaultValueLoader;
    }

    void setConfigAccessor(ConfigAccessor configAccessor) {
        this.configAccessor = configAccessor;
    }

    void setDefaultValueLoader(DefaultValueLoader defaultValueLoader) {
        this.defaultValueLoader = defaultValueLoader;
    }
}
