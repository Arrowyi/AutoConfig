package indi.arrowyi.autoconfig.configmanager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.concurrent.*;

public final class AutoConfig {

    public static final String DEFAULT_ACCESSOR = "DEFAULT";
    public static final String DEFAULT_LOADER = "DEFAULT";

    public enum Type {
        INT(0) {
            boolean isTypeOf(Object value) {
                return value instanceof Integer;
            }

            @Override
            Integer convertValue(String value) {
                return Integer.valueOf(value);
            }
        },
        BOOLEAN(1) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Boolean;
            }

            @Override
            Boolean convertValue(String value) {
                return Boolean.valueOf(value);
            }
        },
        STRING(2) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof String;
            }

            @Override
            String convertValue(String value) {
                return value;
            }
        },
        LONG(3) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Long;
            }

            @Override
            Long convertValue(String value) {
                return Long.valueOf(value);
            }
        },
        FLOAT(4) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Float;
            }

            @Override
            Float convertValue(String value) {
                return Float.valueOf(value);
            }
        },

        DOUBLE(5) {
            @Override
            boolean isTypeOf(Object value) {
                return value instanceof Double;
            }

            @Override
            Double convertValue(String value) {
                return Double.valueOf(value);
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

        abstract Object convertValue(String value);
    }

    public interface ConfigChangedListener {
        void onConfigChanged(String item, Object value);
    }

    private static AutoConfig sInstance = null;

    private AutoConfig() {
    }

    public static void init(AutoConfigLog log) {
        ConfigLog.autoConfigLog = log;
        sInstance = new AutoConfig();
        ServiceLoader<ConfigRegister> load = ServiceLoader.load(ConfigRegister.class);
        load.forEach(register -> {
            register.register(AutoConfig.sInstance);
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

    public static void register(String key, AutoConfig.Type type, String accessor, String defaultLoader
            , String defaultValue) {
        sInstance.doRegister(key, type, accessor, defaultLoader, defaultValue);
    }

    public static boolean set(String key, Object value) {
        return sInstance.doSet(key, value);
    }

    public static Object get(String key) {
        return sInstance.doGet(key);
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

    private void doAddChangedListener(String key, ConfigChangedListener listener) {

        List<ConfigChangedListener> listeners = keyListeners.get(key);

        if (listeners == null) {
            if (!steward.isKeyDefined(key)) {
                throw new ConfigRuntimeException("key : " + key + " has not been defined in settings yet !!!");
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
            , String defaultValue) {
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
                throw new ConfigRuntimeException("setValue wrong res");
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
