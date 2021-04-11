package edu.mcgill.sourcejump

import java.nio.file.Files

val tempDir = Files.createTempDirectory("sourcejump-")
  .toAbsolutePath().toString()