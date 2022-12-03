package indi.arrowyi.autoconfigtest

import indi.arrowyi.autoconfig.AutoRegister
import indi.arrowyi.autoconfig.AutoRegisterToDefault
import indi.arrowyi.autoconfig.configmanager.AutoConfig


@AutoRegisterToDefault(type = AutoRegister.Type.STRING)
const val KOLINT_TEST : String  = "KOLINT_TEST"

class KotlinTest {
    companion object{
        @AutoRegisterToDefault(type = AutoRegister.Type.INT, defaultValue = "1")
        const val KOTLIN_COMPANION = "KOLINT_COM_TEST"
    }
    fun main(){
        println("test String is ${AutoConfig.get(KOLINT_TEST)}")
    }
}