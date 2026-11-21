package cl.uchile.dcc

import munit.FunSuite

import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path, Paths}

class TemplateConfigurationSmokeTest extends FunSuite:
  private val repoRoot = Paths.get(sys.props("user.dir")).toAbsolutePath.normalize()

  private def resolve(path: String): Path =
    repoRoot.resolve(path).normalize()

  private def read(path: String): String =
    Files.readString(resolve(path), StandardCharsets.UTF_8)

  test("smoke suite is running under MUnit"):
    assertEquals(this.getClass.getSuperclass.getSimpleName, "FunSuite")

  test("critical repository files exist"):
    val requiredPaths = List(
      "build.sbt",
      "project/build.properties",
      "project/plugins.sbt",
      "README.md"
    )

    requiredPaths.foreach { relativePath =>
      assert(
        Files.exists(resolve(relativePath)),
        clue(s"Missing required file: $relativePath")
      )
    }

  test("source directory layout exists"):
    val requiredDirectories = List(
      "src/main/scala",
      "src/test/scala"
    )

    requiredDirectories.foreach { relativePath =>
      assert(
        Files.isDirectory(resolve(relativePath)),
        clue(s"Missing required directory: $relativePath")
      )
    }

  test("build.sbt declares scalaVersion and MUnit"):
    val buildSbt = read("build.sbt")

    assert(
      buildSbt.contains("scalaVersion"),
      "Expected build.sbt to declare scalaVersion"
    )
    assert(
      buildSbt.toLowerCase.contains("munit"),
      "Expected build.sbt to declare an MUnit dependency"
    )

  test("project/build.properties declares sbt.version"):
    val buildPropertiesPath = resolve("project/build.properties")
    val buildProperties = read("project/build.properties").trim

    assert(
      Files.size(buildPropertiesPath) > 0,
      "Expected project/build.properties to be non-empty"
    )
    assert(
      buildProperties.contains("sbt.version"),
      "Expected project/build.properties to declare sbt.version"
    )
