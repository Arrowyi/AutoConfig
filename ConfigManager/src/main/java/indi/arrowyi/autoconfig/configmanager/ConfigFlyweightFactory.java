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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

class ConfigFlyweightFactory {

    private Object syncLock;

    public ConfigFlyweightFactory(Object syncLock) {
        this.syncLock = syncLock;
    }

    private Map<String, ConfigAccessor> accessors = new HashMap<>();
    private Map<String, List<ConfigFlyweight>> noAssignedAccessors = new HashMap<>();
    private Map<String, DefaultValueLoader> defaultLoaders = new HashMap<>();
    private Map<String, List<ConfigFlyweight>> noAssignedLoaders = new HashMap<>();

    private Map<AutoConfig.Type, Map<String, Map<String, ConfigFlyweight>>> configInfoes = new HashMap<>();

    void registerAccessor(String name, ConfigAccessor configAccessor) {
        if (configAccessor == null) {
            ConfigLog.e("config accessor is null");
        }

        synchronized (syncLock) {
            if (accessors.containsKey(name)) {
                ConfigLog.e(name + " <-- accessor has already been registered!");
            }

            accessors.put(name, configAccessor);

            List<ConfigFlyweight> flyweights = noAssignedAccessors.remove(name);
            if (flyweights != null) {
                for (ConfigFlyweight flyweight : flyweights) {
                    flyweight.setConfigAccessor(configAccessor);
                }
            }
        }
    }

    void registerDefaultLoader(String name, DefaultValueLoader defaultValueLoader) {
        if (defaultValueLoader == null) {
            ConfigLog.e("default value loader is null");
        }

        synchronized (syncLock) {
            if (defaultLoaders.containsKey(name)) {
                ConfigLog.e(name + " <-- default loader has already been registered!");
            }
            defaultLoaders.put(name, defaultValueLoader);


            List<ConfigFlyweight> flyweights = noAssignedLoaders.remove(name);
            if (flyweights != null) {
                for (ConfigFlyweight flyweight : flyweights) {
                    flyweight.setDefaultValueLoader(defaultValueLoader);
                }
            }
        }
    }

    ConfigFlyweight getInfo(AutoConfig.Type type, String providerName, String defaultLoaderName) {
        synchronized (syncLock) {
            ConfigAccessor provider = accessors.get(providerName);
            DefaultValueLoader loader = defaultLoaders.get(defaultLoaderName);

            Map<String, Map<String, ConfigFlyweight>> providerAndLoaders = configInfoes.get(type);

            if (providerAndLoaders == null) {
                providerAndLoaders = new HashMap<>();
                configInfoes.put(type, providerAndLoaders);
            }

            Map<String, ConfigFlyweight> loaders = providerAndLoaders.get(providerName);

            if (loaders == null) {
                loaders = new HashMap<>();
                providerAndLoaders.put(providerName, loaders);
            }

            ConfigFlyweight configFlyweight = loaders.get(defaultLoaderName);

            if (configFlyweight == null) {
                configFlyweight = new ConfigFlyweight(type, provider, loader);
                loaders.put(defaultLoaderName, configFlyweight);
            }

            if (provider == null) {
                cacheAccessorAndLoaders(providerName, noAssignedAccessors, configFlyweight);
            }

            if (loader == null) {
                cacheAccessorAndLoaders(defaultLoaderName, noAssignedLoaders, configFlyweight);
            }

            return configFlyweight;
        }

    }

    private void cacheAccessorAndLoaders(String name, Map<String, List<ConfigFlyweight>> AccessorOrLoaders
            , ConfigFlyweight flyweight) {

        List<ConfigFlyweight> configFlyweights = AccessorOrLoaders.get(name);
        if (configFlyweights == null) {
            configFlyweights = new ArrayList<>();
            configFlyweights.add(flyweight);
            AccessorOrLoaders.put(name, configFlyweights);
        } else {
            configFlyweights.add(flyweight);
        }
    }
}
