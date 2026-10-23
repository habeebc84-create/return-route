package com.example.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.IssueReport
import com.example.data.model.ReportStatus
import com.example.ui.components.StatusBadge
import com.example.ui.theme.PrimaryBlue
import com.example.ui.theme.StatusSuccess
import com.example.ui.viewmodel.ReturnRouteViewModel

@Composable
fun AdminScreen(
    viewModel: ReturnRouteViewModel,
    modifier: Modifier = Modifier
) {
    val reports by viewModel.issueReports.collectAsState()
    val users = viewModel.repository.sampleUsers

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("admin_screen"),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = "Platform Administration",
                style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "System integrity, user trust scores, and dispute resolution queue.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Platform Metrics Card
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Platform Health & Corridor Metrics", fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceAround
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Active Corridors", style = MaterialTheme.typography.labelSmall)
                            Text("18", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.primary)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("CO2 Saved", style = MaterialTheme.typography.labelSmall)
                            Text("1,420 kg", fontWeight = FontWeight.Black, fontSize = 20.sp, color = StatusSuccess)
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Avg Return Disc.", style = MaterialTheme.typography.labelSmall)
                            Text("28%", fontWeight = FontWeight.Black, fontSize = 20.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        // Issue Reports
        item {
            Text("Trust & Safety Incident Queue", fontWeight = FontWeight.Bold)
        }

        items(reports, key = { it.id }) { report ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(report.subject, fontWeight = FontWeight.Bold)
                        StatusBadge(status = report.status.label)
                    }
                    Text("Reporter: ${report.reporterName} • Category: ${report.category}", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(report.description, fontSize = 13.sp)

                    if (report.status != ReportStatus.RESOLVED) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = { viewModel.resolveReport(report.id, "Reviewed and verified by platform admin.") },
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Resolve Ticket")
                        }
                    }
                }
            }
        }
    }
}
