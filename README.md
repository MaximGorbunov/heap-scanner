# How to use 
1. Build ./gradlew jar
2. Run **may require sudo**
```shell
java  --add-opens java.base/java.lang=ALL-UNNAMED --add-modules jdk.hotspot.agent --add-exports jdk.hotspot.agent/sun.jvm.hotspot=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.tools=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.runtime=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.oops=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.memory=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.gc.g1=ALL-UNNAMED --add-exports jdk.hotspot.agent/sun.jvm.hotspot.utilities=ALL-UNNAMED -jar heap-scanner-1.0-SNAPSHOT.jar <java proccess pid>
```

## Flags
### --print-byte-arrays
Prints old generation byte arrays content as string 