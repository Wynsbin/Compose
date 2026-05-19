package com.yung.home.category.list

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.yung.home.category.files.CategoryFileListActivity
import com.yung.route.RoutePath

@Route(path = RoutePath.Home.CATEGORY_LIST)
class CategoryListActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                Surface(color = Color.Transparent) {
                    val viewModel: CategoryListViewModel = viewModel()
                    CategoryListScreen(
                        viewModel = viewModel,
                        onBack = { finish() },
                        onChildCategoryClick = { categoryId, categoryName ->
                            ARouter.getInstance()
                                .build(RoutePath.Home.CATEGORY_FILES)
                                .withString(CategoryFileListActivity.EXTRA_CATEGORY_ID, categoryId)
                                .withString(CategoryFileListActivity.EXTRA_CATEGORY_NAME, categoryName)
                                .navigation(this@CategoryListActivity)
                        },
                    )
                }
            }
        }
    }
}
