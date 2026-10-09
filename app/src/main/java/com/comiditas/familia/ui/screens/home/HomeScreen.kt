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
    Scaffold { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(FoodSpacing.screen),
            verticalArrangement = Arrangement.spacedBy(FoodSpacing.large)
        ) {
            HomeNavigationCard(
                title = "Menú semanal",
                subtitle = "Consulta y edita las comidas de la semana",
                featured = true,
                onClick = { onNavigate(Screen.Calendar.route) }
            )

            Column(verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium)) {
                HomeNavigationCard(
                    title = "Comidas",
                    subtitle = "Edita las comidas y a quién le gustan",
                    onClick = { onNavigate(Screen.Meals.route) }
                )
                HomeNavigationCard(
                    title = "Miembros",
                    subtitle = "Administra los 3 miembros de la familia",
                    family = true,
                    onClick = { onNavigate(Screen.Members.route) }
                )
            }
        }
    }
}

@Composable
private fun HomeNavigationCard(
    title: String,
    subtitle: String,
    featured: Boolean = false,
    family: Boolean = false,
    onClick: () -> Unit
) {
    val container = when {
        featured -> MaterialTheme.colorScheme.primaryContainer
        family -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surface
    }
    val content = when {
        featured -> MaterialTheme.colorScheme.onPrimaryContainer
        family -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurface
    }
    Card(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = FoodSpacing.minimumTouchTarget)
            .semantics { role = Role.Button },
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(containerColor = container, contentColor = content),
        border = if (!featured && !family) BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant
        ) else null
    ) {
        Column(
            modifier = Modifier.padding(FoodSpacing.large),
            verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)
        ) {
            Text(
                text = title,
                style = if (featured) MaterialTheme.typography.headlineMedium
                    else MaterialTheme.typography.titleLarge
            )
            Text(text = subtitle, style = MaterialTheme.typography.bodyLarge)
        }
    }
}

@Preview(name = "Inicio · estándar", showBackground = true, widthDp = 360, heightDp = 800)
@Preview(name = "Inicio · compacto · texto grande", showBackground = true,
    widthDp = 320, heightDp = 480, fontScale = 2f)
@Composable
private fun HomeScreenPreview() {
    ComiditasFamiliaTheme {
        HomeScreen(onNavigate = {})
    }
}
