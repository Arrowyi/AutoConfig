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
            throw new ConfigRuntimeException("config accessor is null");
        }

        synchronized (syncLock) {
            if (accessors.containsKey(name)) {
                throw new ConfigRuntimeException(name + " <-- accessor has already been registered!");
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
            throw new ConfigRuntimeException("default value loader is null");
        }

        synchronized (syncLock) {
            if (defaultLoaders.containsKey(name)) {
                throw new ConfigRuntimeException(name + " <-- default loader has already been registered!");
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
