package com.template.webserver

import com.google.gson.Gson
import com.template.flows.Initiator
import net.corda.core.identity.CordaX500Name
import net.corda.core.identity.Party
import net.corda.core.messaging.startFlow
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.getOrThrow
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.util.MultiValueMap
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestMethod
import org.springframework.web.client.RestTemplate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Controller
class Controller(rpc: NodeRPCConnection) {
    private val proxy = rpc.proxy

    private val INDEX: String = "index"

    private val URL: String = "https://api.freee.co.jp"
    private val VERSION: String = "/api/1/"

    private val rt: RestTemplate = RestTemplate();

    @RequestMapping(value = "/")
    fun index(): String {
        return INDEX
    }

    @RequestMapping(value = "/getCompanies", method = [RequestMethod.POST])
    fun getCompanies(@RequestBody body: MultiValueMap<String, String>, model: Model): String {
        val token = body.getFirst("token")

        val header = HttpHeaders()
        header.add("Authorization", "Bearer " + token)
        header.add("accept", "application/json")
        val req = HttpEntity<String>(header)

        // ログインユーザ情報を取得
        val USERS_ME: String = "users/me"
        val userJson = rt.exchange(URL + VERSION + USERS_ME, HttpMethod.GET, req, String::class.java)
        val gson = Gson()
        val json = gson.fromJson(userJson.body, Users::class.java)

        // 事業所情報を取得
        val COMPANIES: String = "companies"
        val response = rt.exchange(URL + VERSION + COMPANIES, HttpMethod.GET, req, String::class.java)

        // counterParty
        val x500Name = CordaX500Name.parse("O=PartyB,L=New York,C=US")
        val counterParty = proxy.wellKnownPartyFromX500Name(x500Name) as Party

        // date
        val current = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val formatted = current.format(formatter)

        // Flow Start
        val result = proxy.startFlow(
                ::Initiator,
                counterParty,
                json.user.display_name, // id
                "get companies", // action
                formatted.toString(),  // date
                response.body          // data
        ).returnValue.getOrThrow()

        when (result) {
            is SignedTransaction -> {
                model.addAttribute("code", "200")
                model.addAttribute("data", response.body)
            }
            else -> model.addAttribute("error", "400")
        }

        return INDEX
    }
}

data class User(
        val id: String,
        val email: String,
        val display_name: String,
        val first_name: String,
        val last_name: String,
        val first_name_kana: String,
        val last_name_kana: String
)

data class Users(
        val user: User
)
