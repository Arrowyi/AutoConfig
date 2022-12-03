package indi.arrowyi.autoconfigtest;


import indi.arrowyi.autoconfig.AutoRegister;
import indi.arrowyi.autoconfig.AutoRegisterToDefault;
import indi.arrowyi.autoconfig.configmanager.AutoConfig;

public class AutoConfigTest {
    @AutoRegister(type = AutoRegister.Type.INT, defaultValue ="5")
    public static final String TEST_CONFIG = "test_config";

    @AutoRegisterToDefault(type = AutoRegister.Type.STRING)
    public static final String TEST_String = "TEST_String";

    public static void main(String[] args){
        AutoConfig.init(null);
        AutoConfigTest test = new AutoConfigTest();
        test.testTestConfig();
    }
    public void testTestConfig(){
        System.out.println("config value is " + AutoConfig.get(TEST_CONFIG));
    }

}
