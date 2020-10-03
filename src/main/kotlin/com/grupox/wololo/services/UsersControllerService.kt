package com.grupox.wololo.services

import com.grupox.wololo.errors.CustomException
import com.grupox.wololo.model.User
import com.grupox.wololo.model.helpers.*
import com.grupox.wololo.model.repos.RepoUsers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseCookie
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Service
import org.springframework.web.util.WebUtils
import javax.servlet.http.HttpServletRequest

@Service
class UsersControllerService(@Autowired val repoUsers: RepoUsers) {

    @Autowired
    private lateinit var sha512: SHA512Hash

    fun createUser(newUser: UserForm) : DTO.UserDTO {
        if(repoUsers.findByIsAdminFalseAndMail(newUser.mail).isPresent)
            throw CustomException.BadRequest.IllegalUserException("There is already an user under that email")
        val user = User(newUser.username, newUser.mail, sha512.getSHA512(newUser.password))
        repoUsers.save(user)
        return user.dto()
    }

    fun login(userDto: DTO.UserDTO) : ResponseEntity<DTO.UserDTO>{
        val jwt = JwtSigner.createJwt(userDto.id)
        val authCookie = ResponseCookie.fromClientResponse("X-Auth", jwt)
                .maxAge(3600)
                .httpOnly(true)
                .path("/")
                .secure(false) // Setear a true si tenemos https
                .build()
        return ResponseEntity.ok().header("Set-Cookie", authCookie.toString()).body(userDto)
    }

    fun logout(request: HttpServletRequest): ResponseEntity<Void> {
        val authCookie = ResponseCookie.fromClientResponse("X-Auth", WebUtils.getCookie(request, "X-Auth")!!.value)
                .maxAge(0)
                .httpOnly(true)
                .path("/")
                .secure(false)
                .build()
        return ResponseEntity.ok().header("Set-Cookie", authCookie.toString()).build<Void>()
    }

    fun checkUserCredentials(user: LoginForm): DTO.UserDTO =
        repoUsers.findByMailAndPassword(user.mail, sha512.getSHA512(user.password))
                .orElseThrow { CustomException.Unauthorized.BadLoginException() }.dto()

    fun getUsers(_username: String?): List<DTO.UserDTO> {
        val username = _username ?: return repoUsers.findAllByIsAdminFalse().map { it.dto() }
        return repoUsers.findAllByIsAdminFalseAndUsernameLike(username).map { it.dto() }
    }
}