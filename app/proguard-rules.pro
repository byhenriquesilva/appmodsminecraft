# kotlinx.serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt
-keepclassmembers class kotlinx.serialization.json.** { *** Companion; }
-keepclasseswithmembers class com.byhenriquesilva.atlasdemods.network.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.byhenriquesilva.atlasdemods.network.model.**$$serializer { *; }
