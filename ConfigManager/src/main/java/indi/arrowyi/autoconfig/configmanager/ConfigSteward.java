
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
                ConfigLog.d(key + "'s accessor is " + accessor == null ? "null !!!" : "not null " + " and loader is "
                        + loader == null ? "null !!!" : "not null");

                if (defaultValue == null && loader == null) {
                    ConfigLog.e("both default value and loader are null --> " + key);
                }

                Object dv = loader != null ? loader.getDefaultValue(key, flyweight.getType()) : defaultValue;
                if (dv == null) {
                    ConfigLog.e("default value is null --> " + key);
                    return null;
                }

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
            if (flyweight.getType().isTypeOf(value)) {
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
            Object dv;
            if (flyweight.getDefaultValueLoader() != null) {
                dv = flyweight.getDefaultValueLoader().getDefaultValue(key, flyweight.getType());
            } else {
                dv = defaultValue;
            }

            int res = setValue(key, dv);
            return (res == 1 || res == 2);
        }
    }

    private Map<String, ConfigStuff> keys = new HashMap<>();
    ConfigFlyweightFactory configFlyweightFactory = new ConfigFlyweightFactory(this);


    synchronized void register(String key, AutoConfig.Type type, String accessor, String defaultLoader
            , Object defaultValue, boolean overwrite) {

        if (key == null) {
            ConfigLog.e("register key is null !!!");
            return;
        }

        if (defaultValue == null && (defaultLoader == null || defaultLoader.isEmpty())) {
            ConfigLog.e("both default value and default loader are null!!! --> " + key);
            return;
        }

        if (type == null || !type.isTypeOf(defaultValue)) {
            ConfigLog.e("register type is null --> " + key);
            return;
        }

        ConfigFlyweight configFlyweight = configFlyweightFactory.getInfo(type, (accessor != null ? accessor : "")
                , (defaultLoader != null ? defaultLoader : ""));
        register(key, configFlyweight, defaultValue, overwrite);
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
            ConfigLog.e("getKeyType error : key has not registered");
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
            ConfigLog.e("config " + key + "is not registered correctly!!!");
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
            ConfigLog.e("the key : " + key + " has already defined !!");
        }

        keys.put(key, new ConfigStuff(defaultValue, configFlyweight));
    }
}
