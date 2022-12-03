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
