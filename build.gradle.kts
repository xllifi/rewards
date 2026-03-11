import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.2.21"
  kotlin("plugin.serialization") version "2.2.21"
  alias(libs.plugins.fabric.loom)
  alias(libs.plugins.shadow)
  id("maven-publish")
}

version = "${project.property("mod_version")}+mc${libs.versions.minecraft.get()}"
group = project.property("maven_group") as String

base {
  archivesName = project.property("archives_base_name") as String
}

val targetJavaVersion = 21
java {
  toolchain.languageVersion = JavaLanguageVersion.of(targetJavaVersion)
  // Loom will automatically attach sourcesJar to a RemapSourcesJar task and to the "build" task
  // if it is present.
  // If you remove this line, sources will not be generated.
  withSourcesJar()
}
kotlin {
  compilerOptions {
    optIn.add("kotlin.time.ExperimentalTime")
  }
}

repositories {
  maven { url = uri("https://jitpack.io") }
  maven { url = uri("https://maven.nucleoid.xyz") }
}

val shadowMe: Configuration by configurations.creating {
  // This ensures your code can still see these libraries during compilation
  configurations.implementation.get().extendsFrom(this)
}

dependencies {
  // To change the versions see the gradle.properties file
  minecraft(libs.minecraft)
  mappings(loom.officialMojangMappings())
  modImplementation(libs.fabric.loader)
  modImplementation(libs.fabric.kotlin)
  modImplementation(libs.fabric.api)
  modImplementation(libs.advntr)
  include(libs.advntr)
  modImplementation(libs.sgui)
  include(libs.sgui)
  modImplementation(libs.polymer.core)
  modImplementation(libs.polymer.resourcepack)
  modImplementation(libs.servertranslations)
  include(libs.servertranslations)
  modImplementation(libs.placeholderapi)

  implementation(libs.exposed.core)
  shadowMe(libs.exposed.core)
  implementation(libs.exposed.dao)
  shadowMe(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  shadowMe(libs.exposed.jdbc)
  implementation(libs.exposed.json)
  shadowMe(libs.exposed.json)
  implementation(libs.sqlite.jdbc)
  shadowMe(libs.sqlite.jdbc)
  implementation(libs.pqsql.jdbc)
  shadowMe(libs.pqsql.jdbc)
  implementation(libs.brigadierkt)
  include(libs.brigadierkt)
}

tasks.shadowJar {
  configurations = listOf(shadowMe)

  relocate("org.jetbrains.exposed", "ru.xllifi.rewards.shadow.exposed")
  dependencies {
    exclude(dependency("org.jetbrains.kotlin:.*"))
  }

  mergeServiceFiles()

  archiveClassifier.set("shadow")
}
tasks.remapJar {
  // This tells Loom to use the output of shadowJar as the input for remapping
  inputFile.set(tasks.shadowJar.get().archiveFile)
}
tasks.build {
  dependsOn(tasks.shadowJar)
}

tasks.processResources {
  inputs.property("version", version)

  filesMatching("fabric.mod.json") {
    expand(
      "version" to version,
      "fabric_loader_version" to libs.versions.fabric.loader.get(),
      "minecraft_version" to libs.versions.minecraft.get(),
    )
  }
}

tasks.withType<JavaCompile>().configureEach {
  options.encoding = "UTF-8"
  options.release = targetJavaVersion
}

tasks.withType<KotlinCompile>().configureEach {
  compilerOptions.jvmTarget = JvmTarget.fromTarget(targetJavaVersion.toString())
}

tasks.jar {
  inputs.property("archivesName", project.base.archivesName)

  from("LICENSE.md") {
    rename { "${it}_${inputs.properties["archivesName"]}" }
  }
}

// configure the maven publication
publishing {
  publications {
    create<MavenPublication>("mavenJava") {
      artifactId = project.base.archivesName.get()
      from(components["java"])
    }
  }

  // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
  repositories {
    // Add repositories to publish to here.
    // Notice: This block does NOT have the same function as the block in the top level.
    // The repositories here will be used for publishing your artifact, not for
    // retrieving dependencies.
  }
}