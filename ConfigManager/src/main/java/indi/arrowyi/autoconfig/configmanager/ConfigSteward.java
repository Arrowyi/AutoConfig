package indi.arrowyi.autoconfig.configmanager;

import java.util.HashMap;
import java.util.Map;

class ConfigSteward {
    private static class ConfigStuff {
        private final Object defaultValue;
        private Object curValue;
        private final ConfigFlyweight flyweight;

        private ConfigStuff(Object defaultValue, ConfigFlyweight flyweight) {
            this.defaultValue = defaultValue;
            this.flyweight = flyweight;
        }

        Object getCurValue() {
            return curValue;
        }

        Object getValue(String key) {
            if (curValue == null) {
                ConfigAccessor accessor = flyweight.getConfigAccessor();
                DefaultValueLoader loader = flyweight.getDefaultValueLoader();

                if (defaultValue == null && loader == null) {
                    throw new ConfigRuntimeException("both default value and loader are null");
                }

                Object dv = defaultValue == null ? loader.getDefaultValue(key, flyweight.getType()) : defaultValue;

                curValue = (accessor != null ? accessor.get(key, flyweight.getType(), dv) : dv);
            }

            return curValue;
        }


        /**
         * return : 1. success
         * 0. failed
         * 2. the same with current value
         */
        int setValue(String key, Object value) {
            if (checkValueType(flyweight.getType(), value)) {
                ConfigLog.e("setValue : type is wrong --> " + key + " : " + value);
                return 0;
            }

            if (curValue != null && curValue.equals(value)) {
                return 2;
            }

            ConfigAccessor accessor = flyweight.getConfigAccessor();

            if (accessor == null) {
                curValue = value;
                return 1;
            } else if (accessor.set(key, flyweight.getType(), value)) {
                curValue = value;
                return 1;
            } else {
                return 0;
            }
        }

        boolean reset(String key) {
            if (defaultValue != null) {
                curValue = defaultValue;
            } else {
                curValue = flyweight.getDefaultValueLoader().getDefaultValue(key, flyweight.getType());
            }

            return true;
        }

        private boolean checkValueType(AutoConfig.Type type, Object value) {
            switch (type) {
                case INT:
                    return value instanceof Integer;
                case LONG:
                    return value instanceof Long;
                case FLOAT:
                    return value instanceof Float;
                case DOUBLE:
                    return value instanceof Double;
                case STRING:
                    return value instanceof String;
                case BOOLEAN:
                    return value instanceof Boolean;
                default:
                    throw new ConfigRuntimeException("checkValueType : type is wrong " + type);
            }
        }
    }

    private Map<String, ConfigStuff> keys = new HashMap<>();
    ConfigFlyweightFactory configFlyweightFactory = new ConfigFlyweightFactory(this);


    synchronized void register(String key, AutoConfig.Type type, String accessor, String defaultLoader
            , String defaultValueStr, boolean overwrite) {
        try {
            Object dv = (defaultValueStr == null || defaultValueStr.isEmpty()) ? null : type.convertValue(defaultValueStr);

            if (dv == null && (defaultLoader == null || defaultLoader.isEmpty())) {
                throw new ConfigRuntimeException("both default value and default loader are null!!!");
            }

            ConfigFlyweight configFlyweight = configFlyweightFactory.getInfo(type, (accessor != null ? accessor : "")
                    , (dv != null ? "" : defaultLoader));
            register(key, configFlyweight, dv, overwrite);
        } catch (NumberFormatException e) {
            throw new ConfigRuntimeException("register error : default value error : " + defaultValueStr, e);
        }
    }

    synchronized boolean isKeyDefined(String key) {
        return keys.containsKey(key);
    }

    synchronized Object getCurValue(String key) {
        return keys.get(key).getCurValue();
    }

    AutoConfig.Type getKeyType(String key) {
        ConfigStuff stuff = keys.get(key);
        if (stuff == null) {
            throw new ConfigRuntimeException("getKeyType error : key has not registered");
        }

        return stuff.flyweight.getType();
    }

    /**
     * return : 1. success
     * 0. failed
     * 2. the same with current value
     */
    synchronized int setValue(String key, Object value) {
        return keys.get(key).setValue(key, value);
    }

    synchronized Object getValue(String key) {
        ConfigStuff stuff = keys.get(key);
        if (stuff == null) {
            throw new ConfigRuntimeException("config " + key + "is not registered correctly!!!");
        }

        return stuff.getValue(key);
    }

    public synchronized boolean reset(String key) {
        if (isKeyDefined(key)) {
            return keys.get(key).reset(key);
        }

        return false;
    }

    private void register(String key, ConfigFlyweight configFlyweight, Object defaultValue, boolean overwrite) {
        if (!overwrite && isKeyDefined(key)) {
            throw new ConfigRuntimeException("the key : " + key + " has already defined !!");
        }

        keys.put(key, new ConfigStuff(defaultValue, configFlyweight));
    }
}
