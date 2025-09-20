package xyz.zaph.chirp.domain.exception

class SamePasswordException : RuntimeException("New password must be different from the old one")