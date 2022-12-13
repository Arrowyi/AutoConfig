package indi.arrowyi.autoconfigtest

import indi.arrowyi.autoconfig.AutoRegisterString

object KotlinObjectTest {
    @AutoRegisterString(defaultValue = "object test")
    const val OBJ_TEST = "OBJ_TEST"
}