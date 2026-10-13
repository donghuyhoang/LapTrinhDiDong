package com.midterm.sv102240366

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MainActivity : AppCompatActivity() {

    private lateinit var rvContacts: RecyclerView
    private lateinit var fabAdd: FloatingActionButton
    private lateinit var contactAdapter: ContactAdapter
    private var currentSearchQuery = ""
    private var currentGroupFilter = "Tất cả"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        rvContacts = findViewById(R.id.rvContacts)
        fabAdd = findViewById(R.id.fabAdd)
        val searchView = findViewById<SearchView>(R.id.searchView)
        val chipGroupFilter = findViewById<ChipGroup>(R.id.chipGroupFilter)

        rvContacts.layoutManager = LinearLayoutManager(this)
        rvContacts.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
                    imm.hideSoftInputFromWindow(recyclerView.windowToken, 0)
                    searchView.clearFocus()
                }
            }
        })
        contactAdapter = ContactAdapter(ContactRepository.contactList)
        rvContacts.adapter = contactAdapter

        fabAdd.setOnClickListener {
            val intent = Intent(this, ContactDetailActivity::class.java)
            startActivity(intent)
        }

        // Chức năng Tìm kiếm
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean = false

            override fun onQueryTextChange(newText: String?): Boolean {
                currentSearchQuery = newText ?: ""
                applyFilters()
                return true
            }
        })

        // Chức năng Bộ lọc nhóm
        chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val selectedChip = findViewById<Chip>(checkedIds[0])
                currentGroupFilter = selectedChip.text.toString()
                applyFilters()
            }
        }
        // Tính năng Vuốt item
        // Tính năng Vuốt item có màu nền và Icon
        val itemTouchHelper = androidx.recyclerview.widget.ItemTouchHelper(object : androidx.recyclerview.widget.ItemTouchHelper.SimpleCallback(0, androidx.recyclerview.widget.ItemTouchHelper.LEFT or androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
            override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean = false

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition

                val swipedContact = contactAdapter.contactList[position]

                if (direction == androidx.recyclerview.widget.ItemTouchHelper.LEFT) {
                    ContactRepository.contactList.removeAll { it.id == swipedContact.id }
                    applyFilters()

                    android.widget.Toast.makeText(this@MainActivity, "Đã xóa ${swipedContact.name}", android.widget.Toast.LENGTH_SHORT).show()
                } else if (direction == androidx.recyclerview.widget.ItemTouchHelper.RIGHT) {
                    contactAdapter.notifyItemChanged(position)
                    val intent = Intent(Intent.ACTION_DIAL, android.net.Uri.parse("tel:${swipedContact.phone}"))
                    startActivity(intent)
                }
            }

            // Vẽ màu nền và icon khi đang vuốt
            override fun onChildDraw(
                c: android.graphics.Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                val itemView = viewHolder.itemView
                val background = android.graphics.drawable.ColorDrawable()
                var icon: android.graphics.drawable.Drawable? = null

                if (dX > 0) {
                    // Vuốt sang phải: Nền Xanh lá, Icon Gọi điện
                    background.color = android.graphics.Color.parseColor("#4CAF50")
                    icon = androidx.core.content.ContextCompat.getDrawable(this@MainActivity, android.R.drawable.ic_menu_call)
                } else if (dX < 0) {
                    // Vuốt sang trái: Nền Đỏ, Icon Thùng rác
                    background.color = android.graphics.Color.parseColor("#F44336")
                    icon = androidx.core.content.ContextCompat.getDrawable(this@MainActivity, android.R.drawable.ic_menu_delete)
                }

                if (icon != null) {
                    // Vẽ khung màu nền
                    if (dX > 0) { // Sang phải
                        background.setBounds(itemView.left, itemView.top, itemView.left + dX.toInt(), itemView.bottom)
                    } else { // Sang trái
                        background.setBounds(itemView.right + dX.toInt(), itemView.top, itemView.right, itemView.bottom)
                    }
                    background.draw(c)

                    val iconMargin = (itemView.height - icon.intrinsicHeight) / 2
                    val iconTop = itemView.top + (itemView.height - icon.intrinsicHeight) / 2
                    val iconBottom = iconTop + icon.intrinsicHeight

                    if (dX > 0) {
                        val iconLeft = itemView.left + iconMargin
                        val iconRight = iconLeft + icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    } else if (dX < 0) {
                        val iconRight = itemView.right - iconMargin
                        val iconLeft = iconRight - icon.intrinsicWidth
                        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom)
                    }

                    icon.setTint(android.graphics.Color.WHITE)
                    icon.draw(c)
                }

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        })
        itemTouchHelper.attachToRecyclerView(rvContacts)
    }

    private fun applyFilters() {
        val filteredList = ContactRepository.contactList.filter { contact ->
            // Kiểm tra điều kiện nhóm
            // Kiểm tra điều kiện nhóm hoặc yêu thích
            val matchesGroup = when (currentGroupFilter) {
                "Tất cả" -> true
                "⭐ Yêu thích" -> contact.isFavorite
                else -> contact.group == currentGroupFilter
            }

            // Kiểm tra điều kiện từ khóa (chứa trong tên hoặc số điện thoại, không phân biệt hoa thường)
            val matchesSearch = contact.name.contains(currentSearchQuery, ignoreCase = true) ||
                    contact.phone.contains(currentSearchQuery)

            matchesGroup && matchesSearch
        }
        val vietnameseCollator = java.text.Collator.getInstance(java.util.Locale("vi", "VN"))

        // Sắp xếp danh sách dựa trên bộ từ điển này
        val sortedList = filteredList.sortedWith(compareBy(vietnameseCollator) { it.name.trim() })

        // Cập nhật giao diện với danh sách đã sắp xếp đúng
        contactAdapter.updateList(sortedList)

        // Bật tắt thông báo rỗng
        val tvEmpty = findViewById<android.widget.TextView>(R.id.tvEmpty)
        if (sortedList.isEmpty()) {
            rvContacts.visibility = android.view.View.GONE
            tvEmpty.visibility = android.view.View.VISIBLE
        } else {
            rvContacts.visibility = android.view.View.VISIBLE
            tvEmpty.visibility = android.view.View.GONE
        }
    }

    // Làm mới danh sách khi từ màn hình Thêm/Sửa quay về
    override fun onResume() {
        super.onResume()
        applyFilters()
    }
}