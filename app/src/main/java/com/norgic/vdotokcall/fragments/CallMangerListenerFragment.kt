package com.norgic.vdotokcall.fragments

import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import com.norgic.vdotokcall.feature.dashBoard.ui.DashBoardActivity
import com.norgic.vdotokcall.interfaces.FragmentRefreshListener


/**
 * Created By: Norgic
 * Date & Time: On 5/26/21 At 3:21 PM in 2021
 */
abstract class CallMangerListenerFragment: Fragment(), FragmentRefreshListener {

    override fun onStart() {
        (activity as DashBoardActivity).mListener = this
        super.onStart()
    }

//    override fun onViewStateRestored(savedInstanceState: Bundle?) {
//        super.onViewStateRestored(savedInstanceState)
//        (activity as DashBoardActivity).mListener = this
//    }
//
//    override fun onAttach(activity: Activity) {
//        super.onAttach(activity)
//        (activity as DashBoardActivity).mListener = this
//    }
}