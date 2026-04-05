package com.mbeland.pulse.projector

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication

@SpringBootApplication
@ConfigurationPropertiesScan
class ProjectorApplication

fun main(args: Array<String>) {
    runApplication<ProjectorApplication>(*args)
}
