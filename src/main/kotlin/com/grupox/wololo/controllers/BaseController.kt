package com.grupox.wololo.controllers

import arrow.core.toOption
import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.JwtSigner
import com.grupox.wololo.model.helpers.getOrThrow
import com.grupox.wololo.model.repos.RepoUsers
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jws
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.util.WebUtils
import java.util.*
import javax.servlet.http.Cookie
import javax.servlet.http.HttpServletRequest

abstract class BaseController {
    @ExceptionHandler(CustomException.NotFound::class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFoundException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.Forbidden::class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    fun handleForbiddenException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.BadRequest::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleBadRequestException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.Unauthorized::class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    fun handleUnauthorizedException(exception: CustomException) = exception.dto()

    @ExceptionHandler(CustomException.Service::class)
    @ResponseStatus(HttpStatus.FAILED_DEPENDENCY) // Revisar si es correcto esta http status
    fun handleServiceException(exception: CustomException) = exception.dto()

    fun checkAndGetToken(request: HttpServletRequest): Jws<Claims> {
        val cookie: Cookie? = WebUtils.getCookie(request, "X-Auth")
        val jwt = cookie?.value
        return JwtSigner.validateJwt(jwt.toOption()).getOrThrow()
    }

    fun throwIfNotAllowed(id: UUID) {
        val admin: User = RepoUsers.getAdminById(id).getOrThrow()
    }

}