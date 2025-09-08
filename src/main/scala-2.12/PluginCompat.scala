package com.lightbend.sbt.javaagent

import sbt.*
import java.nio.file.{ Path => NioPath }
import xsbti.FileConverter

private[javaagent] object PluginCompat {
  type FileRef = java.io.File
  type Out = java.io.File

  def toNioPath(a: Attributed[File])(implicit conv: FileConverter): NioPath =
    a.data.toPath()
  def toFile(a: Attributed[File])(implicit conv: FileConverter): File =
    a.data
  def toNioPaths(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[NioPath] =
    cp.map(_.data.toPath()).toVector
  def toFiles(cp: Seq[Attributed[File]])(implicit conv: FileConverter): Vector[File] =
    cp.map(_.data).toVector
  def toFileRef(a: File)(implicit conv: FileConverter): File = a

  // This adds `Def.uncached(...)`
  implicit class DefOp(singleton: Def.type) {
    def uncached[A1](a: A1): A1 = a
  }
}
