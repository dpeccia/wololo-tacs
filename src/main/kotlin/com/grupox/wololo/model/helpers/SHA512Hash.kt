package com.grupox.wololo.model.helpers

import org.springframework.beans.factory.annotation.Autowired
import com.grupox.wololo.configs.properties.SHA512Properties
import java.security.MessageDigest
import org.springframework.stereotype.Service

@Service
class SHA512Hash {

    @Autowired
    private lateinit var sha512Properties: SHA512Properties

    fun getSHA512(toHash: String): String {
        var toHash = toHash
        for (i in 0..99999) {
            toHash = SHA512once(toHash + sha512Properties.salt)
        }
        return SHA512once(toHash)
    }

    private fun SHA512once(toHash: String): String {
        val md: MessageDigest

        md = MessageDigest.getInstance("SHA-512")
        md.update(toHash.toByteArray())
        val mb = md.digest()
        var out = ""
        for (i in mb.indices) {
            val temp = mb[i]
            var s = Integer.toHexString(temp.toInt())
            while (s.length < 2) {
                s = "0$s"
            }
            s = s.substring(s.length - 2)
            out += s
        }
        return out
    }

}
