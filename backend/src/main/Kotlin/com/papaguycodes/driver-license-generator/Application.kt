package com.papaguycodes.driver_license_generator

import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondText
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import java.time.LocalDate
import java.time.format.DateTimeParseException

fun main() {
    embeddedServer(Netty, port = 8080, module = Application::module).start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        jackson()
    }

    install(StatusPages) {
        exception<Throwable> { call, cause ->
            call.respond(HttpStatusCode.InternalServerError, cause.localizedMessage)
        }
    }

    routing {
        get("/") {
            val page = requireNotNull(Application::class.java.classLoader.getResource("index.html")) {
                "Frontend page index.html is missing from the application resources"
            }
            call.respondText(page.readText(), ContentType.Text.Html)
        }

        route("/generate") {
            post {
                val params = call.receive<LicenseRequest>()
                val errors = validateProfile(params)
                if (errors.isNotEmpty()) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("errors" to errors))
                    return@post
                }

                call.respond(
                    LicenseResponse(
                        isSample = true,
                        notice = "SAMPLE - NOT A REAL LICENSE",
                        state = params.state.trim().uppercase(),
                        firstName = params.firstName.trim(),
                        middleName = params.middleName.trim(),
                        lastName = params.lastName.trim(),
                        dateOfBirth = params.dateOfBirth.trim(),
                        address = params.address.trim(),
                        sampleId = "TEST-000001"
                    )
                )
            }
        }

        route("/validate") {
            post {
                val errors = validateProfile(call.receive<LicenseRequest>())
                call.respond(mapOf("valid" to errors.isEmpty(), "errors" to errors))
            }
        }
    }
}

data class LicenseRequest(
    val state: String = "WA",
    val firstName: String = "MARLEY",
    val middleName: String = "",
    val lastName: String = "",
    val dateOfBirth: String = "",
    val address: String = ""
)

data class LicenseResponse(
    val isSample: Boolean,
    val notice: String,
    val state: String,
    val firstName: String,
    val middleName: String,
    val lastName: String,
    val dateOfBirth: String,
    val address: String,
    val sampleId: String
)

private fun validateProfile(request: LicenseRequest): List<String> {
    val errors = mutableListOf<String>()
    val state = request.state.trim()
    val firstName = request.firstName.trim()
    val middleName = request.middleName.trim()
    val lastName = request.lastName.trim()
    val dateOfBirth = request.dateOfBirth.trim()
    val address = request.address.trim()

    if (!state.matches(Regex("[A-Za-z]{2}"))) {
        errors += "Demo region code must be exactly two letters."
    }
    if (firstName.isEmpty()) {
        errors += "First name is required."
    } else if (firstName.length > 60) {
        errors += "First name must be 60 characters or fewer."
    }
    if (middleName.length > 60) {
        errors += "Middle name must be 60 characters or fewer."
    }
    if (lastName.isEmpty()) {
        errors += "Last name is required."
    } else if (lastName.length > 60) {
        errors += "Last name must be 60 characters or fewer."
    }
    if (address.isEmpty()) {
        errors += "Fictional address is required."
    } else if (address.length > 160) {
        errors += "Fictional address must be 160 characters or fewer."
    }

    try {
        if (LocalDate.parse(dateOfBirth).isAfter(LocalDate.now())) {
            errors += "Date of birth cannot be in the future."
        }
    } catch (_: DateTimeParseException) {
        errors += "Enter a valid date of birth."
    }

    return errors
}
