package com.example.qualwebs_chatapp_assignment.ui

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Typeface
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.example.qualwebs_chatapp_assignment.R
import com.example.qualwebs_chatapp_assignment.databinding.AllChatsSingleRowBinding
import com.example.qualwebs_chatapp_assignment.models.ChatPreview

class AllChatsAdapter(
    private val onChatClick: (ChatPreview) -> Unit,
    private var chatList: List<ChatPreview>,
    private var userID: String?
) : RecyclerView.Adapter<AllChatsAdapter.AllChatsViewHolder>() {
    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AllChatsAdapter.AllChatsViewHolder {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        val binding= AllChatsSingleRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return AllChatsViewHolder(binding,userID)

     }

    override fun onBindViewHolder(holder: AllChatsAdapter.AllChatsViewHolder, position: Int) {
        holder.bind(chatList[position])
     }

    override fun getItemCount(): Int {
        return chatList.size;
     }

    @SuppressLint("NotifyDataSetChanged")
    fun updateChats(newChats: List<ChatPreview>?) {
        if(newChats!=null){

        chatList = newChats.sortedByDescending { it.timestamp }
        notifyDataSetChanged()
        }
//        chatList = newChats
    }


    inner class AllChatsViewHolder(val binding: AllChatsSingleRowBinding, var userID: String?) : RecyclerView.ViewHolder(binding.root)
    {
        fun bind(chat: ChatPreview){

            binding.root.setOnClickListener {
                onChatClick(chat)
            }

            binding.userName.text=chat.name
            if(chat.lastMessage.isEmpty()){
                binding.lastMessage.text="Photo"

            }else{
                binding.lastMessage.text=chat.lastMessage
            }

            binding.profileImage.setImageResource(R.drawable.profiledummysvg)

            if(chat.profilePic!="" || chat.profilePic.isNotEmpty()){
                binding.profileImage.setImageBitmap(base64ToBitmap((chat.profilePic)))
            }

            if (!chat.seen) {
                binding.lastMessage.setTypeface(null, Typeface.BOLD)
                binding.lastMessage.setTextColor(Color.BLACK)
                binding.blueDot.visibility=View.VISIBLE
            } else {
                binding.lastMessage.setTypeface(null, Typeface.NORMAL)
                binding.lastMessage.setTextColor(Color.GRAY)
                binding.blueDot.visibility=View.GONE

            }

//            binding.profileImage.loadImage=chat.profilePic

        }
    }
    fun base64ToBitmap(base64String: String?): Bitmap? {
        return try {
            val decodedBytes = Base64.decode(base64String, Base64.DEFAULT)
            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            null
        }
    }


}