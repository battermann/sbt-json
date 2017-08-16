lazy val root = Project("plugins", file(".")) dependsOn sbtJson

lazy val sbtJson = file("..").getAbsoluteFile.toURI