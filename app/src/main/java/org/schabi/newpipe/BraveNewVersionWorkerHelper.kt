package org.schabi.newpipe

import com.grack.nanojson.JsonObject
import com.grack.nanojson.JsonParser
import org.schabi.newpipe.extractor.downloader.Response

object BraveNewVersionWorkerHelper {

    fun getVersionInfo(response: Response): JsonObject {
        val newpipeVersionInfo = JsonParser.`object`()
            .from(response.responseBody()).getObject("flavors")
            .getObject("github").getObject("stable")
        return newpipeVersionInfo
    }
}
