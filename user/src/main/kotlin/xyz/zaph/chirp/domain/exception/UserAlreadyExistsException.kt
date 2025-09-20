package xyz.zaph.chirp.domain.exception

import java.lang.RuntimeException

class UserAlreadyExistsException: RuntimeException(" User with given email or username already exists")