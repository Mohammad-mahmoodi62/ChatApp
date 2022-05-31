package com.example.learningkotlin

import android.os.Bundle
import android.util.Patterns
import android.view.*
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.siddydevelops.customlottiedialogbox.CustomLottieDialog
import toan.android.floatingactionmenu.FloatingActionButton
import java.lang.String

class UserListFragment : Fragment() {

    private lateinit var _findUsersBtn: FloatingActionButton
    private lateinit var _connectByIpBtn: FloatingActionButton
    private lateinit var _customLottieDialog: CustomLottieDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        this.setHasOptionsMenu(true)



        val view = inflater.inflate(R.layout.fragment_user_list, container, false)

    this.updateToolBAr()

    val navController = findNavController();
    navController.currentBackStackEntry?.savedStateHandle
        ?.getLiveData<Boolean>("showWaitingDialog")?.observe(
        viewLifecycleOwner) { result ->
        if(result)
        {
            this.showWaitingDialog()
            navController.currentBackStackEntry?.savedStateHandle
                ?.getLiveData<Boolean>("showWaitingDialog")?.value = false
        }
    }



        val chatUserRecyclerView = view.findViewById<RecyclerView>(R.id.chat_user_recyclerView)
        this._findUsersBtn = view.findViewById(R.id.find_users_btn)
        this._connectByIpBtn = view.findViewById(R.id.connect_ip_btn)

        val viewModel = ViewModelProvider(this).get(UsersViewModel::class.java)
        //tempt
        (activity as? MainActivity)?.test?.setUserViewModel(viewModel)

        val adapter = ChatUserAdapter { UserID ->
            viewModel.onUserClicked(UserID)
        }

        chatUserRecyclerView.adapter = adapter

        viewModel.userList.observe(this.viewLifecycleOwner , Observer {
            it?.let {
                adapter.submitList(it.toMutableList())
                this.dismissWaitingDialog()
            }
        })


        viewModel.navigateToUer.observe(this.viewLifecycleOwner, Observer { UserID ->
            UserID?.let {
                val action = UserListFragmentDirections
                    .actionUserListFragmentToChatFragment(UserID)
                this.findNavController().navigate(action)
                viewModel.onUserNavigated()
            }
        })

        _findUsersBtn.setOnClickListener {
//            (activity as MainActivity).test?.findUsers()
            val action = UserListFragmentDirections
                .actionUserListFragmentToFindUsersFragment()
            this.findNavController().navigate(action)
        }

        _connectByIpBtn.setOnClickListener {
            this.showConnectByIpDialog()
        }

        return view
    }

    private fun showWaitingDialog() {
        this._customLottieDialog = CustomLottieDialog(this.context, "LO04")
        val newColor = context?.applicationContext?.resources?.getColor(R.color.main_color)
        this._customLottieDialog.setLottieBackgroundColor(
            String.format(
                "#%06X",
                0xFFFFFF and newColor!!
            ))

        this._customLottieDialog.setLoadingText("Connecting...")
        this._customLottieDialog.show()
    }

    private fun dismissWaitingDialog() {
        if(this::_customLottieDialog.isInitialized)
            this._customLottieDialog.dismiss()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.ip_menu, menu)
        menu.findItem(R.id.show_ip).title = "IP: ${(activity as MainActivity).test?._udpSocket?.selfIP}"
    }


    private fun updateToolBAr() {
        (activity as MainActivity).toolbarText.text = ""
        //TODO add Image as well
        (activity as MainActivity).toolbarImage.visibility = View.INVISIBLE
        (activity as MainActivity).supportActionBar?.title = resources.getString(R.string.app_name)
    }

    private fun showConnectByIpDialog() {
        val dialogView = LayoutInflater.from(this.context).inflate(R.layout.connect_by_ip_layout, null)

        val builder = AlertDialog.Builder(this.requireContext())
            .setView(dialogView)
        val mAlertDialog = builder.show()


        val connectBtn = dialogView.findViewById<Button>(R.id.connect_target_device)
        val enteredIP = dialogView.findViewById<EditText>(R.id.entered_ip)
        connectBtn.setOnClickListener {
            if(validateIp(enteredIP.text.toString())) {
                (activity as MainActivity).test?.connectToUser(enteredIP.text.toString())
                this.showWaitingDialog()
            }
            mAlertDialog.dismiss()
        }
    }

    private fun validateIp(ip: kotlin.String): Boolean {
        return if(!Patterns.IP_ADDRESS.matcher(ip).matches()) {
            Toast.makeText(this.context, "Entered IP is Wrong", Toast.LENGTH_SHORT).show()
            false
        } else true
    }
}
