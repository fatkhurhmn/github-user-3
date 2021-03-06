package academy.bangkit.githubuser.ui.home

import academy.bangkit.githubuser.BuildConfig
import academy.bangkit.githubuser.data.local.datastore.SettingPreferences
import academy.bangkit.githubuser.data.remote.response.SearchResponse
import academy.bangkit.githubuser.data.remote.response.UserResponse
import academy.bangkit.githubuser.data.remote.retrofit.ApiConfig
import academy.bangkit.githubuser.utils.Event
import androidx.lifecycle.*
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel(private val pref: SettingPreferences) : ViewModel() {

    private val tokenApi = BuildConfig.API_KEY
    private val listUsers = MutableLiveData<ArrayList<UserResponse>>()

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> get() = _isLoading

    private val _isError = MutableLiveData<Boolean>()
    val isError: LiveData<Boolean> get() = _isError

    private val _isNoResult = MutableLiveData<Boolean>()
    val isNoResult: LiveData<Boolean> get() = _isNoResult

    private val _isTyped = MutableLiveData<Boolean>()
    val isTyped: LiveData<Boolean> get() = _isTyped

    private val _message = MutableLiveData<Event<String>>()
    val message: LiveData<Event<String>> get() = _message

    fun setListUsers(username: String) {
        _isLoading.postValue(true)
        _isTyped.postValue(true)

        val client = ApiConfig.getApiService().getSearchUsers(username, tokenApi)
        client.enqueue(object : Callback<SearchResponse> {
            override fun onResponse(
                call: Call<SearchResponse>,
                response: Response<SearchResponse>
            ) {
                _isLoading.postValue(false)
                if (response.isSuccessful) {
                    _isError.postValue(false)
                    response.body()?.users?.apply {
                        _isNoResult.postValue(isEmpty())
                        listUsers.postValue(this as ArrayList<UserResponse>)
                    }
                } else {
                    _isError.postValue(true)
                    _message.postValue(Event(response.message()))
                }
            }

            override fun onFailure(call: Call<SearchResponse>, t: Throwable) {
                _isLoading.postValue(false)
                _isError.postValue(true)
                _message.postValue(Event(t.message!!))
            }
        })
    }

    fun getListUsers(): LiveData<ArrayList<UserResponse>> {
        return listUsers
    }

    fun saveThemeSetting(isDarkMode: Boolean){
        viewModelScope.launch {
            pref.saveThemeSetting(isDarkMode)
        }
    }

    fun getThemeSetting():LiveData<Boolean>{
        return pref.getThemeSetting().asLiveData()
    }
}