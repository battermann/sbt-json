lazy val root = Project("plugins", file(".")) dependsOn sbtJson

lazy val sbtJson = ClasspathDependency(RootProject(file("..").getAbsoluteFile.toURI), None)