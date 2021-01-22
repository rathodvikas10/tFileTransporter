package com.tans.tfiletransporter.ui.activity.filetransport

import android.Manifest
import android.os.Build
import android.os.Bundle
import com.google.android.material.tabs.TabLayout
import com.jakewharton.rxbinding3.view.clicks
import com.tans.tfiletransporter.R
import com.tans.tfiletransporter.databinding.FileTransportActivityBinding
import com.tans.tfiletransporter.ui.activity.BaseActivity
import com.tbruyelle.rxpermissions2.RxPermissions
import kotlinx.coroutines.launch
import kotlinx.coroutines.rx2.await
import org.kodein.di.*
import org.kodein.di.android.di
import org.kodein.di.android.retainedSubDI

class FileTransportActivity : BaseActivity<FileTransportActivityBinding, FileTransportActivityState>(R.layout.file_transport_activity, FileTransportActivityState()) {

    override val di: DI by retainedSubDI(di()) {
        // bind<FileTransportScopeData>() with scoped(AndroidLifecycleScope).singleton { FileTransportScopeData() }
        bind<FileTransportScopeData>() with singleton { FileTransportScopeData() }
    }
    private val fileTransportScopeData by instance<FileTransportScopeData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        launch {
            binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab?) {
                    when (tab?.position) {
                        DirTabType.MyDir.ordinal -> updateStateCompletable { it.copy(DirTabType.MyDir) }.bindLife()
                        DirTabType.RemoteDir.ordinal -> updateStateCompletable { it.copy(DirTabType.RemoteDir) }.bindLife()
                    }
                }

                override fun onTabUnselected(tab: TabLayout.Tab?) {
                }

                override fun onTabReselected(tab: TabLayout.Tab?) {
                }
            })

            binding.floatingActionBt.clicks()
                .doOnNext { fileTransportScopeData.floatBtnEvent.onNext(Unit) }
                .bindLife()


            render({ it.selectedTabType }) {
                binding.floatingActionBt.setImageResource(
                    when (it) {
                        DirTabType.MyDir -> R.drawable.share_variant_outline
                        DirTabType.RemoteDir -> R.drawable.download_outline
                    }
                )
                changeDirFragment(it)
            }.bindLife()
        }

    }

    fun changeDirFragment(dirTabType: DirTabType) {
        val transaction = supportFragmentManager.beginTransaction()
        when (dirTabType) {
            DirTabType.MyDir -> {
                var fragment = supportFragmentManager.findFragmentByTag(MyDirFragment.FRAGMENT_TAG)
                if (fragment == null) {
                    fragment = MyDirFragment()
                    transaction.add(R.id.fragment_container_layout, fragment, MyDirFragment.FRAGMENT_TAG)
                } else {
                    transaction.show(fragment)
                }
                for (f in supportFragmentManager.fragments) {
                    if (f != fragment) { transaction.hide(f) }
                }
            }

            DirTabType.RemoteDir -> {
                var fragment = supportFragmentManager.findFragmentByTag(RemoteDirFragment.FRAGMENT_TAG)
                if (fragment == null) {
                    fragment = RemoteDirFragment()
                    transaction.add(R.id.fragment_container_layout, fragment, RemoteDirFragment.FRAGMENT_TAG)
                } else {
                    transaction.show(fragment)
                }
                for (f in supportFragmentManager.fragments) {
                    if (f != fragment) { transaction.hide(f) }
                }
            }
        }
        transaction.commitAllowingStateLoss()
    }

}

data class FileTransportActivityState(
    val selectedTabType: DirTabType = DirTabType.MyDir
)

enum class DirTabType { MyDir, RemoteDir }