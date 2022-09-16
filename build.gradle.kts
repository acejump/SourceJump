import org.gradle.api.JavaVersion.VERSION_11
import org.jetbrains.changelog.*

plugins {
  id("org.jetbrains.intellij") version "1.9.0"
  kotlin("jvm") version "1.7.20-RC"
  id("org.jetbrains.changelog") version "1.3.1"
  id("com.github.ben-manes.versions") version "0.42.0"
}

group = "org.sourcejump"
version = "0.0.1-SNAPSHOT"

repositories.mavenCentral()

dependencies {
  implementation(kotlin("stdlib"))
  implementation("org.kohsuke:github-api:1.308")
//  implementation("info.debatty:java-string-similarity:2.0.0")
}

// See https://github.com/JetBrains/gradle-intellij-plugin/
intellij {
  version.set("2022.2")
}

changelog {
  version.set("0.0.1")
  path.set("${project.projectDir}/CHANGES.md")
  header.set(provider { "[${project.version}] - ${date()}" })
  itemPrefix.set("-")
  unreleasedTerm.set("Unreleased")
}

tasks {
  compileKotlin {
    kotlinOptions.jvmTarget = VERSION_11.toString()
  }

  publishPlugin {
    val intellijPublishToken: String? by project
    token.set(intellijPublishToken)
  }

  patchPluginXml {
    sinceBuild.set("203.7717.56")
    changeNotes.set(provider {
      changelog.getAll().values.take(2).last().toHTML()
    })
  }
}
