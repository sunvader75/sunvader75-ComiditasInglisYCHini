package com.comiditas.familia.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Restaurant
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.comiditas.familia.ui.theme.ComiditasFamiliaTheme
import com.comiditas.familia.ui.theme.FoodSpacing
import com.comiditas.familia.ui.theme.MemberColors

@Composable
fun FoodSectionHeader(title: String, modifier: Modifier = Modifier, subtitle: String? = null) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(FoodSpacing.small)) {
        Text(
            text = title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.semantics { heading() }
        )
        if (subtitle != null) {
            Text(subtitle, style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

// Decorative only: actions should use Material buttons with their own touch targets.
@Composable
fun FoodIconContainer(icon: ImageVector, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
        contentColor = MaterialTheme.colorScheme.onPrimaryContainer
    ) {
        Box(Modifier.size(FoodSpacing.minimumTouchTarget), contentAlignment = Alignment.Center) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(24.dp))
        }
    }
}

// Identity colors never carry text: names remain readable for every stored color.
@Composable
fun MemberNameBadge(name: String, identityColor: Color, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier.semantics(mergeDescendants = true) {},
        shape = CircleShape,
        color = MaterialTheme.colorScheme.secondaryContainer,
        contentColor = MaterialTheme.colorScheme.onSecondaryContainer
    ) {
        Row(
            Modifier.padding(horizontal = FoodSpacing.inset, vertical = FoodSpacing.small),
            horizontalArrangement = Arrangement.spacedBy(FoodSpacing.extraSmall),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(12.dp).background(identityColor, CircleShape))
            Text(name, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
fun FoodEmptyState(
    title: String,
    message: String,
    modifier: Modifier = Modifier,
    icon: ImageVector = Icons.Outlined.Restaurant
) {
    Column(
        modifier = modifier.padding(FoodSpacing.large),
        verticalArrangement = Arrangement.spacedBy(FoodSpacing.medium)
    ) {
        FoodIconContainer(icon)
        FoodSectionHeader(title, subtitle = message)
    }
}

@Preview(name = "Fundamentos · claro", showBackground = true)
@Preview(name = "Fundamentos · texto grande", showBackground = true, fontScale = 2f)
@Composable
private fun FoodFoundationPreview() {
    ComiditasFamiliaTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            Column(
                Modifier.verticalScroll(rememberScrollState()).padding(FoodSpacing.screen),
                verticalArrangement = Arrangement.spacedBy(FoodSpacing.large)
            ) {
                FoodSectionHeader("Comidas en familia", subtitle = "Un plan para compartir cada día")
                MemberNameBadge("María", MemberColors.first())
                FoodEmptyState("Todo por preparar", "Añade tus comidas favoritas para empezar.")
            }
        }
    }
}
