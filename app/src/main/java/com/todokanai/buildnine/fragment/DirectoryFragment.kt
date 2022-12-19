package com.todokanai.buildnine.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.todokanai.buildnine.R
import com.todokanai.buildnine.adapter.DirectoryRecyclerAdapter
import com.todokanai.buildnine.application.MyApplication
import com.todokanai.buildnine.databinding.FragmentDirectoryBinding
import com.todokanai.buildnine.room.MyDatabase
import com.todokanai.buildnine.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DirectoryFragment : Fragment() {

    lateinit var binding: FragmentDirectoryBinding
    private val viewModel: SettingsViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {

        val adapter = DirectoryRecyclerAdapter()
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_directory, container, false)
        binding.directoryRecyclerView.adapter = adapter
        binding.directoryRecyclerView.layoutManager = LinearLayoutManager(context)


        //--------------------

        val myDatabase = MyDatabase.getInstance(MyApplication.appContext)

        lifecycleScope.launch(Dispatchers.IO) {
            adapter.mDirectoryList.addAll(myDatabase.roomPathDao().getAll())
        }
        //----------------------
        // 코루틴 성공 샘플?




        // Inflate the layout for this fragment
        binding.swipe.setOnRefreshListener {
            adapter.notifyDataSetChanged()
            binding.swipe.isRefreshing = false          //------swipe 해서 목록 새로고침
        }
        return binding.root
    }
}