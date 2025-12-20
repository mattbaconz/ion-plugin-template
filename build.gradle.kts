plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.example"
version = "1.1.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    implementation("com.github.mattbaconz:IonAPI:1.5.0")
    // Also add ion-paper explicitly for platform classes (JitPack multi-module workaround)
    implementation("com.github.mattbaconz.IonAPI:ion-paper:1.5.0")
    
    // HikariCP for database connection pooling
    implementation("com.zaxxer:HikariCP:5.1.0") {
        exclude(group = "org.slf4j")
    }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}

tasks {
    shadowJar {
        archiveClassifier.set("")
        
        // Note: Relocation is disabled due to ASM compatibility issues with Java 21 bytecode.
        // If you need relocation, use the shade-relocate plugin or wait for ShadowJar update.
        // relocate("com.ionapi", "com.example.iontemplate.libs.ionapi")
        
        // Note: minimize() is disabled as it can cause issues with reflection-heavy libraries.
        // minimize()
        
        // Remove test dependencies
        exclude("net/bytebuddy/**")
        exclude("org/mockito/**")
        exclude("org/junit/**")
        exclude("org/objenesis/**")
        exclude("org/opentest4j/**")
        exclude("org/apiguardian/**")
        
        // Remove META-INF bloat
        exclude("META-INF/*.SF")
        exclude("META-INF/*.DSA")
        exclude("META-INF/*.RSA")
        exclude("META-INF/maven/**")
        exclude("META-INF/versions/**")
        exclude("META-INF/native-image/**")
        exclude("META-INF/proguard/**")
    }
    
    build {
        dependsOn(shadowJar)
    }
}
