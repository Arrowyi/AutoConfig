/*
 * Copyright (c) 2023.  Arrowyi. All rights reserved
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

package indi.arrowyi.autoconfig.dependancytest;

import indi.arrowyi.autoconfig.AutoRegisterDouble;
import indi.arrowyi.autoconfig.AutoRegisterFloat;
import indi.arrowyi.autoconfig.AutoRegisterInt;
import indi.arrowyi.autoconfig.AutoRegisterLong;
import indi.arrowyi.autoconfig.configmanager.AutoConfig;
import indi.arrowyi.autoconfig.configmanager.ConfigAccessor;
import indi.arrowyi.autoconfig.configmanager.DefaultValueLoader;

import static indi.arrowyi.autoconfigtest.AutoConfigTest.*;

public class DependancyTest {

    @AutoRegisterInt(defaultValue = 5)
    public static final String DEPENDANCY_TEST_INT = "dependancy_test_int";
    @AutoRegisterLong(defaultValue = 100000992)
    public static final String DEPENDANCY_TEST_LONG = "dependancy_test_long";
    @AutoRegisterFloat(defaultValue = 0.5f)
    public static final String DEPENDANCY_TEST_FLOAST = "dependancy_test_float";
    @AutoRegisterDouble(defaultValue = 03.141592654)
    public static final String DEPENDANCY_TEST_DOUIBLE = "dependancy_test_double";

    public static void main(String[] args) {
        AutoConfig.init(null);
        AutoConfig.loadConfigRegister(null);
        DependancyTest test = new DependancyTest();
        test.testRun();
    }

    private void testRun() {

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
                    case FLOAT:return 0.4f;
                    case DOUBLE:return 0.6;
                    case BOOLEAN:return true;
                    case STRING:return "test_aa";
                }
                return null;
            }
        });


        System.out.println(AutoConfig.getInt(TEST_INT_DEFAULT));

        System.out.println(AutoConfig.getString(TEST_STRING));

        System.out.println(AutoConfig.getDouble(DEPENDANCY_TEST_DOUIBLE));
    }

}
