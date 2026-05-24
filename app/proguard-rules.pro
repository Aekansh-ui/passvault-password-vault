# SQLCipher
-keep class net.sqlcipher.** { *; }
-keep class net.sqlcipher.database.** { *; }
-dontwarn net.sqlcipher.**

# Room
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class *
-keep @androidx.room.Dao interface *
-dontwarn androidx.room.**

# Hilt
-keep class dagger.hilt.** { *; }
-keep class javax.inject.** { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-dontwarn dagger.hilt.**

# Security Crypto
-keep class androidx.security.crypto.** { *; }

# Biometric
-keep class androidx.biometric.** { *; }

# Preserve source file names for crash debugging
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile
