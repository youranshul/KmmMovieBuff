package decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.*
import com.arkivanov.decompose.value.Value
import kotlinx.serialization.Serializable

class MovieBuffRootImpl(
    componentContext: ComponentContext,
    private val mainScreen: (ComponentContext, (movieId: Int) -> Unit) -> MainScreenComponent,
    private val movieDetails: (
        ComponentContext, movieId: Int, onBackPressed: () -> Unit
    ) -> DetailsScreenComponent
) : MovieBuffRoot, ComponentContext by componentContext {

    constructor(
        componentContext: ComponentContext
    ) : this(componentContext,
        mainScreen = { childContext, onMovieSelected ->
            MainScreenComponentImpl(childContext, onMovieSelected)
        },
        movieDetails = { childContext, movieId, onBackPressed ->
            DetailsScreenComponentImpl(childContext, movieId) {
                onBackPressed.invoke()
            }
        }
    )

    private val navigation = StackNavigation<Configuration>()

    private val stack = childStack(
        key = "RootComponent",
        source = navigation,
        serializer = Configuration.serializer(),
        initialConfiguration = Configuration.Dashboard,
        handleBackButton = true,
        childFactory = ::createChild
    )

    private fun createChild(
        configuration: Configuration,
        componentContext: ComponentContext
    ): MovieBuffRoot.Child =
        when (configuration) {
            Configuration.Dashboard -> MovieBuffRoot.Child.MainScreen(
                mainScreen(componentContext, ::onMovieSelected)
            )

            is Configuration.Details -> MovieBuffRoot.Child.DetailScreen(
                movieDetails(componentContext, configuration.movieId, ::onDetailsScreenBackPressed)
            )
        }

    private fun onMovieSelected(movieId: Int) {
        println("item onMovieSelected-$movieId")
        navigation.push(Configuration.Details(movieId), onComplete = {
            println("Ansh: isMovieSelected ")
        })
    }


    private fun onDetailsScreenBackPressed(){
        navigation.pop()
    }


    override val childStack: Value<ChildStack<*, MovieBuffRoot.Child>>
        get() = value()

    private fun value() = stack

    @Serializable
    private sealed class Configuration {
        @Serializable
        data object Dashboard : Configuration()

        @Serializable
        data class Details(
            val movieId: Int
        ) : Configuration()
    }
}