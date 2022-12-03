package indi.arrowyi.autoconfig.configmanager;

import com.google.auto.service.AutoService;
import indi.arrowyi.autoconfig.configmanager.AutoConfig.Type;
import java.lang.Override;

@AutoService(ConfigRegister.class)
public class test_configCommonSettingsDef implements ConfigRegister {
  @Override
  public void register(AutoConfig config) {
    config.register("test_config", Type.INT, "", "", "5");
    config.register("OBJ_TEST", Type.STRING, "", "", "object test");
    config.register("TEST_String", Type.STRING, "DEFAULT", "DEFAULT", "");
    config.register("KOLINT_TEST", Type.STRING, "DEFAULT", "DEFAULT", "");
    config.register("KOLINT_COM_TEST", Type.INT, "DEFAULT", "DEFAULT", "1");
  }
}
