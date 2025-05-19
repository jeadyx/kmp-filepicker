package io.github.jeadyx.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.jeadyx.FileItem
import io.github.jeadyx.FilePickerMode

@Composable
fun FilePickerDialog(
    title: String,
    mode: FilePickerMode,
    currentPath: String,
    items: List<FileItem>,
    onItemClick: (FileItem) -> Unit,
    onNavigateUp: () -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(title)
                Text(
                    text = currentPath,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        text = {
            Column {
                // 导航栏
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onNavigateUp) {
                        Icon(Icons.Default.ArrowBack, "返回上级")
                    }
                    Text(
                        text = currentPath,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.weight(1f)
                    )
                }

                // 文件列表
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(400.dp)
                ) {
                    items(items) { item ->
                        FileItemRow(
                            item = item,
                            onClick = { onItemClick(item) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                enabled = mode == FilePickerMode.SAVE_FILE || items.any { it.isSelected }
            ) {
                Text(if (mode == FilePickerMode.SAVE_FILE) "保存" else "选择")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        },
        modifier = modifier
    )
}

@Composable
private fun FileItemRow(
    item: FileItem,
    onClick: () -> Unit
) {
    ListItem(
        headlineContent = { Text(item.name) },
        leadingContent = {
            Icon(
                imageVector = when {
                    item.isDirectory -> Icons.Default.CheckCircle
                    item.isSelected -> Icons.Default.CheckCircle
                    else -> Icons.Default.List
                },
                contentDescription = null
            )
        },
        modifier = Modifier.clickable(onClick = onClick)
    )
} 