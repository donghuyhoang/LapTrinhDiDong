package com.midterm.sv102240366

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ContactAdapter(var contactList: List<Contact>) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvPhone: TextView = itemView.findViewById(R.id.tvPhone)
        val btnCall: ImageButton = itemView.findViewById(R.id.btnCall)
        val btnSms: ImageButton = itemView.findViewById(R.id.btnSms)
        val tvLetterHeader: TextView = itemView.findViewById(R.id.tvLetterHeader)

        val cvAvatar: androidx.cardview.widget.CardView = itemView.findViewById(R.id.cvAvatar)

        val tvAvatarInitials: TextView = itemView.findViewById(R.id.tvAvatarInitials)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val currentContact = contactList[position]
        // Lấy chữ cái đầu tiên viết hoa
        val initials = currentContact.name.trim().firstOrNull()?.uppercaseChar()?.toString() ?: "?"
        holder.tvAvatarInitials.text = initials

        // Tạo danh sách màu
        val colors = listOf("#F44336", "#E91E63", "#9C27B0", "#673AB7", "#3F51B5", "#2196F3", "#009688", "#4CAF50", "#FF9800", "#795548")
        // Trộn màu dựa trên độ dài tên
        val colorIndex = kotlin.math.abs(currentContact.name.hashCode()) % colors.size
        holder.cvAvatar.setCardBackgroundColor(android.graphics.Color.parseColor(colors[colorIndex]))

        // Cập nhật Tên và Số điện thoại
        holder.tvName.text = currentContact.name
        holder.tvPhone.text = currentContact.phone

        // Mở màn hình Gọi điện
        holder.btnCall.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:${currentContact.phone}"))
            context.startActivity(intent)
        }

        // Mở màn hình Nhắn tin
        holder.btnSms.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:${currentContact.phone}"))
            context.startActivity(intent)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ContactDetailActivity::class.java)
            intent.putExtra("CONTACT_DATA", currentContact)
            context.startActivity(intent)
        }

        //  Giữ vào liên hệ để hiện thông báo Xóa
        holder.itemView.setOnLongClickListener {
            val context = holder.itemView.context
            val builder = android.app.AlertDialog.Builder(context)
            builder.setTitle("Xóa liên hệ")
            builder.setMessage("Bạn có chắc chắn muốn xóa ${currentContact.name} khỏi danh bạ?")

            // Nếu chọn Xóa
            builder.setPositiveButton("Xóa") { _, _ ->
                val positionToRemove = holder.adapterPosition
                if (positionToRemove != RecyclerView.NO_POSITION) {
                    // Lấy chính xác liên hệ đang chọn trên màn hình
                    val contactToRemove = contactList[positionToRemove]

                    // Xóa khỏi danh sách gốc dựa vào ID duy nhất
                    ContactRepository.contactList.removeAll { it.id == contactToRemove.id }

                    // Xóa khỏi danh sách hiển thị và cập nhật giao diện
                    val currentMutableList = contactList.toMutableList()
                    currentMutableList.removeAt(positionToRemove)
                    contactList = currentMutableList

                    notifyItemRemoved(positionToRemove)
                    notifyItemRangeChanged(positionToRemove, contactList.size)
                    android.widget.Toast.makeText(context, "Đã xóa ${contactToRemove.name}", android.widget.Toast.LENGTH_SHORT).show()
                }
            }

            // Nếu chọn Hủy
            builder.setNegativeButton("Hủy", null)
            builder.show()

            true // Trả về true để báo hệ thống đã xử lý xong cú nhấn giữ này
        }
        // Ánh xạ nút Yêu thích
        val btnFavorite = holder.itemView.findViewById<android.widget.ImageButton>(R.id.btnFavorite)

        // Cập nhật icon và màu sắc tùy theo trạng thái
        if (currentContact.isFavorite) {
            btnFavorite.setImageResource(android.R.drawable.star_on)
            btnFavorite.setColorFilter(android.graphics.Color.parseColor("#FFC107")) // Màu vàng
        } else {
            btnFavorite.setImageResource(android.R.drawable.star_off)
            btnFavorite.setColorFilter(android.graphics.Color.parseColor("#BDBDBD")) // Màu xám nhạt
        }

        // Xử lý khi bấm nút Yêu thích
        btnFavorite.setOnClickListener {
            currentContact.isFavorite = !currentContact.isFavorite
            notifyItemChanged(position) //Load lại item
        }
        // Gom nhóm chữ cái tiêu đề
        val currentLetter = getFirstLetter(currentContact.name)

        if (position == 0) {
            // Người đầu tiên của danh sách luôn hiển thị chữ cái
            holder.tvLetterHeader.visibility = android.view.View.VISIBLE
            holder.tvLetterHeader.text = currentLetter
        } else {
            val previousContact = contactList[position - 1]
            val previousLetter = getFirstLetter(previousContact.name)

            if (currentLetter != previousLetter) {
                holder.tvLetterHeader.visibility = android.view.View.VISIBLE
                holder.tvLetterHeader.text = currentLetter
            } else {
                holder.tvLetterHeader.visibility = android.view.View.GONE
            }
        }
    }

    // Đếm liên hệ
    override fun getItemCount(): Int {
        return contactList.size
    }

    // Làm mới danh sách
    fun updateList(newList: List<Contact>) {
        contactList = newList
        notifyDataSetChanged()
    }

    private fun getFirstLetter(name: String): String {
        val firstChar = name.trim().firstOrNull()?.uppercaseChar()
        // Gom nhóm chữ cái
        return if (firstChar != null && firstChar.isLetter()) {
            firstChar.toString()
        } else {
            "#"
        }
    }
}