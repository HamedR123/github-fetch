import com.google.gson.annotations.SerializedName

data class GitHubUser(
    val id: Int,
    val login: String,
    val followers: Int,
    val following: Int,
    @SerializedName("created_at") val createdAt: String
) {
    var repoList: List<GitHubRepo> = listOf()
}
