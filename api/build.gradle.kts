import com.diffplug.gradle.spotless.SpotlessExtension
import org.gradle.api.JavaVersion.VERSION_21
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

val quarkusPlatformGroupId: String by project
val quarkusPlatformArtifactId: String by project
val quarkusPlatformVersion: String by project

dependencies {
    implementation(enforcedPlatform("$quarkusPlatformGroupId:$quarkusPlatformArtifactId:$quarkusPlatformVersion"))
    implementation("io.quarkus:quarkus-info")
    implementation("io.quarkus:quarkus-kotlin")
    implementation("io.quarkus:quarkus-resteasy-reactive")
    implementation("io.quarkus:quarkus-rest-qute")
    implementation("io.quarkiverse.qute.web:quarkus-qute-web")
    implementation("io.quarkus:quarkus-rest")
    implementation("io.quarkus:quarkus-rest-jackson")
    implementation(libs.jackson.kotlin)
    implementation("io.quarkus:quarkus-websockets")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("io.quarkus:quarkus-arc")
    testImplementation("io.quarkus:quarkus-junit5")
    testImplementation("io.quarkus:quarkus-junit5-mockito")
    testImplementation(libs.hamcrest.json)
    testImplementation(libs.mockito.kotlin)
    testImplementation(libs.mockito.inline)
    testImplementation("io.rest-assured:rest-assured")
}

group = "pp"

// Please do not change the following marker comments as they are used by the "release-please" github action.
// See https://github.com/googleapis/release-please/blob/main/docs/customizing.md#updating-arbitrary-files
// x-release-please-start-version
version = "0.0.1"
// x-release-please-end

java {
    sourceCompatibility = VERSION_21
    targetCompatibility = VERSION_21
}

tasks.test {
    systemProperty("java.util.logging.manager", "org.jboss.logmanager.LogManager")
    finalizedBy(tasks.jacocoTestReport)
}

tasks.jacocoTestReport {
    dependsOn(tasks.test)
    reports {
        xml.required = true
        html.required = false
    }
}

allOpen {
    annotation("jakarta.ws.rs.Path")
    annotation("jakarta.enterprise.context.ApplicationScoped")
    annotation("jakarta.persistence.Entity")
    annotation("io.quarkus.test.junit.QuarkusTest")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = VERSION_21.toString()
    kotlinOptions.javaParameters = true
}

configure<SpotlessExtension> {
    kotlin {
        diktat("2.0.0")
    }
    kotlinGradle {
        target("*.gradle.kts")
        diktat("2.0.0")
    }
}
