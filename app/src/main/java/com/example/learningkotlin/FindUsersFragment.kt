package com.example.learningkotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.skyfishjy.library.RippleBackground
import kotlin.concurrent.thread

class FindUsersFragment : Fragment() {

    private lateinit var rippleBackground:RippleBackground
    var secondsToShowAnimation = 0


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_find_users, container, false)

        this.updateToolBar()

        rippleBackground = view.findViewById(R.id.content)
        this.showAnimation()

        val viewModel = ViewModelProvider(this).get(FindUsersViewModel::class.java)
        (activity as MainActivity).test?.setFunAddFoundUser{
                user -> viewModel.addData(user)
        }

        val foundUserRecyclerView = view.findViewById<RecyclerView>(R.id.found_user_recyclerView)

        val foundUserAdapter = FoundUsersAdapter { user ->
            viewModel.onUserClicked(user)
        }
        foundUserRecyclerView.adapter = foundUserAdapter

        viewModel.foundUserList.observe(viewLifecycleOwner, Observer {
            it?.let {
                foundUserAdapter.submitList(it.toMutableList())
                this.secondsToShowAnimation += 1
            }
        })

        viewModel.onUserClicked.observe(this.viewLifecycleOwner, Observer { userId ->
            userId.let {
                if( userId!= null) {
                    this.findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "showWaitingDialog",
                        true
                    )
                    this.findNavController().popBackStack()

                    (activity as MainActivity).test?.connectToUser(userId)
                    viewModel.onUserNavigated()
                }
            }
        })

        (activity as MainActivity).test?.findUsers()

        return view
    }

    private fun showAnimation() {
        this.rippleBackground.startRippleAnimation()
        this.secondsToShowAnimation = 2
        thread {
            while (true) {
                this.secondsToShowAnimation -= 1
                Thread.sleep(1000)
                if(this.secondsToShowAnimation <= 0)
                    break
            }
            if(activity!= null)
                (activity as MainActivity).runOnUiThread {
                    this.rippleBackground.stopRippleAnimation()
                }
        }
    }

    private fun updateToolBar() {
        (activity as MainActivity).toolbarText.text = "Searching for Users"
        //TODO add Image as well
        (activity as MainActivity).toolbarImage.visibility = View.INVISIBLE
        (activity as MainActivity).supportActionBar?.title = ""
    }
}