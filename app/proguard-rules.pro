# Room 与 Compose 自带 consumer rules，这里仅补充 Room 生成类的保留规则。
-keep class * extends androidx.room.RoomDatabase
-keep @androidx.room.Entity class * { *; }
