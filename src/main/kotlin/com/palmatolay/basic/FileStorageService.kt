package com.palmatolay.basic

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.core.io.Resource
import org.springframework.core.io.UrlResource
import org.springframework.stereotype.Service
import org.springframework.util.StringUtils
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.net.MalformedURLException
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.nio.file.StandardCopyOption

@Service
class FileStorageService @Autowired
constructor(fileStorageProperties: FileStorageProperties) {

    private val fileStorageLocation: Path = Paths.get(fileStorageProperties.uploadDir)
            .toAbsolutePath().normalize()

    init {

        try {
            Files.createDirectories(this.fileStorageLocation)
        } catch (ex: Exception) {
            throw FileStorageException("Could not create the directory where the uploaded files will be stored.", ex)
        }

    }

    fun storeFile(file: MultipartFile): String {
        // Normalize file name
        val fileName = StringUtils.cleanPath(file.originalFilename!!)
//        val fileName = file.originalFilename?.let { StringUtils.cleanPath(it) }

        try {
            // Check if the file's name contains invalid characters
            if (fileName.contains("..")) {
                throw FileStorageException("Sorry! Filename contains invalid path sequence $fileName")
            }

            // Copy file to the target location (Replacing existing file with the same name)
            val targetLocation = this.fileStorageLocation.resolve(fileName)
            Files.copy(file.inputStream, targetLocation, StandardCopyOption.REPLACE_EXISTING)

            return fileName
        } catch (ex: IOException) {
            throw FileStorageException("Could not store file $fileName. Please try again!", ex)
        }

    }

    fun loadFileAsResource(fileName: String): Resource {
        try {
            val filePath = this.fileStorageLocation.resolve(fileName).normalize()
            val resource = UrlResource(filePath.toUri())
            return if (resource.exists()) {
                resource
            } else {
                throw MyFileNotFoundException("File not found $fileName")
            }
        } catch (ex: MalformedURLException) {
            throw MyFileNotFoundException("File not found $fileName", ex)
        }

    }
}
