package com.vdotok.one2one.feature.dashBoard.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.vdotok.network.models.UserModel
import com.vdotok.one2one.R
import com.vdotok.one2one.databinding.UserRowBinding


class AllUserListAdapter(private val list: List<UserModel>, private val callbacks: OnItemClickCallback):
    RecyclerView.Adapter<AllUserListViewHolder>(), Filterable {
    var selection = false
    var dataList: ArrayList<UserModel> = ArrayList()
    var filteredItems: ArrayList<UserModel> = ArrayList()
    init {
        dataList.addAll(list)
        filteredItems.addAll(list)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllUserListViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return AllUserListViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: AllUserListViewHolder, position: Int) {
        val listData = dataList[position]
        holder.binding?.groupModel = listData

        holder.binding?.imgAudioCall?.setOnClickListener {
            callbacks.audioCall(position)
//            selection = true
        }

        holder.binding?.imgVideoCall?.setOnClickListener {
            callbacks.videoCall(position)
//            selection = true
        }
//        if (listData.isSelected){
//            holder.binding?.imgUserSelected?.visibility = View.VISIBLE
//        } else {
//            selection = false
//            holder.binding?.imgUserSelected?.visibility = View.GONE
//        }


    }

    override fun getItemCount(): Int = dataList.size

    fun updateData(userModelList: List<UserModel>) {
        dataList.clear()
        dataList.addAll(userModelList)
        filteredItems.clear()
        filteredItems.addAll(userModelList)
        notifyDataSetChanged()

    }

    override fun getFilter(): Filter? {
        return object : Filter() {
            override fun performFiltering(charSequence: CharSequence): FilterResults {
                val charString = charSequence.toString()
                if (charString.isEmpty()) {
                    dataList = filteredItems
                } else {
                    val filteredList: ArrayList<UserModel> = ArrayList()
                    for (row in filteredItems) {
                        if (row.fullName?.split(" ")?.first()?.toLowerCase()?.startsWith(charString.toLowerCase()) == true
                            || row.fullName?.split(" ")?.last()?.toLowerCase()?.startsWith(charString.toLowerCase()) == true) {
                            filteredList.add(row)
                        }
                    }
                    dataList = filteredList
                }
                val filterResults = FilterResults()
                filterResults.values = dataList
                return filterResults
            }

            override fun publishResults(charSequence: CharSequence, filterResults: FilterResults) {
                dataList = filterResults.values as ArrayList<UserModel>
                callbacks.searchResult(dataList.size)
                notifyDataSetChanged()
            }
        }
    }

    fun getSelectedUsers(): List<UserModel> {
        val users: ArrayList<UserModel> = ArrayList()

        for(user in dataList){
            when {
                user.isSelected -> users.add(user)
            }
        }
        return users
    }
}

class AllUserListViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.user_row, parent, false)) {
    var binding: UserRowBinding? = null
    init {
        binding = DataBindingUtil.bind(itemView)
    }
}

interface OnItemClickCallback {
    fun audioCall(position: Int)
    fun videoCall(position: Int)
    fun searchResult(position: Int)
}