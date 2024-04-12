package org.jetbrains.bsp.bazel

import ch.epfl.scala.bsp4j.BuildTarget
import ch.epfl.scala.bsp4j.BuildTargetCapabilities
import ch.epfl.scala.bsp4j.BuildTargetIdentifier
import ch.epfl.scala.bsp4j.WorkspaceBuildTargetsResult
import org.jetbrains.bsp.GoBuildTarget
import org.jetbrains.bsp.bazel.base.BazelBspTestBaseScenario
import org.jetbrains.bsp.bazel.base.BazelBspTestScenarioStep
import kotlin.time.Duration.Companion.minutes
import java.net.URI

object BazelBspGoProjectTest : BazelBspTestBaseScenario() {

  val defaultSdkHomePath = URI("file://\$BAZEL_OUTPUT_BASE_PATH/external/go_sdk/")

  @JvmStatic
  fun main(args: Array<String>) = executeScenario()

  override fun expectedWorkspaceBuildTargetsResult(): WorkspaceBuildTargetsResult =
    WorkspaceBuildTargetsResult(
      listOf(
        exampleBuildTarget(),
        libBuildTarget(),
        libTestBuildTarget(),
      )
    )

  override fun scenarioSteps(): List<BazelBspTestScenarioStep> = listOf(
    workspaceBuildTargets(),
  )

  private fun workspaceBuildTargets(): BazelBspTestScenarioStep =
    BazelBspTestScenarioStep("workspace build targets") {
      testClient.testWorkspaceTargets(
        1.minutes,
        expectedWorkspaceBuildTargetsResult()
      )
    }

  private fun exampleBuildTarget(): BuildTarget {
    val exampleGoBuildTarget = GoBuildTarget(
      sdkHomePath = defaultSdkHomePath,
      importPath = "",
    )

    val exampleBuildTargetData = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//example:hello"),
      listOf("application"),
      listOf("go"),
      listOf(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = true; it.canDebug = true }
    )

    exampleBuildTargetData.displayName = "$targetPrefix//example:hello"
    exampleBuildTargetData.baseDirectory = "file://\$WORKSPACE/example/"
    exampleBuildTargetData.data = exampleGoBuildTarget
    exampleBuildTargetData.dataKind = "go"
    exampleBuildTargetData.dependencies = listOf(
      BuildTargetIdentifier("$targetPrefix//lib:go_default_library")
    )

    return exampleBuildTargetData
  }

  private fun libBuildTarget(): BuildTarget {
    val libGoBuildTarget = GoBuildTarget(
      sdkHomePath = defaultSdkHomePath,
      importPath = "example.com/lib",
    )

    val libBuildTargetData = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//lib:go_default_library"),
      listOf("library"),
      listOf("go"),
      listOf(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = false; it.canRun = false; it.canDebug = false }
    )

    libBuildTargetData.displayName = "$targetPrefix//lib:go_default_library"
    libBuildTargetData.baseDirectory = "file://\$WORKSPACE/lib/"
    libBuildTargetData.data = libGoBuildTarget
    libBuildTargetData.dataKind = "go"

    return libBuildTargetData
  }

  private fun libTestBuildTarget(): BuildTarget {
    val libTestGoBuildTarget = GoBuildTarget(
      sdkHomePath = defaultSdkHomePath,
      importPath = "",
    )

    val libTestBuildTargetData = BuildTarget(
      BuildTargetIdentifier("$targetPrefix//lib:go_default_test"),
      listOf("test"),
      listOf("go"),
      listOf(),
      BuildTargetCapabilities().also { it.canCompile = true; it.canTest = true; it.canRun = false; it.canDebug = true }
    )

    libTestBuildTargetData.displayName = "$targetPrefix//lib:go_default_test"
    libTestBuildTargetData.baseDirectory = "file://\$WORKSPACE/lib/"
    libTestBuildTargetData.data = libTestGoBuildTarget
    libTestBuildTargetData.dataKind = "go"

    return libTestBuildTargetData
  }
}
