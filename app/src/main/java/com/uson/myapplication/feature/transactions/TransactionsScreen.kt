package com.uson.myapplication.feature.transactions

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.uson.myapplication.feature.home.TransactionItem

@Composable
fun TransactionsScreen(
    allowMockData: Boolean,
    onTransaction: (TransactionItem) -> Unit,
    onAdd: () -> Unit,
    onHome: () -> Unit,
    onReports: () -> Unit,
    onAlerts: () -> Unit,
    viewModel: TransactionsViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    LaunchedEffect(allowMockData) { viewModel.load(allowMockData) }
    TransactionsListScreen(
        state = state,
        onKind = viewModel::selectKind,
        onPreviousMonth = viewModel::previousMonth,
        onNextMonth = viewModel::nextMonth,
        onTransaction = onTransaction,
        onNew = onAdd,
        onHome = onHome,
        onReports = onReports,
        onAlerts = onAlerts,
    )
}
