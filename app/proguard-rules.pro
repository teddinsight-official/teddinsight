-dontwarn okhttp3.**
-dontwarn okio.**
-dontwarn javax.annotation.**
-dontwarn org.conscrypt.**
-dontwarn com.squareup.picasso.**
-dontnote retrofit2.Platform
-dontwarn retrofit2.Platform$Java8
-keepattributes Signature
-keepattributes Exceptions
-keep class ng.com.teddinsight.teddinsight_app.models.** { *; }
# Parceler library
-keep interface org.parceler.Parcel
-keep @org.parceler.Parcel class * { *; }
-keep class **$$Parcelable { *; }
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep public class * extends com.bumptech.glide.module.AppGlideModule
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
