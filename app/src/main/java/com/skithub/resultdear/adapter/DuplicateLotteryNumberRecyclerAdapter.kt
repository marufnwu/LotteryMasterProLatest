package com.skithub.resultdear.adapter

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.skithub.resultdear.R
import com.skithub.resultdear.databinding.DuplicateLotteryNumberRecyclerViewModelLayoutBinding
import com.skithub.resultdear.model.LotteryNumberModel
import com.skithub.resultdear.ui.common_number_details.CommonNumberDetailsActivity
import com.skithub.resultdear.ui.last_digit_first_prize.LastDigitFirstPrizeActivity
import com.skithub.resultdear.ui.last_digit_first_prize.LastDigitFirstPrizeDetailsActivity
import com.skithub.resultdear.ui.middle_details.MiddleDetailsActivity
import com.skithub.resultdear.ui.middle_details.OneStMiddleDetailsActivity
import com.skithub.resultdear.ui.middle_details.TwoNDmiddleDetailsActivity
import com.skithub.resultdear.ui.middle_number.MiddleNumberActivity
import com.skithub.resultdear.ui.middle_part.MiddlePartActivity
import com.skithub.resultdear.ui.middle_part.MiddlePartDetailsActivity
import com.skithub.resultdear.ui.middle_part.MiddleSerialActivity
import com.skithub.resultdear.ui.middle_play_less.PlaylessActivity
import com.skithub.resultdear.ui.middle_plays_more_days.MiddlePlaysMoreByDaysActivity
import com.skithub.resultdear.ui.number_not_plays.NotPlayedNumActivity
import com.skithub.resultdear.ui.tow_nd_middle_plays_more.OneStMiddleNumberActivity
import com.skithub.resultdear.ui.tow_nd_middle_plays_more.TwoNdMiddlePlaysMoreActivity
import com.skithub.resultdear.utils.Constants
import com.skithub.resultdear.utils.MyExtensions.shortToast

class DuplicateLotteryNumberRecyclerAdapter(val context: Context, val list: MutableList<LotteryNumberModel>): RecyclerView.Adapter<DuplicateLotteryNumberRecyclerAdapter.DuplicateLotteryNumberRecyclerViewHolder>() {

    private var middle : String? = null

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DuplicateLotteryNumberRecyclerViewHolder {
        val binding= DuplicateLotteryNumberRecyclerViewModelLayoutBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return DuplicateLotteryNumberRecyclerViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DuplicateLotteryNumberRecyclerViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class DuplicateLotteryNumberRecyclerViewHolder(val binding: DuplicateLotteryNumberRecyclerViewModelLayoutBinding): RecyclerView.ViewHolder(binding.root),
        View.OnClickListener {

        @SuppressLint("SetTextI18n")
        fun bind(item: LotteryNumberModel) {
            try {

                if (context is MiddlePartActivity) {
                    binding.lotteryNumberCount.visibility = View.VISIBLE
                }else{
                    binding.lotteryNumberCount.visibility = View.GONE
                }




                if (context is MiddleNumberActivity || context is MiddlePlaysMoreByDaysActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                }else if (context is LastDigitFirstPrizeActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber!!.takeLast(1)
                }else if (context is MiddleSerialActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                }else if (context is com.skithub.resultdear.ui.number_not_plays.MiddleSerialActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                }else if (context is MiddlePartDetailsActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                }else if (context is MiddlePartActivity) {
                    val rangeWithCount = item.lotteryNumber
                    val list =rangeWithCount?.split(",")!!

                    binding.lotteryNumberCount.text= list[0]
                    binding.lotteryNumberTextView.text= list[1]
                    binding.viewDetailsTextView.text= list[2]

                }else if (context is PlaylessActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                }else if (context is TwoNdMiddlePlaysMoreActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                }else if (context is OneStMiddleNumberActivity) {
                    binding.lotteryNumberTextView.text= item.lotteryNumber
                } else {
                    binding.lotteryNumberTextView.text="${item.lotterySerialNumber} ${item.lotteryNumber}"
                }
                binding.duplicateLotteryNumberRootLayout.setOnClickListener(this)
            } catch (e: Exception) {}
        }

        override fun onClick(v: View?) {
            v?.let {
                val lotteryIntent: Intent?
                if (context is MiddleNumberActivity || context is MiddlePlaysMoreByDaysActivity) {
                    lotteryIntent = Intent(context,MiddleDetailsActivity::class.java)
                }else if(context is PlaylessActivity){
                    lotteryIntent = Intent(context,MiddleDetailsActivity::class.java)
                }else if(context is com.skithub.resultdear.ui.number_not_plays.MiddleSerialActivity){
                    lotteryIntent = Intent(context,NotPlayedNumActivity::class.java).putExtra("middle", list.get(absoluteAdapterPosition).lotteryNumber)
                }else if(context is MiddlePartActivity){

                    val item = list.get(absoluteAdapterPosition)

                    val rangeWithCount = item.lotteryNumber
                    val list =rangeWithCount?.split(",")!!

                    lotteryIntent = if(getMiddle()!=null){
                        val middle = getMiddle()!!
                        val range = list[1]

                        Intent(context,MiddlePartDetailsActivity::class.java).putExtra("middle", middle).putExtra("range", range)
                    }else{
                        null
                    }


                }else if(context is TwoNdMiddlePlaysMoreActivity){
                    lotteryIntent = Intent(context,TwoNDmiddleDetailsActivity::class.java)
                }else if(context is OneStMiddleNumberActivity){
                    lotteryIntent = Intent(context,OneStMiddleDetailsActivity::class.java)
                }else if(context is LastDigitFirstPrizeActivity){
                    lotteryIntent = Intent(context, LastDigitFirstPrizeDetailsActivity::class.java)
                }else if(context is MiddleSerialActivity){
                    lotteryIntent = Intent(context, MiddlePartActivity::class.java)
                }else {
                     lotteryIntent = Intent(context,CommonNumberDetailsActivity::class.java)
                }

                when (it.id) {
                    R.id.duplicateLotteryNumberRootLayout -> {
                       if(lotteryIntent!=null){
                           lotteryIntent.putExtra(Constants.lotteryNumberKey,list[adapterPosition].lotteryNumber)
                           (context as Activity).startActivity(lotteryIntent)
                       }

                    }
                }
            }
        }
    }


    fun setMiddle(middle : String){
        this.middle = middle
    }

    private fun getMiddle():String?{
        return this.middle
    }



}