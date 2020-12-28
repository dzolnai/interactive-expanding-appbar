package com.egeniq.interactiveexpandingappbar.util


fun String?.formatAsPosterUrl() : String? {
    if (this == null) {
        return this
    }
    // We would have to fetch these from the configuration, but for this simple app we just hardcode these
    val baseUrl = "https://image.tmdb.org/t/p/"
    val fileSize = "w342"
    return "$baseUrl$fileSize$this"
}