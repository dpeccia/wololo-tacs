package com.grupox.wololo.external_services_tests

import com.grupox.wololo.model.Coordinates
import com.grupox.wololo.model.externalservices.HttpService
import com.grupox.wololo.model.externalservices.TopoData
import com.grupox.wololo.model.helpers.getOrThrow
import okhttp3.*
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.spy

class TopoDataTest {
    private val topoData: TopoData = TopoData()
    private val coordinate1 = Coordinates((-62.435146).toFloat(), (-37.771053).toFloat())
    private val coordinate2 = Coordinates((-62.418774).toFloat(), (-36.89055).toFloat())

    private val correctJsonResponse: String = "{\n" +
            "  \"results\": [\n" +
            "    {\n" +
            "      \"elevation\": 311.2, \n" +
            "      \"location\": {\n" +
            "        \"lat\": -37.771053, \n" +
            "        \"lng\": -62.435146\n" +
            "      }\n" +
            "    }, \n" +
            "    {\n" +
            "      \"elevation\": 130.5, \n" +
            "      \"location\": {\n" +
            "        \"lat\": -36.89055, \n" +
            "        \"lng\": -62.418774\n" +
            "      }\n" +
            "    }\n" +
            "  ], \n" +
            "  \"status\": \"OK\"\n" +
            "}"

    private lateinit var response: Response

    @Test
    fun `successfully request elevation data from some Coordinates returns the elevation for each coordinate`() {
        response = Response.Builder()
                .body(ResponseBody.create(MediaType.parse("application/json"), correctJsonResponse))
                .code(200)
                .message("")
                .request(Request.Builder().url("https://api.opentopodata.org/v1/test-dataset?locations=-37.771053,-62.435146|-36.89055,-62.418774").build())
                .protocol(Protocol.HTTP_2)
                .build()

        topoData.httpService = spy(HttpService("TopoData"))
        Mockito.doReturn(response).`when`(topoData.httpService).getData("https://api.opentopodata.org/v1/test-dataset?locations=-37.771053,-62.435146|-36.89055,-62.418774")

        val result = topoData.requestElevation(listOf(coordinate1, coordinate2)).getOrThrow()
        assertThat(result.size).isEqualTo(2)
        assertThat(result.map { it.elevation }.sorted()).isEqualTo(listOf(130.5, 311.2))
        assertTrue(coordinate2.isEqualTo(result.minBy { it.elevation }!!.location))
        assertTrue(coordinate1.isEqualTo(result.maxBy { it.elevation }!!.location))
    }
}