package indi.arrowyi.autoconfigtest

import indi.arrowyi.autoconfig.AutoRegisterInt
import indi.arrowyi.autoconfig.AutoRegisterString
import indi.arrowyi.autoconfig.configmanager.AutoConfig


@AutoRegisterString(defaultValue = "kotlin")
const val KOLINT_TEST : String  = "KOLINT_TEST"

class KotlinTest {
    companion object{
        @AutoRegisterInt(defaultValue = 6)
        const val KOTLIN_COMPANION = "KOLINT_COM_TEST"
    }

    fun main(){
        println("test String is ${AutoConfig.get(KOLINT_TEST)}")
    }
}