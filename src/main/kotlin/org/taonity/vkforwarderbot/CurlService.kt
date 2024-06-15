package org.taonity.vkforwarderbot

import mu.KotlinLogging
import org.springframework.stereotype.Component
import java.io.InputStreamReader
import java.util.stream.Collectors
import kotlin.jvm.optionals.getOrNull

private val LOGGER = KotlinLogging.logger {}

@Component
class CurlService(
    private val cacheService: CacheService
) {
    fun downloadVideoInCache(videoUrl: String, videoBaseName: String) : String? {
        val videoName = "${videoBaseName}.mp4"

        LOGGER.debug { "Start downloading the video $videoUrl with $videoBaseName name" }

        val process = ProcessBuilder(
            "bash.exe",
            "-c",
            "curl --silent --output ${cacheService.cacheDirPath}/${videoName} '${videoUrl}'",
        )
            .start()
        waitForVideoToDownload(process)

        val curlErrorLog = process.errorReader().lines().collect(Collectors.joining())

        if(curlErrorLog.isNotEmpty()) {
            LOGGER.error { curlErrorLog }
            return null
        }

        return cacheService.listFilesInCache().stream()
            .filter { cachedVideoName -> cachedVideoName.contains(videoName) }
            .findAny()
            .getOrNull()
    }

    private fun waitForVideoToDownload(process: Process) {
        // TODO: find a better way to do the waiting
        InputStreamReader(process.inputStream).use { inputStreamReader ->
            while (inputStreamReader.read() >= 0) {
            }
        }

        LOGGER.info { "Finish downloading the video" }
    }
}