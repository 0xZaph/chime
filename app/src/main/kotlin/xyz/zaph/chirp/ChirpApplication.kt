package xyz.zaph.chirp

import jakarta.annotation.PostConstruct
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.stereotype.Component
import xyz.zaph.chirp.infra.database.entities.UserEntity
import xyz.zaph.chirp.infra.database.repositories.UserRepository

@SpringBootApplication
class ChirpApplication

fun main(args: Array<String>) {
    runApplication<ChirpApplication>(*args)
}

@Component
class Demo(
    private val repository: UserRepository
) {
    @PostConstruct
    fun init() {
        if (repository.findByEmail("foo@gmail.com") == null) {
            repository.save(
                UserEntity(
                    email = "foo@gmail.com",
                    username = "foo",
                    hashedPassword = "hashedPassword"
                )
            )
        }


    }
}