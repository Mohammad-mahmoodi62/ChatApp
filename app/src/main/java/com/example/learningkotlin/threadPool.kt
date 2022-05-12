import com.example.learningkotlin.BugRepoter
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

object threadPool {
    private val pool: ExecutorService
    init {
        pool = Executors.newFixedThreadPool(4)
    }

    fun run(runner: Runnable ){
        pool.execute(runner)
    }
}