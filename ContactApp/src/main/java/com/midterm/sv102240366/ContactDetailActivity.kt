package com.midterm.sv102240366

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.util.UUID

class ContactDetailActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_detail)
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        // Ánh xạ
        val edtName = findViewById<EditText>(R.id.edtName)
        val edtPhone = findViewById<EditText>(R.id.edtPhone)
        val edtNote = findViewById<EditText>(R.id.edtNote)
        val rgGroup = findViewById<RadioGroup>(R.id.rgGroup)
        val rbFamily = findViewById<RadioButton>(R.id.rbFamily)
        val rbWork = findViewById<RadioButton>(R.id.rbWork)
        val rbFriends = findViewById<RadioButton>(R.id.rbFriends)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnCancel = findViewById<Button>(R.id.btnCancel)

        // Bật nút mũi tên quay lại trên thanh tiêu đề
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        // Nút huỷ
        btnCancel.setOnClickListener {
            finish() // Đóng form và không lưu gì cả
        }

        // Check xem đang ở thêm hay sửa
        @Suppress("DEPRECATION")
        val contactToEdit = intent.getSerializableExtra("CONTACT_DATA") as? Contact

        if (contactToEdit != null) {
            // --- CHẾ ĐỘ SỬA ---
            supportActionBar?.title = "Sửa Liên Hệ"

            // Đổ dữ liệu cũ lên màn hình
            edtName.setText(contactToEdit.name)
            edtPhone.setText(contactToEdit.phone)
            edtNote.setText(contactToEdit.note) // Hiển thị ghi chú cũ

            when (contactToEdit.group) {
                "Gia đình" -> rbFamily.isChecked = true
                "Công việc" -> rbWork.isChecked = true
                "Bạn bè" -> rbFriends.isChecked = true
            }
        } else {
            supportActionBar?.title = "Thêm Liên Hệ"
        }

        // Nút Lưu
        btnSave.setOnClickListener {
            val name = edtName.text.toString().trim()
            val phone = edtPhone.text.toString().trim()
            val note = edtNote.text.toString().trim()

            // Bắt buộc nhập tên và số điện thoại
            if (name.isEmpty() || phone.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập đủ Tên và Số điện thoại", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Lấy nhóm đang được chọn
            val selectedGroupId = rgGroup.checkedRadioButtonId
            val group = findViewById<RadioButton>(selectedGroupId).text.toString()

            if (contactToEdit != null) {
                val existingContact = ContactRepository.contactList.find { it.id == contactToEdit.id }
                if (existingContact != null) {
                    existingContact.name = name
                    existingContact.phone = phone
                    existingContact.group = group
                    existingContact.note = note
                }
                Toast.makeText(this, "Đã cập nhật thành công!", Toast.LENGTH_SHORT).show()
            } else {
                val newContact = Contact(UUID.randomUUID().toString(), name, phone, group, note)
                ContactRepository.contactList.add(newContact)
                Toast.makeText(this, "Đã lưu liên hệ mới!", Toast.LENGTH_SHORT).show()
            }

            finish()
        }
    }
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}