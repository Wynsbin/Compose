package com.yung.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alibaba.android.arouter.facade.annotation.Route
import com.yung.route.RoutePath

@Route(path = RoutePath.Home.WEIGHT_EDIT)
class GramWeightEditActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val initial = intent.getStringExtra(EXTRA_INITIAL_GRAMS) ?: DEFAULT_INITIAL
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: GramWeightEditViewModel = viewModel(
                        factory = GramWeightEditViewModelFactory(initial),
                    )
                    GramWeightEditScreen(
                        viewModel = viewModel,
                        onClose = { finish() },
                        onComplete = { grams ->
                            setResult(
                                RESULT_OK,
                                Intent().putExtra(EXTRA_RESULT_GRAMS, grams),
                            )
                            finish()
                        },
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_INITIAL_GRAMS = "extra_initial_grams"
        const val EXTRA_RESULT_GRAMS = "extra_result_grams"
        private const val DEFAULT_INITIAL = "300"

        @JvmStatic
        fun start(context: Context, initialGrams: String = DEFAULT_INITIAL) {
            val starter = Intent(context, GramWeightEditActivity::class.java)
                .putExtra(EXTRA_INITIAL_GRAMS, initialGrams)
            context.startActivity(starter)
        }
    }
}
