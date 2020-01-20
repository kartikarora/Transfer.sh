package me.kartikarora.transfersh

import me.kartikarora.transfersh.custom.CountingTypedFile
import me.kartikarora.transfersh.models.TransferActivityModel
import org.junit.Assert.*
import org.junit.Test
import java.io.File
import java.nio.file.Files
import kotlin.random.Random

class BasicUnitTest {

    val model by lazy { TransferActivityModel() }

    @Test
    fun percentageCalculationIsBetweenZeroAndHundred() {
        val value = Random.nextLong(0,1000)
        val max = Random.nextLong(value,1000)
        val percentage = model.getPercentageFromValue(value, max)
        assertTrue(percentage in 0..100)
    }

    @Test
    fun testNetworkResponseIsOK() {
        val response = model.pingServerForResponseTest("https://transfer.sh")
        assertEquals(response.status, 200)
    }

    @Test
    fun testNetworkResponseIsFromCorrectServerThroughHeader() {
        val response = model.pingServerForResponseTest("https://transfer.sh")
        response.headers.forEach {
            if (it.name.equals("server", ignoreCase = true))
                assertTrue(it.value.contains("transfer.sh")) // will fail because header value is Transfer.sh
        }
    }

    @Test
    fun testUploadFileResponseIsOK() {
        val classLoader = javaClass.classLoader
        val file = File(classLoader?.getResource("test_image.jpg")?.file)
        val multipartTypedOutput = model.createMultipartDataFromFileToUpload(file, Files.probeContentType(file.toPath()), object : CountingTypedFile.FileUploadListener {
            override fun uploaded(num: Long) {
                println(num / file.totalSpace * 100)
            }
        })
        val response = model.uploadMultipartTypedOutputToRemoteServerTest("https://transfer.sh", file.name, multipartTypedOutput)
        assertEquals(response.status, 200)
    }

    @Test
    fun testUploadFilePercentFromModelIsBetweenZeroAndHundred() {

    }
}