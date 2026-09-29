package com.byhenriquesilva.atlasdemods.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.byhenriquesilva.atlasdemods.data.ModsRepository
import com.byhenriquesilva.atlasdemods.data.SecretStore
import com.byhenriquesilva.atlasdemods.ui.screens.catalog.CatalogScreen
import com.byhenriquesilva.atlasdemods.ui.screens.detail.ModDetailScreen
import com.byhenriquesilva.atlasdemods.ui.screens.enviar.EnviarScreen
import com.byhenriquesilva.atlasdemods.ui.screens.gerenciar.GerenciarScreen
import com.byhenriquesilva.atlasdemods.ui.screens.settings.SettingsScreen

private object Routes {
    const val CATALOG = "catalog"
    const val DETAIL = "detail/{modId}"
    const val ENVIAR = "enviar"
    const val GERENCIAR = "gerenciar"
    const val SETTINGS = "settings"
    fun detail(modId: String) = "detail/$modId"
}

@Composable
fun AtlasNavHost(repository: ModsRepository, secretStore: SecretStore) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Routes.CATALOG) {
        composable(Routes.CATALOG) {
            CatalogScreen(
                repository = repository,
                secretStore = secretStore,
                onOpenMod = { id -> navController.navigate(Routes.detail(id)) },
                onOpenEnviar = { navController.navigate(Routes.ENVIAR) },
                onOpenGerenciar = { navController.navigate(Routes.GERENCIAR) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) },
            )
        }

        composable(
            route = Routes.DETAIL,
            arguments = listOf(navArgument("modId") { type = NavType.StringType }),
        ) { backStackEntry ->
            val modId = backStackEntry.arguments?.getString("modId").orEmpty()
            ModDetailScreen(
                repository = repository,
                modId = modId,
                baseUrl = secretStore.baseUrl,
                onBack = { navController.popBackStack() },
            )
        }

        composable(Routes.ENVIAR) {
            EnviarScreen(repository = repository, secretStore = secretStore, onBack = { navController.popBackStack() })
        }

        composable(Routes.GERENCIAR) {
            GerenciarScreen(repository = repository, secretStore = secretStore, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            SettingsScreen(repository = repository, secretStore = secretStore, onBack = { navController.popBackStack() })
        }
    }
}
