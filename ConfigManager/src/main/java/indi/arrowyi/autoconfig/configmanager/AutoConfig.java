
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

import java.util.*;
import java.util.concurrent.*;

public final class AutoConfig {

    public static final String DEFAULT_ACCESSOR = "DEFAULT";
    public static final String DEFAULT_LOADER = "DEFAULT";

    public enum Type {
        INT(0) {
            boolean isTypeOf(Object value) {
                return value instanceof Integer;
            }
        },
        BOOLEAN(1) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Boolean;
            }

        },
        STRING(2) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof String;
            }
        },
        LONG(3) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Long;
            }
        },
        FLOAT(4) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Float;
            }
        },

        DOUBLE(5) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Double;
            }
        },

        OBJECT(6) {
            @Override
            boolean isTypeOf(Object value) {
                return value != null;
            }

        };

        private int value;

        Type(int value) {
            this.value = value;
        }

        int getValue() {
            return value;
        }

        abstract boolean isTypeOf(Object value);

    }

    public interface ConfigChangedListener {
        void onConfigChanged(String item, Object value);
    }

    private static AutoConfig sInstance = null;

    private AutoConfig() {
    }

    public static synchronized void init(AutoConfigLog log) {
        if (sInstance != null) {
            return;
        }

        ConfigLog.autoConfigLog = log;
        sInstance = new AutoConfig();
    }

    public static void loadConfigRegister() {
        loadConfigRegister(null);
    }

    //if the loader is null, will use the current thread context loader
    public static void loadConfigRegister(ClassLoader loader) {
        ServiceLoader<ConfigRegister> load = (loader == null ? ServiceLoader.load(ConfigRegister.class)
                : ServiceLoader.load(ConfigRegister.class, loader));
        load.forEach(register -> {
            AutoConfig.sInstance.runRegister(register);
        });
    }

    public static void addChangedListener(String key, ConfigChangedListener listener) {
        sInstance.doAddChangedListener(key, listener);
    }

    public static void removeChangedListener(String key, ConfigChangedListener listener) {
        sInstance.doRemoveChangedListener(key, listener);
    }


    public static void registerAccessor(String name, ConfigAccessor accessor) {
        sInstance.doRegisterAccessor(name, accessor);
    }

    public static void registerDefaultValueLoader(String name, DefaultValueLoader loader) {
        sInstance.doRegisterDefaultValueLoader(name, loader);
    }

    public static void registerInt(String key, String accessor, String defaultLoader
            , int defaultValue) {
        sInstance.doRegister(key, Type.INT, accessor, defaultLoader, Integer.valueOf(defaultValue));
    }

    public static void registerLong(String key, String accessor, String defaultLoader
            , long defaultValue) {
        sInstance.doRegister(key, Type.LONG, accessor, defaultLoader, Long.valueOf(defaultValue));
    }

    public static void registerBoolean(String key, String accessor, String defaultLoader
            , boolean defaultValue) {
        sInstance.doRegister(key, Type.BOOLEAN, accessor, defaultLoader, Boolean.valueOf(defaultValue));
    }

    public static void registerFloat(String key, String accessor, String defaultLoader
            , float defaultValue) {
        sInstance.doRegister(key, Type.FLOAT, accessor, defaultLoader, Float.valueOf(defaultValue));
    }

    public static void registerDouble(String key, String accessor, String defaultLoader
            , double defaultValue) {
        sInstance.doRegister(key, Type.DOUBLE, accessor, defaultLoader, Double.valueOf(defaultValue));
    }

    public static void registerString(String key, String accessor, String defaultLoader
            , Object defaultValue) {
        sInstance.doRegister(key, Type.STRING, accessor, defaultLoader, (String) defaultValue);
    }

    public static void registerObject(String key, String accessor, String defaultLoader, Object defaultValue) {
        sInstance.doRegister(key, Type.OBJECT, accessor, defaultLoader, defaultValue);
    }

    private static boolean set(String key, Object value) {
        return sInstance.doSet(key, value);
    }

    public static boolean setInt(String key, int value) {
        return set(key, Integer.valueOf(value));
    }

    public static boolean setLong(String key, long value) {
        return set(key, Long.valueOf(value));
    }

    public static boolean setFloat(String key, float value) {
        return set(key, Float.valueOf(value));
    }

    public static boolean setDouble(String key, double value) {
        return set(key, Double.valueOf(value));
    }

    public static boolean setBoolean(String key, boolean value) {
        return set(key, Boolean.valueOf(value));
    }

    public static boolean setString(String key, String value) {
        return set(key, value);
    }

    public static boolean setObject(String key, Object value) {
        return set(key, value);
    }

    public static Object get(String key) {
        return sInstance.doGet(key);
    }

    public static int getInt(String key) {
        return (Integer) get(key);
    }

    public static long getLong(String key) {
        return (Long) get(key);
    }

    public static float getFloat(String key) {
        Object value = get(key);
        if (value instanceof Double) {
            return ((Double) value).floatValue();
        }

        return (Float) value;
    }

    public static double getDouble(String key) {
        return (Double) get(key);
    }

    public static boolean getBoolean(String key) {
        return (Boolean) get(key);
    }

    public static String getString(String key) {
        return (String) get(key);
    }

    public static Object getObject(String key) {
        return get(key);
    }


    public static boolean isKeyDefined(String key) {
        return sInstance.doIsKeyDefined(key);
    }

    public static Type getKeyType(String key) {
        return sInstance.doGetKeyType(key);
    }

    public static boolean reset(String key) {
        return sInstance.doReset(key);
    }


    private Map<String, List<ConfigChangedListener>> keyListeners = new ConcurrentHashMap<>();
    private Executor notifyExecutor = new ThreadPoolExecutor(0
            , Runtime.getRuntime().availableProcessors(),
            60L, TimeUnit.SECONDS,
            new SynchronousQueue<Runnable>());

    static final ConfigSteward steward = new ConfigSteward();

    private Set<Class<?>> registers = new HashSet<>();

    private boolean checkRegisterHasRan(Class<?> registerClass) {
        return registers.contains(registerClass);
    }

    private void runRegister(ConfigRegister register) {
        if (checkRegisterHasRan(register.getClass())) {
            return;
        }

        register.register(this);
        registers.add(register.getClass());
    }

    private void doAddChangedListener(String key, ConfigChangedListener listener) {

        List<ConfigChangedListener> listeners = keyListeners.get(key);

        if (listeners == null) {
            if (!steward.isKeyDefined(key)) {
                ConfigLog.e("key : " + key + " has not been defined in settings yet !!!");
                return;
            }

            listeners = new ArrayList<>();
            keyListeners.put(key, listeners);
        }

        listeners.add(listener);
    }

    private void doRemoveChangedListener(String key, ConfigChangedListener listener) {
        List<ConfigChangedListener> listeners = keyListeners.get(key);

        if (listeners == null) {
            ConfigLog.e("removeChangedListener didn't have key listener");
            return;
        }

        listeners.remove(listener);
    }


    private void doRegisterAccessor(String name, ConfigAccessor accessor) {
        steward.configFlyweightFactory.registerAccessor(name, accessor);
    }

    private void doRegisterDefaultValueLoader(String name, DefaultValueLoader loader) {
        steward.configFlyweightFactory.registerDefaultLoader(name, loader);
    }

    private void doRegister(String key, AutoConfig.Type type, String accessor, String defaultLoader
            , Object defaultValue) {
        //if has the defaultloader, no need to use default value
        if (defaultLoader != null && !defaultLoader.isEmpty()) {
            defaultValue = null;
        }

        steward.register(key, type, accessor, defaultLoader, defaultValue, false);
    }


    private boolean doSet(String key, Object value) {
        switch (steward.setValue(key, value)) {
            case 2:
                return true;
            case 0:
                return false;
            case 1: {
                onSettingItemChanged(key, value);
                return true;
            }
            default:
                ConfigLog.e("setValue wrong res");
                return false;
        }
    }

    private Object doGet(String key) {
        return steward.getValue(key);
    }

    private boolean doIsKeyDefined(String key) {
        return steward.isKeyDefined(key);
    }

    private Type doGetKeyType(String key) {
        return steward.getKeyType(key);
    }

    private boolean doReset(String key) {
        boolean res = steward.reset(key);
        if (res) {
            onSettingItemChanged(key, steward.getCurValue(key));
        }
        return res;
    }


    private void onSettingItemChanged(String item, Object value) {
        List<ConfigChangedListener> listeners = keyListeners.get(item);
        if (listeners != null) {
            notifyExecutor.execute(() -> {
                for (ConfigChangedListener listener : listeners) {
                    listener.onConfigChanged(item, value);
                }
            });

        }
    }
}
