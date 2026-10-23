package com.example.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.data.model.UserRole
import com.example.ui.components.ReturnRouteBrandHeader

/** Demo portal selection only; production authentication needs a trusted auth provider. */
@Composable
fun LoginScreen(onSignIn: (UserRole) -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            ReturnRouteBrandHeader()
            Spacer(Modifier.height(28.dp))
            Text(
                "Choose your portal",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )
            Text(
                "Select a role-specific demo account",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 6.dp, bottom = 20.dp)
            )

            PortalButton(
                title = "Continue as User / Passenger",
                subtitle = "Find rides, send parcels, and manage bookings",
                role = UserRole.PASSENGER,
                onSignIn = onSignIn
            )
            Spacer(Modifier.height(12.dp))
            PortalButton(
                title = "Continue as Rider / Driver",
                subtitle = "Publish journeys, manage vehicles, and bookings",
                role = UserRole.DRIVER,
                onSignIn = onSignIn
            )
            Spacer(Modifier.height(12.dp))
            PortalButton(
                title = "Continue as Administrator",
                subtitle = "Review platform activity and manage reports",
                role = UserRole.ADMIN,
                onSignIn = onSignIn
            )

            Spacer(Modifier.height(20.dp))
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    "Demo mode: these buttons select bundled sample profiles; they do not verify credentials or protect data. Configure a trusted authentication backend before using real accounts or exposing admin tools.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(14.dp)
                )
            }
        }
    }
}

@Composable
private fun PortalButton(
    title: String,
    subtitle: String,
    role: UserRole,
    onSignIn: (UserRole) -> Unit
) {
    val icon = when (role) {
        UserRole.ADMIN -> Icons.Default.AdminPanelSettings
        UserRole.DRIVER, UserRole.DELIVERY_PROVIDER -> Icons.Default.DirectionsCar
        else -> Icons.Default.Person
    }
    Button(
        onClick = { onSignIn(role) },
        modifier = Modifier.fillMaxWidth(),
        contentPadding = PaddingValues(16.dp)
    ) {
        Icon(icon, contentDescription = null)
        Column(modifier = Modifier.weight(1f).padding(start = 12.dp)) {
            Text(title, fontWeight = FontWeight.Bold)
            Text(subtitle, style = MaterialTheme.typography.bodySmall)
        }
    }
}
