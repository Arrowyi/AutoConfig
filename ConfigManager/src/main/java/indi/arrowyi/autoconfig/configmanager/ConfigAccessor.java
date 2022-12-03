package indi.arrowyi.autoconfig.configmanager;

public interface ConfigAccessor {
    boolean set(String key, AutoConfig.Type type, Object value);
    Object get(String key, AutoConfig.Type type, Object defaultValue);
}
