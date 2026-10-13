package com.midterm.sv102240366

import java.util.UUID

object ContactRepository {
    val contactList = mutableListOf<Contact>()

    init {
        contactList.add(Contact(UUID.randomUUID().toString(), "Đồng Huy Hoàng", "0901234567", "Gia đình"))
        contactList.add(Contact(UUID.randomUUID().toString(), "Nguyễn Văn A", "0987654321", "Công việc"))
        contactList.add(Contact(UUID.randomUUID().toString(), "Trần Thị B", "0911222333", "Bạn bè"))
        contactList.add(Contact(UUID.randomUUID().toString(), "Lê Hoàng C", "0933444555", "Gia đình"))
        contactList.add(Contact(UUID.randomUUID().toString(), "Phạm D", "0977888999", "Công việc"))
    }
}