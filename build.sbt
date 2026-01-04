ThisBuild / scalaVersion := "3.3.5"
ThisBuild / organization := "com.expatledger"
ThisBuild / organizationName := "The Expat Ledger"
ThisBuild / coverageMinimumStmtTotal := 90
ThisBuild / coverageFailOnMinimum := true

import Dependencies.*

lazy val dockerImage = "eclipse-temurin:21-jre"

lazy val root = (project in file("."))
  .aggregate(sharedKernel, apiGateway, tenantService)
  .settings(
    name := "expat-ledger-backend",
  )

lazy val sharedKernel = (project in file("modules/shared-kernel"))
  .enablePlugins(Fs2Grpc)
  .settings(
    name := "shared-kernel",
    Compile / scalacOptions ~= (_.filterNot(_ == "-Wvalue-discard")),
    libraryDependencies ++= sharedKernelDependencies
  )

lazy val apiGateway = (project in file("modules/api-gateway"))
  .dependsOn(sharedKernel)
  .enablePlugins(JavaAppPackaging, DockerPlugin)
  .settings(
    name := "api-gateway",
    libraryDependencies ++= apiGatewayDependencies,
    dockerBaseImage := dockerImage,
    dockerExposedPorts := Seq(8080)
  )

lazy val tenantService = (project in file("modules/tenant-service"))
  .dependsOn(sharedKernel)
  .enablePlugins(JavaAppPackaging, DockerPlugin, Fs2Grpc)
  .settings(
    name := "tenant-service",
    libraryDependencies ++= tenantServiceDependencies,
    dockerBaseImage := dockerImage,
    dockerExposedPorts := Seq(9000)
  )
