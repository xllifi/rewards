import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.internal.config.LanguageVersion
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  kotlin("jvm") version "2.2.21"
  kotlin("plugin.serialization") version "2.2.21"
  alias(libs.plugins.fabric.loom)
  id("maven-publish")
}

version = "${project.property("mod_version")}+mc${libs.versions.minecraft.get()}"
group = project.property("maven_group") as String

base {
  archivesName = "${project.property("archives_base_name")}-$version"
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
  // Add repositories to retrieve artifacts from in here.
  // You should only use this when depending on other mods because
  // Loom adds the essential maven repositories to download Minecraft and libraries from automatically.
  // See https://docs.gradle.org/current/userguide/declaring_repositories.html
  // for more information about repositories.
  maven { url = uri("https://jitpack.io") }
  maven { url = uri("https://maven.nucleoid.xyz") }
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
  include(libs.polymer.core)
  modImplementation(libs.polymer.resourcepack)
  include(libs.polymer.resourcepack)

  implementation(libs.exposed.core)
  implementation(libs.exposed.dao)
  implementation(libs.exposed.jdbc)
  implementation(libs.exposed.json)
  implementation(libs.sqlite.jdbc)
  implementation(libs.pqsql.jdbc)
  implementation(libs.brigadierkt)
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