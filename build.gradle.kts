import org.jetbrains.changelog.*
import org.jetbrains.intellij.tasks.PublishTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
  id("org.jetbrains.intellij") version "0.7.2"
  kotlin("jvm") version "1.4.32"
  id("org.jetbrains.changelog") version "1.1.2"
  id("com.github.ben-manes.versions") version "0.38.0"
}

group = "org.sourcejump"
version = "0.0.1-SNAPSHOT"

repositories.mavenCentral()

dependencies {
  implementation(kotlin("stdlib"))
  implementation("org.kohsuke:github-api:1.127")
//  implementation("info.debatty:java-string-similarity:2.0.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
  version = "2021.1"
}

changelog {
  version = "0.0.1"
  path = "${project.projectDir}/CHANGES.md"
  header = closure { "[${project.version}] - ${date()}" }
  itemPrefix = "-"
  unreleasedTerm = "Unreleased"
}

tasks {
  withType<KotlinCompile> {
    kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
  }

  withType<PublishTask> {
    val intellijPublishToken: String? by project
    token(intellijPublishToken)
  }

  patchPluginXml {
    changeNotes(closure {
      changelog.getAll().values.take(2).last().toHTML()
    })
  }
}