package org.sourcejump

import org.kohsuke.github.GHContent
import java.io.File
import java.nio.file.Files

val tempDir = Files.createTempDirectory("sourcejump-")
  .toAbsolutePath().toString()

fun File.writeOrigin(result: GHContent, extension: String) =
  writeText(when (extension) {
    "java", "kt" -> "//"
    "py" -> "#"
    // TODO: Others?
    else -> null
  }?.let { "$it ${result.htmlUrl}\n\n" } ?: "")

fun List<GHContent>.store(tempDir: File, extension: String) {
  tempDir.mkdirs()
  val tempPath = tempDir.absolutePath
  forEachIndexed { i, result ->
    if (result.isFile) return@forEachIndexed
    result.read().use {
      File("$tempPath/${i}_" + result.name)
        .apply { writeOrigin(result, extension) }
        .appendBytes(it.readAllBytes())
    }
  }
}