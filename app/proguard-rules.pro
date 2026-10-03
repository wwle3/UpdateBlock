-dontwarn io.github.libxposed.annotation.**
-adaptresourcefilecontents META-INF/xposed/java_init.list
-keep public class * extends io.github.libxposed.api.XposedModule {
    public <init>();
}
-keep class moe.elin.updateblock.ModuleMain { public <init>(); }
-keep class moe.elin.updateblock.App { *; }
-keep class moe.elin.updateblock.MainActivity { *; }
