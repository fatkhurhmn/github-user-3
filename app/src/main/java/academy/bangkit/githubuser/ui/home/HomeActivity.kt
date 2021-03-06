package academy.bangkit.githubuser.ui.home

import academy.bangkit.githubuser.R
import academy.bangkit.githubuser.adapter.UserAdapter
import academy.bangkit.githubuser.data.local.datastore.SettingPreferences
import academy.bangkit.githubuser.data.remote.response.UserResponse
import academy.bangkit.githubuser.databinding.ActivityHomeBinding
import academy.bangkit.githubuser.ui.detail.UserDetailActivity
import academy.bangkit.githubuser.ui.favorite.FavoriteActivity
import academy.bangkit.githubuser.utils.PreferencesViewModelFactory
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.preferencesDataStore
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.snackbar.Snackbar

private val Context.dataStore: DataStore<androidx.datastore.preferences.core.Preferences> by preferencesDataStore(
    name = "setting"
)

class HomeActivity : AppCompatActivity(), Toolbar.OnMenuItemClickListener {

    private lateinit var binding: ActivityHomeBinding
    private lateinit var homeViewModel: HomeViewModel
    private val userAdapter: UserAdapter by lazy { UserAdapter(true) }
    private var isDarkMode = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val pref = SettingPreferences.getInstance(dataStore)
        homeViewModel = ViewModelProvider(
            this,
            PreferencesViewModelFactory(pref)
        )[HomeViewModel::class.java]

        initToolbar()
        searchUser()
        initListUsers()
        showListUsers()
        navigateToUserDetail()
        hideToTypeIcon()
        showLoading()
        showNoResult()
        showError()
        showMessage()
        getThemeSetting()
    }

    private fun initToolbar() {
        binding.homeToolbar.setOnMenuItemClickListener(this)
    }

    private fun searchUser() {
        with(binding) {
            searchViewUser.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(username: String?): Boolean {
                    username?.let {
                        homeViewModel.setListUsers(username)
                    }
                    searchViewUser.clearFocus()
                    return true
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    return false
                }
            })
        }
    }

    private fun initListUsers() {
        homeViewModel.getListUsers().observe(this) { listUsers ->
            listUsers?.let {
                if (listUsers.isNotEmpty()) {
                    userAdapter.setUser(listUsers)
                }
            }
        }
    }

    private fun showListUsers() {
        with(binding.rvListUsers) {
            layoutManager = LinearLayoutManager(this@HomeActivity)
            adapter = userAdapter
            setHasFixedSize(true)
        }
    }

    private fun navigateToUserDetail() {
        userAdapter.setOnItemClickCallback(object : UserAdapter.OnItemClickCallback {
            override fun onItemClicked(user: UserResponse) {
                val userDetailIntent = Intent(this@HomeActivity, UserDetailActivity::class.java)
                userDetailIntent.putExtra(UserDetailActivity.EXTRA_USER, user)
                startActivity(userDetailIntent)
            }
        })
    }

    private fun hideToTypeIcon() {
        homeViewModel.isTyped.observe(this) { isLoading ->
            if (isLoading) {
                binding.tvHideToType.visibility = View.GONE
            }
        }
    }

    private fun showLoading() {
        homeViewModel.isLoading.observe(this) { isLoading ->
            with(binding) {
                if (isLoading) {
                    viewSearchLoading.root.visibility = View.VISIBLE
                    rvListUsers.visibility = View.GONE
                    viewNoResults.root.visibility = View.GONE
                    viewError.root.visibility = View.GONE
                } else {
                    viewSearchLoading.root.visibility = View.GONE
                    rvListUsers.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showNoResult() {
        homeViewModel.isNoResult.observe(this) { isNoResult ->
            with(binding) {
                if (isNoResult) {
                    viewNoResults.root.visibility = View.VISIBLE
                    rvListUsers.visibility = View.GONE
                } else {
                    viewNoResults.root.visibility = View.GONE
                }
            }
        }
    }

    private fun showError() {
        homeViewModel.isError.observe(this) { isError ->
            with(binding) {
                if (isError) {
                    viewError.root.visibility = View.VISIBLE
                    viewSearchLoading.root.visibility = View.GONE
                    rvListUsers.visibility = View.GONE
                } else {
                    viewError.root.visibility = View.GONE
                }
            }
        }
    }

    private fun showMessage() {
        homeViewModel.message.observe(this) {
            it.getContentIfNotHandled()?.let { message ->
                Snackbar.make(binding.homeContainer, message, Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMenuItemClick(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.btn_favorite -> {
                val favoriteIntent = Intent(this, FavoriteActivity::class.java)
                startActivity(favoriteIntent)
                true
            }

            R.id.btn_mode -> {
                homeViewModel.saveThemeSetting(isDarkMode)
                true
            }

            else -> false
        }
    }

    private fun getThemeSetting() {
        homeViewModel.getThemeSetting().observe(this) { isDarkMode ->
            if (isDarkMode) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
                binding.homeToolbar.menu.getItem(1).setIcon(R.drawable.ic_dark_mode)
                this.isDarkMode = false
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
                binding.homeToolbar.menu.getItem(1).setIcon(R.drawable.ic_light_mode)
                this.isDarkMode = true
            }
        }
    }
}