package com.example.qualwebs_chatapp_assignment.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.RecyclerView
import com.example.qualwebs_chatapp_assignment.databinding.ReceivedImgMsgRowBinding
import com.example.qualwebs_chatapp_assignment.databinding.ReceivedMsgRowBinding
import com.example.qualwebs_chatapp_assignment.databinding.SentImgMsgRowBinding
import com.example.qualwebs_chatapp_assignment.databinding.SentMsgRowBinding
import com.example.qualwebs_chatapp_assignment.models.ChatMessage
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ChatAdapter(private val messages: List<ChatMessage> ,
                  private val currentUid: String,
                  private val onEditMessage: (ChatMessage,Int) -> Unit,
                  private val onDeleteMessage: (ChatMessage) -> Unit,
                  private val receiverBitmap: Bitmap?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

        private val MESSAGE_SENT =1
        private val MESSAGE_RECEIVED=2
        private val IMG_MESSAGE_SENT=3
        private val IMG_MESSAGE_RECEIVED=4

    override fun getItemViewType(position: Int): Int {
        val message = messages[position]

        return when
        {
            message.imageMsg!= "" ->{
                if(message.senderId==currentUid) IMG_MESSAGE_SENT else IMG_MESSAGE_RECEIVED
            }
            message.senderId ==currentUid -> MESSAGE_SENT else -> MESSAGE_RECEIVED
        }

//        return if (messages[position].senderId == currentUid) MESSAGE_SENT else MESSAGE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        return when(viewType){
            MESSAGE_SENT ->{
                val binding =SentMsgRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                SentMessageViewHolder(binding)
            }
            MESSAGE_RECEIVED ->{
                val binding = ReceivedMsgRowBinding.inflate(LayoutInflater.from(parent.context), parent, false)
                ReceivedMessageViewHolder(binding)
            }
            IMG_MESSAGE_SENT -> {
                val binding = SentImgMsgRowBinding.inflate(LayoutInflater.from(parent.context),parent ,false)
                SentImageMsgViewHolder(binding)

            }
            IMG_MESSAGE_RECEIVED ->{
                val binding = ReceivedImgMsgRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
                ReceivedImageMsgViewHolder(binding)
            }
            else-> throw IllegalArgumentException("Invalid view type")

        }





//        return if (viewType==MESSAGE_SENT){
//            val binding = SentMsgRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//            SentMessageViewHolder(binding)
//        }else{
//            val binding = ReceivedMsgRowBinding.inflate(LayoutInflater.from(parent.context),parent,false)
//            ReceivedMessageViewHolder(binding)
//        }
    }

    override fun getItemCount(): Int {
        return messages.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
            val message = messages[position]

            when(holder){
                is SentMessageViewHolder -> holder.bind(message,position)
                is ReceivedMessageViewHolder -> holder.bind(message)
                is SentImageMsgViewHolder -> holder.bind(message,position)
                is ReceivedImageMsgViewHolder -> holder.bind(message)

            }
    }

    inner class SentMessageViewHolder(private val binding: SentMsgRowBinding)
        :  RecyclerView.ViewHolder(binding.root)
    {
            fun bind(message : ChatMessage,position: Int){
                binding.messageTextSent.text=message.message
                binding.timestampSent.text=formatTimestamp(message.timestamp)

                binding.root.setOnLongClickListener {
                    showOptionsDialog(message,binding.root.context,position)
                    true
                }

            }

    }

    inner class ReceivedMessageViewHolder(private val binding: ReceivedMsgRowBinding)
        :  RecyclerView.ViewHolder(binding.root)
    {
        fun bind(message : ChatMessage){
            binding.messageTextReceived.text=message.message
            binding.timestampReceived.text=formatTimestamp(message.timestamp)
            binding.profileImage.setImageBitmap(receiverBitmap)
        }
     }



    inner class SentImageMsgViewHolder(private val binding: SentImgMsgRowBinding) : RecyclerView.ViewHolder(binding.root){
        fun bind(message: ChatMessage,position: Int){
            binding.timestampSender.text = formatTimestamp(message.timestamp)
            message.imageMsg?.let {
                val decodeBitmap = decodeBase64(it)
                binding.chatImageViewSender.setImageBitmap(decodeBitmap)

                binding.root.setOnLongClickListener {
                    showOptionsDialog(message,binding.root.context,position)
                    true
                }


            }
        }

    }

    inner class ReceivedImageMsgViewHolder(private val binding : ReceivedImgMsgRowBinding
    ): RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage){
            binding.timestampReceiver.text=formatTimestamp(message.timestamp)
            message.imageMsg?.let{
                val decodeBitmap = decodeBase64(it)
                binding.chatImageViewReceiver.setImageBitmap(decodeBitmap)
            }

        }
    }



    private fun showOptionsDialog(message: ChatMessage , context: Context,position: Int) {
        val options = arrayOf("Edit", "Delete")
        AlertDialog.Builder(context)
            .setItems(options) { _, which ->
                when (which) {
                    0 -> onEditMessage(message,position)
                    1 -> onDeleteMessage(message)
                }
            }
            .show()
    }


    private fun formatTimestamp(timestamp: Long): String {
        val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
        return sdf.format(Date(timestamp))
    }

    private fun decodeBase64(base64Str: String): Bitmap {
        val decodedBytes = Base64.decode(base64Str, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
    }

}