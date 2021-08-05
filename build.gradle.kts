import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val dusseldorfKtorVersion = "2.1.6.2-6ce5eaa"
val ktorVersion = ext.get("ktorVersion").toString()
val mainClass = "no.nav.omsorgspengermidlertidigalene.AppKt"
val kafkaEmbeddedEnvVersion = ext.get("kafkaEmbeddedEnvVersion").toString()
val kafkaVersion = ext.get("kafkaVersion").toString() // Alligned med version fra kafka-embedded-env
val k9FormatVersion = "5.1.33"
val fuelVersion = "2.3.1"
val kotlinVersion = "1.5.21"

plugins {
    kotlin("jvm") version "1.5.21"
    id("com.github.johnrengelman.shadow") version "7.0.0"
}

buildscript {
    // Henter ut diverse dependency versjoner, i.e. ktorVersion.
    apply("https://raw.githubusercontent.com/navikt/dusseldorf-ktor/6ce5eaa4666595bb6b550fca5ca8bbdc242961a0/gradle/dusseldorf-ktor.gradle.kts")
}

dependencies {
    // Server
    implementation("no.nav.helse:dusseldorf-ktor-core:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-jackson:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-metrics:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-health:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-ktor-auth:$dusseldorfKtorVersion")
    implementation("com.github.kittinunf.fuel:fuel:$fuelVersion")
    implementation("com.github.kittinunf.fuel:fuel-coroutines:$fuelVersion"){
        exclude(group = "org.jetbrains.kotlinx", module = "kotlinx-coroutines-core")
    }

    // Client
    implementation("no.nav.helse:dusseldorf-ktor-client:$dusseldorfKtorVersion")
    implementation("no.nav.helse:dusseldorf-oauth2-client:$dusseldorfKtorVersion")
    implementation("io.lettuce:lettuce-core:5.3.5.RELEASE")
    implementation("com.github.fppt:jedis-mock:0.17.0")

    // Test
    testImplementation("no.nav.helse:dusseldorf-test-support:$dusseldorfKtorVersion")
    testImplementation("no.nav:kafka-embedded-env:$kafkaEmbeddedEnvVersion")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") {
        exclude(group = "org.eclipse.jetty")
    }

    //K9-format
    implementation("no.nav.k9:soknad:$k9FormatVersion")
    implementation("org.glassfish:jakarta.el:3.0.3")

    // kafka
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")

    testImplementation("org.skyscreamer:jsonassert:1.5.0")
    testImplementation("org.awaitility:awaitility-kotlin:4.1.0")
}

repositories {
    mavenLocal()
    maven {
        name = "GitHubPackages"
        url = uri("https://maven.pkg.github.com/navikt/dusseldorf-ktor")
        credentials {
            username = project.findProperty("gpr.user") as String? ?: System.getenv("GITHUB_USERNAME")
            password = project.findProperty("gpr.key") as String? ?: System.getenv("GITHUB_TOKEN")
        }
    }
    mavenCentral()
    maven("https://dl.bintray.com/kotlin/ktor")
    maven("https://kotlin.bintray.com/kotlinx")
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
    maven("https://dl.bintray.com/kotlin/kotlin-eap")
}


java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}


tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.named<KotlinCompile>("compileTestKotlin") {
    kotlinOptions.jvmTarget = "1.8"
}

tasks.withType<ShadowJar> {
    archiveBaseName.set("app")
    archiveClassifier.set("")
    manifest {
        attributes(
            mapOf(
                "Main-Class" to mainClass
            )
        )
    }
}

tasks.withType<Wrapper> {
    gradleVersion = "7.1"
}

tasks.withType<Test> {
    useJUnitPlatform()
}