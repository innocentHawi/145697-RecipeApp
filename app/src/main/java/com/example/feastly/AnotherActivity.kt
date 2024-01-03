package com.example.feastly

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.feastly.adapter.MainCategoryAdapter
import com.example.feastly.adapter.SubCategoryAdapter
import com.example.feastly.database.RecipeDatabase
import com.example.feastly.entities.Category
import com.example.feastly.entities.CategoryItems
import com.example.feastly.entities.MealsItems
import com.example.feastly.entities.Recipes
import kotlinx.coroutines.launch

class AnotherActivity : BaseActivity() {
    var arrMainCategory = ArrayList<CategoryItems>()
    var arrSubCategory = ArrayList<MealsItems>()

    var mainCategoryAdapter = MainCategoryAdapter()
    var subCategoryAdapter = SubCategoryAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_another)


        getDataFromDb()

        mainCategoryAdapter.setClickListener(onClicked)
        subCategoryAdapter.setClickListener(onClickedSubItem)



    }

    private val onClicked = object : MainCategoryAdapter.OnItemClickListener{
        override fun onClicked(categoryName: String) {
            getMealDataFromDb(categoryName)
        }
    }

    private val onClickedSubItem = object : SubCategoryAdapter.OnItemClickListener{
        override fun onClicked(id: String) {
            var intent = Intent(this@AnotherActivity,DetailActivity::class.java)
            intent.putExtra("id",id)
            startActivity(intent)
        }
    }

    private fun getDataFromDb(){
        launch{
            this.let {
                var cat = RecipeDatabase.getDatabase(this@AnotherActivity).recipeDao().getAllCategory()
                arrMainCategory = cat as ArrayList<CategoryItems>
                arrMainCategory.reverse()

                getMealDataFromDb(arrMainCategory[0].strcategory)
                mainCategoryAdapter.setData(arrMainCategory)
                val rv_main_category: RecyclerView = findViewById(R.id.rv_main_category)
                rv_main_category.layoutManager = LinearLayoutManager(this@AnotherActivity, LinearLayoutManager.HORIZONTAL, false)
                rv_main_category.adapter = mainCategoryAdapter
            }

        }
    }

    private fun getMealDataFromDb(categoryName:String){
        val tvCategory: TextView = findViewById(R.id.tvCategory)
        tvCategory.text = "$categoryName+Category"
        launch{
            this.let {
                var cat = RecipeDatabase.getDatabase(this@AnotherActivity).recipeDao().getSpecificMealList(categoryName)
                arrSubCategory = cat as ArrayList<MealsItems>
                subCategoryAdapter.setData(arrSubCategory)
                val rv_sub_category: RecyclerView = findViewById(R.id.rv_sub_category)
                rv_sub_category.layoutManager = LinearLayoutManager(this@AnotherActivity, LinearLayoutManager.HORIZONTAL, false)
                rv_sub_category.adapter = subCategoryAdapter
            }
        }
    }
}
