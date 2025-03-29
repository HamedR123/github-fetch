import retrofit2.http.GET
import retrofit2.http.Path

interface GitHubApiService {
    @GET("/users/{username}")
    suspend fun getUserInfo(@Path("username") user: String): GitHubUser

    @GET("users/{username}/repos")
    suspend fun getRepos(@Path("username") username: String): List<GitHubRepo>
}
