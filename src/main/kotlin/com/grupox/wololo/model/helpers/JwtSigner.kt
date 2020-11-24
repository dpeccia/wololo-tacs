package com.grupox.wololo.model.helpers

import arrow.core.*
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.User
import io.jsonwebtoken.*
import io.jsonwebtoken.security.Keys
import org.bson.types.ObjectId
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Service
import java.security.KeyPair
import java.time.Duration
import java.time.Instant
import java.util.*

@Service
object JwtSigner {
    private val keyPair: KeyPair = Keys.keyPairFor(SignatureAlgorithm.RS256)

    fun createJwt(userId: String): String {
        return Jwts.builder()
                .signWith(keyPair.private, SignatureAlgorithm.RS256)
                .setSubject(userId)
                .setIssuer("identity")
                .setExpiration(Date.from(Instant.now().plus(Duration.ofMinutes(60))))
                .setIssuedAt(Date.from(Instant.now()))
                .compact()
    }

    fun validateJwt(jwt: Option<String>): Either<CustomException, Jws<Claims>> =
            try {
                Right(Jwts.parserBuilder()
                        .setSigningKey(keyPair.public)
                        .build()
                        .parseClaimsJws(jwt.getOrElse { throw CustomException.Unauthorized.TokenException("Token not found. Login Again") }))
            } catch (ex: ExpiredJwtException) { Left(CustomException.Unauthorized.TokenException("Token expired. Login Again"))
            } catch (ex: Exception) { Left(CustomException.Unauthorized.TokenException("Token expired. Login Again"))
            } catch (ex: CustomException) { Left(ex) }
}