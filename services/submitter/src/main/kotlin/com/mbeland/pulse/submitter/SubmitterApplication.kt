package com.mbeland.pulse.submitter

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class SubmitterApplication

fun main(args: Array<String>) {
    runApplication<SubmitterApplication>(*args)
}
