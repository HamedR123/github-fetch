import kotlinx.coroutines.*
import retrofit2.HttpException
import kotlin.system.exitProcess

fun httpError(e: HttpException): ApiError? = Dependencies.gson.fromJson(
    e.response()?.errorBody()?.string(),
    ApiError::class.java)

suspend fun getUserInfo(username: String) = supervisorScope {

    val userDeferred = async { Dependencies.gitHub.getUserInfo(username) }
    val reposDeferred = async { Dependencies.gitHub.getRepos(username) }

    val gitHubUser = try {
        println("Fetching user data...")
        userDeferred.await()
    } catch (e: HttpException) {
        println(httpError(e))
        return@supervisorScope null
    } catch (e: Exception) {
        println("Unknown error fetching user: ${e.message}")
        return@supervisorScope null
    }

    val userRepos = try {
        println("Fetching user repos...")
        reposDeferred.await()
    } catch (e: HttpException) {
        println(e)
        emptyList()
    } catch (e: Exception) {
        println("Unknown error fetching repos: ${e.message}")
        emptyList()
    }

    return@supervisorScope Pair(gitHubUser, userRepos)
}

fun printUser(user: GitHubUser) {
    println(user)
    for (repo in user.repoList) {
        println(repo)
    }
}

fun main(): Unit = runBlocking {
    val users: MutableList<GitHubUser> = mutableListOf()
    val allRepos: MutableList<GitHubRepo> = mutableListOf()
    val menu = """
        ============ MENU ============
        0. Show menu
        1. Fetch user data by username
        2. List all saved users
        3. Search user by username
        4. Search repo by name
        5. Exit
        ==============================
        """.trimIndent()
    println(menu)
    while (true) {
        print("Select an option: ")
        val option = readln()
        when (option) {
            "0" -> println(menu)
            "1" -> {
                print("Enter GitHub username: ")
                val username = readln()
                val localUser = users.find { it.login.equals(username, ignoreCase = true) }
                if (localUser != null) {
                    printUser(localUser)
                    continue
                }
                val userInfo = getUserInfo(username) ?: continue
                val (gitHubUser, userRepos) = userInfo
                gitHubUser.repoList = userRepos
                allRepos.addAll(userRepos)
                users.add(gitHubUser)
                printUser(gitHubUser)
            }
            "2" -> {
                if (users.isEmpty()) println("No users saved ⚠")
                for (user in users) {
                    println(user)
                }
            }
            "3" -> {
                print("Enter username to search: ")
                val username = readln().lowercase()
                println("Searching \uD83D\uDD0D")
                val found = users.filter { it.login.lowercase().contains(username) }
                if (found.isEmpty()) println("No user found ⚠")
                found.forEach {
                    println(it)
                }
            }
            "4" -> {
                print("Enter repo name to search: ")
                val query = readln()
                println("Searching \uD83D\uDD0D")
                val matches = allRepos.filter { it.name.contains(query, ignoreCase = true) }
                if (matches.isEmpty()) println("No repositories found ⚠")
                matches.forEach {
                    println(it)
                }
            }
            "5" -> {
                println("Goodbye \uD83D\uDC4B")
                exitProcess(0)
            }
            else -> println("invalid option ❗")
        }
    }
}
