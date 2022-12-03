package indi.arrowyi.autoconfigtest

import indi.arrowyi.autoconfig.AutoRegister

object KotlinObjectTest {
    @AutoRegister(type = AutoRegister.Type.STRING, defaultValue = "object test")
    const val OBJ_TEST = "OBJ_TEST"
}