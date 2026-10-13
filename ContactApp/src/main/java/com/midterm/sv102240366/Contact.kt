package com.midterm.sv102240366

import java.io.Serializable

data class Contact(
    val id: String,
    var name: String,
    var phone: String,
    var group: String,
    var note: String = "",
    var isFavorite: Boolean = false
) : Serializable