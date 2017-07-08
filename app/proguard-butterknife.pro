###---------------Begin: proguard configuration for ButterKnife  ----------
# For Butterknife:
-keep class butterknife.** { *; }
-dontwarn butterknife.internal.**

# Version 8
-keep class **_ViewBinding { *; }

-keepclasseswithmembernames class * { @butterknife.* <fields>; }
-keepclasseswithmembernames class * { @butterknife.* <methods>; }
###---------------End: proguard configuration for ButterKnife  ----------