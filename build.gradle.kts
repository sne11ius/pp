import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.JavaVersion.VERSION_21
import org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    jacoco
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.allopen)
    alias(libs.plugins.quarkus)
    alias(libs.plugins.spotless)
    alias(libs.plugins.detekt)
}

repositories {
    mavenCentral()
    mavenLocal()
}

dependencies {
    implementation(enforcedPlatform(libs.quarkus.bom))
    implementation("io.quarkus:quarkus-info")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation("io.quarkus:quarkus-rest-qute")
    implementation("io.quarkiverse.qute.web:quarkus-qute-web:3.4.0")
    implementation(libs.jackson.kotlin)
    implementation("io.quarkus:quarkus-websockets")
    implementation("io.quarkus:quarkus-scheduler")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation(libs.hamcrest.json)
    testImplementation("io.quarkus:quarkus-jacoco")
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation("io.rest-assured:rest-assured")
}

group = "pp"

// Updated via semantic-release-replace-plugin
version = "1.2.0"

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    finalizedBy(tasks.jacocoTestReport)
    configure<JacocoTaskExtension> {
        excludeClassLoaders = listOf("*QuarkusClassLoader")
        setDestinationFile(layout.buildDirectory.file("jacoco-quarkus.exec").get().asFile)
        reports {
            junitXml.required.set(true)
            html.required.set(true)
        }
    }
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    enabled = false
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<KotlinCompile> {
    compilerOptions.jvmTarget.set(JVM_21)
    compilerOptions.javaParameters.set(true)
}

configure<SpotlessExtension> {
    kotlin {
        diktat("2.0.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        diktat("2.0.0").configFile("./diktat-analysis.yaml")
    }
}
