
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
    private static class ConfigCheckUtil {
        private ConfigCheckUtil() {
        }

        static Object getValue(String key, ConfigFlyweight flyweight) {
            ConfigAccessor accessor = flyweight.getConfigAccessor();
            DefaultValueLoader loader = flyweight.getDefaultValueLoader();
            ConfigLog.d(key + "'s accessor is " + accessor == null ? "null !!!" : "not null " + " and loader is "
                    + loader == null ? "null !!!" : "not null");

            if (accessor == null || loader == null) {
                ConfigLog.e("default value or accessor are null --> " + key);
                return null;
            }

            Object dv = loader.getDefaultValue(key, flyweight.getType());
            if (dv == null) {
                ConfigLog.e("default value is null --> " + key);
                return null;
            }

            if (!flyweight.getType().isTypeOf(dv)) {
                ConfigLog.e("default value type is not right --> " + key);
                return null;
            }

            return accessor.get(key, flyweight.getType(), dv);
        }


        /**
         * return : 1. success
         * 0. failed
         * 2. the same with current value
         */
        static int setValue(String key, Object value, ConfigFlyweight flyweight) {
            if (!flyweight.getType().isTypeOf(value)) {
                ConfigLog.e("setValue : type is wrong --> " + key + " : " + value);
                return 0;
            }

            ConfigAccessor accessor = flyweight.getConfigAccessor();

            if (accessor == null) {
                ConfigLog.e("setValue : accessor is null  --> " + key + " : " + value);
                return 0;
            } else if (accessor.set(key, flyweight.getType(), value)) {
                return 1;
            } else {
                ConfigLog.e("setValue : failed!! --> " + key + " : " + value);
                return 0;
            }
        }

        static boolean reset(String key, ConfigFlyweight flyweight) {
            DefaultValueLoader loader = flyweight.getDefaultValueLoader();
            if (loader == null) {
                ConfigLog.e("reset : default loader is null --> " + key);
                return false;
            }

            int res = setValue(key, loader.getDefaultValue(key, flyweight.getType()), flyweight);
            return (res == 1 || res == 2);
        }
    }

    private Map<String, ConfigFlyweight> keys = new HashMap<>();
    ConfigFlyweightFactory configFlyweightFactory = new ConfigFlyweightFactory(this);


    synchronized void register(String key, AutoConfig.Type type, String accessor, String defaultLoader, boolean overwrite) {

        if (key == null) {
            ConfigLog.e("register key is null !!!");
            return;
        }

        if (defaultLoader == null) {
            ConfigLog.e("default loader is null!!! --> " + key);
            return;
        }

        if (type == null) {
            ConfigLog.e("register type is null --> " + key);
            return;
        }

        ConfigFlyweight configFlyweight = configFlyweightFactory.getInfo(type, (accessor != null ? accessor : "")
                , (defaultLoader != null ? defaultLoader : ""));
        register(key, configFlyweight, overwrite);
    }

    synchronized boolean isKeyDefined(String key) {
        return keys.containsKey(key);
    }

    synchronized Object getCurValue(String key) {
        return ConfigCheckUtil.getValue(key, keys.get(key));
    }

    AutoConfig.Type getKeyType(String key) {
        ConfigFlyweight flyweight = keys.get(key);
        if (flyweight == null) {
            ConfigLog.e("getKeyType is null --> " + key);
            return null;
        }

        return flyweight.getType();
    }

    /**
     * return : 1. success
     * 0. failed
     * 2. the same with current value
     */
    synchronized int setValue(String key, Object value) {
        if (!isKeyDefined(key)) {
            ConfigLog.e("setValue " + key + "is not registered!!!");
            return 0;
        }
        return ConfigCheckUtil.setValue(key, value, keys.get(key));
    }

    synchronized Object getValue(String key) {
        ConfigFlyweight flyweight = keys.get(key);
        if (flyweight == null) {
            ConfigLog.e("config " + key + "is not registered correctly!!!");
            return null;
        }

        return ConfigCheckUtil.getValue(key, flyweight);
    }

    public synchronized boolean reset(String key) {
        if (!isKeyDefined(key)) {
            ConfigLog.e("reset failed !!!, key is not defined --> " + key);
            return false;
        }

        return ConfigCheckUtil.reset(key, keys.get(key));
    }

    private void register(String key, ConfigFlyweight configFlyweight, boolean overwrite) {
        if (!overwrite && isKeyDefined(key)) {
            ConfigLog.e("the key : " + key + " has already defined !!");
            return;
        }

        keys.put(key, configFlyweight);
    }
}
