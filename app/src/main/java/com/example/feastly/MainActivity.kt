package com.example.feastly

import android.content.Context
import android.content.Intent
import android.os.Bundle
import retrofit2.Call
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.viewpager2.widget.ViewPager2
import com.example.feastly.database.RecipeDatabase
import com.example.feastly.entities.Category
import com.example.feastly.entities.Meal
import com.example.feastly.entities.MealsItems
import com.example.feastly.interfaces.GetDataService
import com.example.feastly.retofitclient.RetrofitClientInstance
import com.google.android.material.button.MaterialButton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import pub.devrel.easypermissions.AppSettingsDialog
import pub.devrel.easypermissions.EasyPermissions
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory


class MainActivity : BaseActivity(), EasyPermissions.RationaleCallbacks, EasyPermissions.PermissionCallbacks {

    private var READ_STORAGE_PERM = 123
    private val introSliderAdapter = IntroSliderAdapter(
        listOf(
            IntroSlide(
                "Unleash your inner chef!",
                " Introducing Feastly - Your pocket cookbook & culinary companion.",
                R.drawable.bg
            ),
            IntroSlide(
                "Say goodbye to recipe chaos!",
                "Tired of lost notes, crumpled cookbooks, and dinner dilemmas? Feastly simplifies cooking, bringing order to your kitchen and deliciousness to your table.",
                R.drawable.bugger_real_
            ),
            IntroSlide(
                "Explore, cook, create.",
                "Discover thousands of recipes, whip up perfect meals with step-by-step guides, and personalize your kitchen with [App Name]'s powerful features. Start your culinary journey today!",
                R.drawable.food3
            )
        )
    )


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        readStorageTask()

        val introSliderViewPager: ViewPager2 = findViewById(R.id.introSliderViewPager)
        introSliderViewPager.adapter = introSliderAdapter
        setupIndicators()
        setCurrentIndicator(0)
        introSliderViewPager.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                setCurrentIndicator(position)
            }
        })
        val buttonNext: MaterialButton = findViewById(R.id.buttonNext)
        buttonNext.setOnClickListener{
            if (introSliderViewPager.currentItem + 1 < introSliderAdapter.itemCount){
                introSliderViewPager.currentItem += 1
            }else{
                Intent(applicationContext, AnotherActivity::class.java).also {
                    startActivity(it)
                    finish()//one time slide
                }
            }
        }
        val textSkipIntro: TextView = findViewById(R.id.textSkipIntro)
        textSkipIntro.setOnClickListener {
            Intent(applicationContext, AnotherActivity::class.java).also {
                startActivity(it)
                finish()
            }
        }
    }


    private fun setupIndicators(){
        val indicators = arrayOfNulls<ImageView>(introSliderAdapter.itemCount)
        val layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        layoutParams.setMargins(8, 0, 8, 0)
        for (i in indicators.indices){
            indicators[i] = ImageView(applicationContext)
            indicators[i].apply {
                this?.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
                this?.layoutParams = layoutParams
            }
            val indicatorsContainer: LinearLayout = findViewById(R.id.indicatorsContainer)
            indicatorsContainer.addView(indicators[i])
        }
    }


    private fun setCurrentIndicator(index: Int){
        val indicatorsContainer: LinearLayout = findViewById(R.id.indicatorsContainer)
        val childCount = indicatorsContainer.childCount
        for (i in 0 until childCount){
            val imageView = indicatorsContainer[i] as ImageView
            if (i == index){
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_active
                    )
                )
            }else{
                imageView.setImageDrawable(
                    ContextCompat.getDrawable(
                        applicationContext,
                        R.drawable.indicator_inactive
                    )
                )
            }
        }
    }
    fun getCategories() {
        val service = RetrofitClientInstance.retrofitInstance!!.create(GetDataService::class.java)
        val call = service.getCategoryList()
        call.enqueue(object : Callback<Category>{
            override fun onFailure(call: Call<Category>, t: Throwable) {

                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<Category>,
                response: Response<Category>
            ) {

                for (arr in response.body()!!.categorieitems!!){
                    getMeal(arr.strcategory)
                }
                insertDataIntoRoomDb(response.body())
            }

        })
    }

    fun getMeal(categoryName:String) {
        val service = RetrofitClientInstance.retrofitInstance!!.create(GetDataService::class.java)
        val call = service.getMealList(categoryName)
        call.enqueue(object : Callback<Meal>{
            override fun onFailure(call: Call<Meal>, t: Throwable) {

                Toast.makeText(this@MainActivity, "Something went wrong", Toast.LENGTH_SHORT)
                    .show()
            }

            override fun onResponse(
                call: Call<Meal>,
                response: Response<Meal>
            ) {
                insertMealDataIntoRoomDb(categoryName,response.body())
            }

        })
    }
    fun insertDataIntoRoomDb(category: Category?) {

        launch {
            this.let {
                for (arr in category!!.categorieitems!!){
                    RecipeDatabase.getDatabase(this@MainActivity)
                        .recipeDao().insertCategory(arr)
                }

                //button
            }
        }
    }

    fun insertMealDataIntoRoomDb(categoryName:String,meal: Meal?) {

        launch {
            this.let {


                for (arr in meal!!.mealsItem!!){
                    var mealItemModel = MealsItems(
                        arr.id,
                        arr.idMeal,
                        categoryName,
                        arr.strMeal,
                        arr.strMealThumb
                    )
                    RecipeDatabase.getDatabase(this@MainActivity)
                        .recipeDao().insertMeal(mealItemModel)
                    Log.d("mealData",arr.toString())
                }
            }
        }
    }

    fun clearDataBase(){
        launch {
                this.let {
                    RecipeDatabase.getDatabase(this@MainActivity).recipeDao().clearDb()
                }
        }
    }

    private fun hasReadStoragePermission():Boolean{
        return EasyPermissions.hasPermissions(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
    }

    private fun readStorageTask() {
        if (!hasReadStoragePermission()) {
            clearDataBase()
            getCategories()
        } else {
            EasyPermissions.requestPermissions(
                this,
                "This app needs access to storage,",
                READ_STORAGE_PERM,
                android.Manifest.permission.READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode,permissions,grantResults,this)
    }

    override fun onRationaleAccepted(requestCode: Int) {

    }

    override fun onRationaleDenied(requestCode: Int) {

    }

    override fun onPermissionsGranted(requestCode: Int, perms: MutableList<String>) {
        if (EasyPermissions.somePermissionPermanentlyDenied(this,perms)){
            AppSettingsDialog.Builder(this).build().show()
        }
    }

    override fun onPermissionsDenied(requestCode: Int, perms: MutableList<String>) {

    }

}
