# AutoConfig
This is a auto config(key - value) lib for the java project which is easy to define and use with the good extensibility.
It uses the annotation to make the key associate to its value -- what the default value is , how to store the value and how to get the default value.

## How to use
1. from the maven:
````
allprojects {
    repositories {
	    ...
	    maven { url 'https://jitpack.io' }
	}
}
````
2. add the dependency
````
dependencies {
	implementation 'com.github.Arrowyi:AutoConfig:Tag'
}
````
3. define your config

````
@AutoRegisterDouble(defaultValue = 03.141592654)
public static final String TEST_DOUIBLE = "test_double";
````
4. init the AutoConfig once
````
public static void main(String[] args){
    AutoConfig.init(null);
    AutoConfig.loadConfigRegister(null);
}
````
5.get the value
````
System.out.println(AutoConfig.getDouble(TEST_DOUIBLE));
````
this is the step you should do for the key-value config, and if you just do those, the **TEST_DOUIBLE** config will store the value in the runtime memory but not the permanent memory

## More tips
The default behavior of the default loader and the accessor is : 1. get the default value indicated in the  annotation and the store the value in the runtime memory.
If you want to change the default behaviors, you could indicate the ***default value loader*** or the ***accessor***, for example :
````
@AutoRegisterAccessor("Test")
public class TestAccessor implements ConfigAccessor {
    @Override
    public boolean set(String key, AutoConfig.Type type, Object value) {
        return false;
    }

    @Override
    public Object get(String key, AutoConfig.Type type, Object defaultValue) {
        return null;
    }
}

@AutoRegisterDefaultLoader("Test")
public class TestLoader implements DefaultValueLoader {

    @Override
    public Object getDefaultValue(String key, AutoConfig.Type type) {
        return null;
    }
}
````

and then , indicate the ***default loader*** or the ***accessor*** in the annotation with the **"name"**
````
    @AutoRegisterString(defaultLoader = "Test", accessor = "Test")
    public static final String TEST_STRING = "test_string";
````
so that, the ***TEST_STRING*** config will load the default value from the ***TestLoader*** and store the value with ***TestAccessor***

__More usage you could check the demo code in the Tester module.__

## Technical
This lib use the **flyweight** pattern to reduce the memory cost, and a type system to make sure the value's type is correct.




