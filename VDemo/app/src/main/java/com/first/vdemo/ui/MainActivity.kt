package com.first.vdemo.ui

import android.Manifest
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.first.vdemo.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

//请求权限码
const val REQUEST_PERMISSIONS = 9527
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //设置保持竖屏
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        //关联NavController与BottonNavigationView
        val navController = findNavController(R.id.nav_host)
        val bottomNavigation: BottomNavigationView =findViewById(R.id.bottomNavigation)
        bottomNavigation.setupWithNavController(navController)

        //请求权限
        requestPermission()
    }

    @AfterPermissionGranted(REQUEST_PERMISSIONS)
    private fun requestPermission() {
        val permissions = arrayOf(
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.READ_PHONE_STATE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        if (EasyPermissions.hasPermissions(this, *permissions)) {
            //有权限 开始定位
            Toast.makeText(this, "获取权限成功！！", Toast.LENGTH_SHORT).show()
        } else {
            //无权限
            EasyPermissions.requestPermissions(this, "需要权限", REQUEST_PERMISSIONS, *permissions)
        }
    }

    //请求权限结果
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        //设置权限请求结果
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this)
    }
}