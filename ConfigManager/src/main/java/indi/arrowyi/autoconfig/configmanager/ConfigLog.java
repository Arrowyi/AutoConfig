package indi.arrowyi.autoconfig.configmanager;


class ConfigLog {

    private static final String TAG = "AutoConfig : ";
    static AutoConfigLog autoConfigLog = null;

    public static void e(String msg) {
        if (autoConfigLog != null) {
            autoConfigLog.error(TAG + msg);
        }
    }
}
