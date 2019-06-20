package com.example.thechat

import android.app.Activity
import android.content.Context
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide

class MessageAdapter(context: Context,resource: Int, messages:List<Message>) :
    ArrayAdapter<Message>(context,resource,messages) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View? {
        var convertView = convertView
        if (convertView == null) {
            convertView = (context as Activity).layoutInflater.inflate(R.layout.message_item,parent,false)
        } else {

            val message = getItem(position)

            val imageView = convertView.findViewById<ImageView>(R.id.imageView)
            val messageText = convertView.findViewById<TextView>(R.id.messageText)
            val authorText = convertView.findViewById<TextView>(R.id.authorName)

            if (message.photoUrl != null) {
                messageText.visibility = View.GONE
                messageText.visibility = View.VISIBLE
                Glide.with(imageView!!.context).load(message.photoUrl).into(imageView)
            } else {

                messageText.visibility = View.VISIBLE
                imageView?.visibility = View.GONE
                messageText.text = message.text
            }
            authorText.text = message.name
            return convertView
        }
        return convertView
    }
}