package com.comiditas.familia.ui.screens.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.comiditas.familia.ui.navigation.Screen
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import com.comiditas.familia.ui.theme.FoodSpacing

@Composable
fun HomeScreen(onNavigate: (String) -> Unit) {
    Scaffold(containerColor = MaterialTheme.colorScheme.background) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(FoodSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(FoodSpacing.inset)
        ) {
            HomeNavigationCard(
                title = "Menú semanal",
                subtitle = "Consulta y edita las comidas de la semana",
                featured = true,
                onClick = { onNavigate(Screen.Calendar.route) }
            )

            HomeNavigationCard(
                title = "Comidas",
                subtitle = "Edita las comidas y a quién le gustan",
                onClick = { onNavigate(Screen.Meals.route) }
            )
            HomeNavigationCard(
                title = "Miembros",
                subtitle = "Administra los 3 miembros de la familia",
                onClick = { onNavigate(Screen.Members.route) }
            )
        }
    }
}

@Composable
private fun HomeNavigationCard(
    title: String,
    subtitle: String,
    featured: Boolean = false,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FoodSpacing.minimumTouchTarget)
            .semantics { role = Role.Button },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.onSurface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)
    ) {
        Column(
            modifier = Modifier.padding(FoodSpacing.medium),
            verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)
        ) {
            if (featured) {
                // An inset heading prioritizes planning without a dashboard-sized hero.
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.medium,
                    color = MaterialTheme.colorScheme.primaryContainer,
                    contentColor = MaterialTheme.colorScheme.primary
                ) {
                    Text(
                        text = title,
                        modifier = Modifier.padding(
                            horizontal = FoodSpacing.medium,
                            vertical = FoodSpacing.small
                        ),
                        style = MaterialTheme.typography.titleLarge
                    )
                }
            } else {
                Text(
                    text = title,
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.titleLarge
                )
            }
            Text(
                text = subtitle,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}

@Preview(name = "Inicio · compacto", showBackground = true, widthDp = 320, heightDp = 480)
@Preview(name = "Inicio · estándar", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Inicio · compacto · texto grande", showBackground = true,
    widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun HomeScreenPreview() {
    ComiditasFamiliaTheme {
        HomeScreen(onNavigate = {})
    }
}
