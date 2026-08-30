package com.twohearts.app.services.media

import android.content.Context
import java.io.File

/**
 * FileService — file operations for non-media files.
 *
 * Matches legacy FileService with:
 * - Private app directory access
 * - File read/write operations
 * - File cleanup utilities
 */
class FileService(private val context: Context) {

    private val appDir: File = context.filesDir

    /**
     * Get the app's private directory.
     */
    fun getAppDirectory(): File = appDir

    /**
     * Get media directory.
     */
    fun getMediaDirectory(): File {
        val mediaDir = File(appDir, "media")
        if (!mediaDir.exists()) {
            mediaDir.mkdirs()
        }
        return mediaDir
    }

    /**
     * Read a file from app directory.
     */
    fun readFile(fileName: String): String? {
        return try {
            val file = File(appDir, fileName)
            if (file.exists()) {
                file.readText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Write a file to app directory.
     */
    fun writeFile(fileName: String, content: String): Boolean {
        return try {
            val file = File(appDir, fileName)
            file.writeText(content)
            true
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Delete a file from app directory.
     */
    fun deleteFile(fileName: String): Boolean {
        val file = File(appDir, fileName)
        return file.exists() && file.delete()
    }

    /**
     * Check if a file exists.
     */
    fun fileExists(fileName: String): Boolean {
        return File(appDir, fileName).exists()
    }

    /**
     * Get file size.
     */
    fun getFileSize(fileName: String): Long {
        val file = File(appDir, fileName)
        return if (file.exists()) file.length() else 0L
    }

    /**
     * Get total directory size.
     */
    fun getDirectorySize(directory: File): Long {
        var size = 0L
        if (directory.exists()) {
            if (directory.isFile) {
                size = directory.length()
            } else {
                directory.listFiles()?.forEach { file ->
                    size += getDirectorySize(file)
                }
            }
        }
        return size
    }

    /**
     * Clean up orphan media files.
     * Returns the number of files cleaned.
     */
    fun cleanupOrphanMedia(knownMediaIds: Set<String>): Int {
        val mediaDir = getMediaDirectory()
        var cleaned = 0

        mediaDir.listFiles()?.forEach { file ->
            val fileId = file.nameWithoutExtension
            if (fileId !in knownMediaIds) {
                if (file.delete()) {
                    cleaned++
                }
            }
        }

        return cleaned
    }

    /**
     * Get total app size (media + other files).
     */
    fun getTotalAppSize(): Long {
        return getDirectorySize(appDir)
    }
}
