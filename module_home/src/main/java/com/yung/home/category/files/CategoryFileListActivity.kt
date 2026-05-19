package com.yung.home.category.files

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

@Route(path = RoutePath.Home.CATEGORY_FILES)
class CategoryFileListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val categoryId = intent.getStringExtra(EXTRA_CATEGORY_ID).orEmpty()
        val categoryName = intent.getStringExtra(EXTRA_CATEGORY_NAME).orEmpty()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: CategoryFileListViewModel = viewModel(
                        factory = CategoryFileListViewModelFactory(
                            application = application,
                            categoryId = categoryId,
                            categoryName = categoryName,
                        ),
                    )
                    CategoryFileListScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                    )
                }
            }
        }
    }

    companion object {
        const val EXTRA_CATEGORY_ID = "extra_category_id"
        const val EXTRA_CATEGORY_NAME = "extra_category_name"

        @JvmStatic
        fun start(context: Context, categoryId: String, categoryName: String) {
            val starter = Intent(context, CategoryFileListActivity::class.java)
                .putExtra(EXTRA_CATEGORY_ID, categoryId)
                .putExtra(EXTRA_CATEGORY_NAME, categoryName)
            context.startActivity(starter)
        }
    }
}
