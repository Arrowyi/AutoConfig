package indi.arrowyi.autoconfig.configmanager;

public interface DefaultValueLoader {
    Object getDefaultValue(String key, AutoConfig.Type type);
}
