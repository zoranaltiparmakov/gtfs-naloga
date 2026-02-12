plugins {
    id("java")
}

group = "com.ridango"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Only for CLI friendly-output.
    implementation("tools.jackson.core:jackson-databind:3.0.4")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    testImplementation("org.mockito:mockito-core:5.21.0")
    testImplementation("org.mockito:mockito-junit-jupiter:5.21.0")
}

tasks.test {
    useJUnitPlatform()
    // Attach mockito to Java agent to enable inline-mock-maker.
    jvmArgs( "-javaagent:" + configurations.testRuntimeClasspath.get() .first { it.name.contains("mockito-core") }.absolutePath )
}