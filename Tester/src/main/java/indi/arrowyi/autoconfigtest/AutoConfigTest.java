package indi.arrowyi.autoconfigtest;


import indi.arrowyi.autoconfig.*;
import indi.arrowyi.autoconfig.configmanager.AutoConfig;
import indi.arrowyi.autoconfig.configmanager.ConfigAccessor;
import indi.arrowyi.autoconfig.configmanager.DefaultValueLoader;

public class AutoConfigTest {
    @AutoRegisterInt(defaultValue =5)
    public static final String TEST_INT = "test_int";
    @AutoRegisterLong(defaultValue = 100000992)
    public static final String TEST_LONG = "test_long";
    @AutoRegisterFloat(defaultValue = 0.5f)
    public static final String TEST_FLOAST = "test_float";
    @AutoRegisterDouble(defaultValue = 03.141592654)
    public static final String TEST_DOUIBLE = "test_double";
    @AutoRegisterBoolean(defaultValue = true)
    public static final String TEST_BOOLEAN = "test_boolean";
    @AutoRegisterString(defaultValue = "test")
    public static final String TEST_STRING = "test_string";

    @AutoRegisterIntWithDefault
    public static final String TEST_INT_DEFAULT = "test_int_default";
    @AutoRegisterFloatWithDefault
    public static final String TEST_FLOAT_DEFAULT = "test_float_default";
    @AutoRegisterBooleanWithDefault
    public static final String TEST_BOOLEAN_DEFAULT = "test_boolean_default";

    @AutoRegisterStringWithDefault
    public static final String TEST_STRING_DEFAULT = "test_string_default";

    public static void main(String[] args){
        AutoConfig.init(null);
        AutoConfigTest test = new AutoConfigTest();
        test.testTestConfig();
    }
    public void testTestConfig(){
        System.out.println("config value is " + AutoConfig.getInt(TEST_INT));
        System.out.println(AutoConfig.getDouble(TEST_DOUIBLE));
        System.out.println(AutoConfig.getBoolean(TEST_BOOLEAN));
        System.out.println(AutoConfig.getString(TEST_STRING));

        AutoConfig.registerAccessor(AutoConfig.DEFAULT_ACCESSOR, new ConfigAccessor() {
            @Override
            public boolean set(String key, AutoConfig.Type type, Object value) {
                System.out.println("set with default accessor : " + key + " type is " + type + " value is :" + value);
                return true;
            }

            @Override
            public Object get(String key, AutoConfig.Type type, Object defaultValue) {
                System.out.println("get with default accessor : " + key + " type is " + type + " value is :" + defaultValue);
                return defaultValue;
            }
        });

        AutoConfig.registerDefaultValueLoader(AutoConfig.DEFAULT_LOADER, new DefaultValueLoader() {
            @Override
            public Object getDefaultValue(String key, AutoConfig.Type type) {
                System.out.println("get with default loader : " + key + " type is " + type);
                switch (type){
                    case INT:return 1;
                    case LONG:return 3;
                    case FLOAT:return 0.4;
                    case DOUBLE:return 0.6;
                    case BOOLEAN:return true;
                    case STRING:return "test_aa";
                }
                return null;
            }
        });

        AutoConfig.getInt(TEST_INT_DEFAULT);
        System.out.println(AutoConfig.getInt(TEST_INT_DEFAULT));

        System.out.println(AutoConfig.getFloat(TEST_FLOAT_DEFAULT));
        System.out.println(AutoConfig.getBoolean(TEST_BOOLEAN_DEFAULT));
        System.out.println(AutoConfig.getString(TEST_STRING_DEFAULT));


    }

}
