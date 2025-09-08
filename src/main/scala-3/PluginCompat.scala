package com.lightbend.sbt.javaagent

import java.nio.file.{ Path => NioPath }
import sbt.*
import xsbti.{ FileConverter, HashedVirtualFileRef, VirtualFile }

private[javaagent] object PluginCompat:
  type FileRef = HashedVirtualFileRef
  type Out = VirtualFile

  def toNioPath(a: Attributed[HashedVirtualFileRef])(using conv: FileConverter): NioPath =
    conv.toPath(a.data)
  inline def toFile(a: Attributed[HashedVirtualFileRef])(using conv: FileConverter): File =
    toNioPath(a).toFile()
  def toNioPaths(cp: Seq[Attributed[HashedVirtualFileRef]])(using conv: FileConverter): Vector[NioPath] =
    cp.map(toNioPath).toVector
  inline def toFiles(cp: Seq[Attributed[HashedVirtualFileRef]])(using conv: FileConverter): Vector[File] =
    toNioPaths(cp).map(_.toFile())
  def toFileRef(a: File)(using conv: FileConverter): HashedVirtualFileRef =
    conv.toVirtualFile(a.toPath())
end PluginCompat
