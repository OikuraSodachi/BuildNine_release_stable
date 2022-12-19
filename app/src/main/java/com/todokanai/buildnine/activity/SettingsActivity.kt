package com.todokanai.buildnine.activity

import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.todokanai.buildnine.R
import com.todokanai.buildnine.databinding.ActivitySettingsBinding
import com.todokanai.buildnine.viewmodel.SettingsViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SettingsActivity : AppCompatActivity() {

    lateinit var binding: ActivitySettingsBinding
    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_settings)

        val backBtn = findViewById<ImageButton>(R.id.Backbtn)
        val pathBtn = findViewById<Button>(R.id.scanFolderButton)
        val scanBtn = findViewById<Button>(R.id.Scanbtn)


        backBtn.setOnClickListener { startActivity(viewModel.intentmain)} //Backbtn에 대한 동작
        scanBtn.setOnClickListener{ viewModel.scan() }          // scan버튼 난타하면 리스트 사이즈 증가 issue. 작업 완료시까지 버튼 disable로 해결?
        pathBtn.setOnClickListener { viewModel.setPath(binding.mPathInput.text)  }
    }
}