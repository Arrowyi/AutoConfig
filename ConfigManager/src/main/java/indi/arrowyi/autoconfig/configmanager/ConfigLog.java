/*
 * Copyright (c) 2022.  Arrowyi. All rights reserved
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

package indi.arrowyi.autoconfig.configmanager;


class ConfigLog {

    private static final String TAG = "AutoConfig : ";
    static AutoConfigLog autoConfigLog = null;
    static Boolean debug = true;

    static void e(String msg, Throwable e) {
        if (!debug) {
            throw new ConfigRuntimeException(msg, e);
        }

        if (autoConfigLog != null) {
            autoConfigLog.error(TAG + msg + " with " + e.toString());
        }
    }

    static void e(String msg) {
        if (!debug) {
            throw new ConfigRuntimeException(msg);
        }

        if (autoConfigLog != null) {
            autoConfigLog.error(TAG + msg);
        }
    }

    static void d(String msg) {
        if (autoConfigLog != null) {
            autoConfigLog.warning(TAG + msg);
        }
    }
}
