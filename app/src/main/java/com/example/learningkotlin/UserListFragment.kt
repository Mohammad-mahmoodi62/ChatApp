package com.example.learningkotlin

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView

class UserListFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        //temp
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_user_list, container, false)
//        textview = findViewById<TextView>(R.id.textView)
        val foundUsersList = view.findViewById<RecyclerView>(R.id.found_users_list)

        val viewModel = ViewModelProvider(this).get(UsersViewModel::class.java)
        //tempt
        (activity as? MainActivity)?.test?.setUserViewModel(viewModel)

        val adapter = FoundUserAdapter { pos ->
            viewModel.onUserClicked(pos)
        }

        foundUsersList.adapter = adapter

        viewModel.userList.observe(this.viewLifecycleOwner , Observer {
            it?.let {
                adapter.submitList(it.toMutableList())

            }
        })

        //TODO: pass the user id of chatting
        viewModel.navigateToUer.observe(this.viewLifecycleOwner, Observer { position ->
            position?.let {
                val action = UserListFragmentDirections
                    .actionUserListFragmentToChatFragment(position)
                this.findNavController().navigate(action)
                viewModel.onUserNavigated()
            }
        })


//        val changeBtn = findViewById<Button>(R.id.changetext)
        val findBtn = view.findViewById<Button>(R.id.find_users)
        val debugButton = view.findViewById<Button>(R.id.debugger)
//        test.setTxtView(textview, this)
        // set on-click listener
//        changeBtn.setOnClickListener {
//            // your code to perform when the user clicks on the button
//            val userinput = findViewById<EditText>(R.id.userinput)
//            var userMsg:String = userinput.text.toString()
////            var msg = OwnedProject2(userMsg, "android")
////            test.sendMsg(msg as BaseMessage)
//            var msg = OwnedProject2(userMsg, "android")
////            val json = Json { encodeDefaults = true }
////            val serialized = json.encodeToString(msg as BaseMessage)
//            test.sendMsg(msg)
//        }
        findBtn.setOnClickListener {
            (this.activity as? MainActivity)?.test?.findUsers()
        }
        debugButton.setOnClickListener {
            BugRepoter.setBugReport(true)
        }
        return view
    }

}