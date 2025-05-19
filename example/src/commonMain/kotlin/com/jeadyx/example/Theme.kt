package com.jeadyx.example

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun AppTheme(content: @Composable () -> Unit){
    MaterialTheme(content = content)
//    CompositionLocalProvider(LocalDensity provides Density(density = 1f)) {
//        CompositionLocalProvider(
//            LocalLayoutDirection provides LayoutDirection.Ltr,
//        ) {
//            CompositionLocalProvider(LocalFontFamilyResolver provides FontFamily.Default as FontFamily.Resolver){
//                MaterialTheme(content = content)
//            }
//        }
//    }
}
