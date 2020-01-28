package me.kartikarora.transfersh

import me.kartikarora.transfersh.custom.CountingTypedFile
import me.kartikarora.transfersh.models.TransferActivityModel
import org.junit.Assert.*
import org.junit.Test
import retrofit.RetrofitError
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

class BasicUnitTest {

    val model by lazy { TransferActivityModel() }

    @Test
    fun percentageCalculationIsBetweenZeroAndHundred() {
        val value = Random.nextLong(0, 1000)
        val max = Random.nextLong(value, 1000)
        val percentage = model.getPercentageFromValue(value, max)
        assertTrue(percentage in 0..100)
    }

    @Test
    fun testNetworkIsResponding() {
        try {
            val response = model.pingServerForResponseTest("https://transfer.sh")
            assertEquals(response.status, 200)
        } catch (error: RetrofitError) {
            assertEquals(error.response.status, 500)
        }
    }

    @Test
    fun testNetworkResponseIsFromCorrectServerThroughHeader() {
        val response = model.pingServerForResponseTest("https://transfer.sh")
        response.headers.forEach {
            if (it.name.equals("server", ignoreCase = true))
                assertFalse(it.value.contains("transfer.sh"))
        }
    }

    @Test
    fun testUploadFileEndpointIsResponding() {
        val classLoader = javaClass.classLoader
        val file = File(classLoader?.getResource("test_image.jpg")?.file)
        val multipartTypedOutput = model.createMultipartDataFromFileToUpload(file, Files.probeContentType(file.toPath()),
                CountingTypedFile.FileUploadListener { })
        try {
            val response = model.uploadMultipartTypedOutputToRemoteServerTest("https://transfer.sh", file.name, multipartTypedOutput)
            assertEquals(response.status, 200)
        } catch (error: RetrofitError) {
            assertEquals(error.response.status, 500)
        }
    }

    @Test
    fun testUploadFilePercentFromModelIsBetweenZeroAndHundred() {
        val classLoader = javaClass.classLoader
        val file = File(classLoader?.getResource("test_image.jpg")?.file)
        val multipartTypedOutput = model.createMultipartDataFromFileToUpload(file, Files.probeContentType(file.toPath()),
                CountingTypedFile.FileUploadListener {
                    val percentage = model.getPercentageFromValue(it, file.length())
                    assertTrue(percentage in 0..100)
                })
        try {
            val response = model.uploadMultipartTypedOutputToRemoteServerTest("https://transfer.sh", file.name, multipartTypedOutput)
            assertEquals(response.status, 200)
        } catch (error: RetrofitError) {
            assertEquals(error.response.status, 500)
        }
    }
}