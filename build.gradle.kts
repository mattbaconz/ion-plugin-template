plugins {
    java
    id("com.gradleup.shadow") version "8.3.5"
}

group = "com.example"
version = "1.0.0"

repositories {
    mavenLocal()
    mavenCentral()
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://jitpack.io")
}

dependencies {
    compileOnly("io.papermc.paper:paper-api:1.20.4-R0.1-SNAPSHOT")
    
    // IonAPI modules - only include what you need to reduce JAR size
    // Using JitPack for CI/CD builds, mavenLocal() for local development
    implementation("com.ionapi:ion-api:1.2.0")          // Core utilities (24KB)
    implementation("com.ionapi:ion-core:1.2.0")         // Platform abstraction (8KB)
    implementation("com.ionapi:ion-database:1.2.0")     // Database ORM (53KB)
    implementation("com.ionapi:ion-economy:1.2.0")      // Economy system (19KB)
    implementation("com.ionapi:ion-redis:1.2.0")        // Redis pub/sub (15KB)
    implementation("com.ionapi:ion-gui:1.2.0")          // GUI framework (22KB)
    implementation("com.ionapi:ion-item:1.2.0")         // Item builder (12KB)
    implementation("com.ionapi:ion-ui:1.2.0")           // Scoreboard/BossBar (11KB)
    implementation("com.ionapi:ion-tasks:1.2.0")        // Task scheduler (10KB)
    
    // To reduce size, comment out modules you don't use:
    // - Remove ion-redis if not using Redis (-15KB + Jedis dependency)
    // - Remove ion-database if not using database (-53KB + HikariCP)
    // - Remove ion-economy if not using economy (-19KB)
}

tasks.shadowJar {
    archiveClassifier.set("")
    // Note: Enable relocation in production to avoid conflicts with other plugins
    // relocate("com.ionapi", "${project.group}.libs.ionapi")
}

tasks.build {
    dependsOn(tasks.shadowJar)
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(21))
}
