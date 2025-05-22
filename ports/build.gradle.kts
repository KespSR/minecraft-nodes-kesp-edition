import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

version = "0.0.13"

val OUTPUT_JAR_NAME = "nodes-ports"
val mcTarget        = "1.21"
val JVM_VERSION     = 21          // JDK y byte-code

plugins {
    id("org.jetbrains.kotlin.jvm") version "2.0.20"
    id("com.gradleup.shadow")      version "8.3.2"   // fork con ASM 9.6
    application                                   // solo para fijar mainClass
    `maven-publish`
}

application {
    mainClass.set("dummy.Main")    // no existe; evita la validación de Shadow
}

repositories {
    mavenCentral()
    maven { url = uri("https://repo.papermc.io/repository/maven-public/") }
    maven { url = uri("https://ci.ender.zone/plugin/repository/everything") }
}

java {
    toolchain.languageVersion.set(JavaLanguageVersion.of(JVM_VERSION))
    // byte-code 21; necesario para enlazar con paper-api 1.21
}

configurations {
    create("resolvableImplementation") {
        isCanBeResolved = true
        isCanBeConsumed = true
    }
}

dependencies {
    compileOnly(platform("org.jetbrains.kotlin:kotlin-bom"))
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

    implementation(project(":nodes"))

    compileOnly("io.papermc.paper:paper-api:$mcTarget-R0.1-SNAPSHOT")

    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit")
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions.jvmTarget.set(JvmTarget.JVM_21)
}

tasks.named<ShadowJar>("shadowJar") {
    archiveFileName.set("${OUTPUT_JAR_NAME}-${mcTarget}-${version}.jar")
    relocate("com.google", "ports.shadow.gson")
    minimize()
    configurations = listOf(project.configurations.named("resolvableImplementation").get())
}

tasks {
    build { dependsOn(shadowJar) }
    test  { testLogging.showStandardStreams = true }
}
