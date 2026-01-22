package it.dogior.hadEnough

import android.annotation.SuppressLint
import android.content.SharedPreferences
import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.core.content.res.ResourcesCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lagradost.cloudstream3.CommonActivity.showToast

class Settings(
    private val plugin: StreamingCommunityPlugin,
    private val sharedPref: SharedPreferences?,
) : BottomSheetDialogFragment() {
    private var currentLang: String = sharedPref?.getString("lang", "it") ?: "it"
    private var currentLangPosition: Int = sharedPref?.getInt("langPosition", 0) ?: 0

    private fun View.makeTvCompatible() {
        this.setPadding(
            this.paddingLeft + 10,
            this.paddingTop + 10,
            this.paddingRight + 10,
            this.paddingBottom + 10
        )
        this.background = getDrawable("outline")
    }

    // Helper function to get a drawable resource by name
    @SuppressLint("DiscouragedApi")
    @Suppress("SameParameterValue")
    private fun getDrawable(name: String): Drawable? {
        // SOSTITUITO: BuildConfig.LIBRARY_PACKAGE_NAME con nome package hardcoded
        val id = plugin.resources?.getIdentifier(name, "drawable", "it.dogior.hadEnough")
        return id?.let { ResourcesCompat.getDrawable(plugin.resources ?: return null, it, null) }
    }

    // Helper function to get a string resource by name
    @SuppressLint("DiscouragedApi")
    @Suppress("SameParameterValue")
    private fun getString(name: String): String? {
        // SOSTITUITO: BuildConfig.LIBRARY_PACKAGE_NAME con nome package hardcoded
        val id = plugin.resources?.getIdentifier(name, "string", "it.dogior.hadEnough")
        return id?.let { plugin.resources?.getString(it) }
    }

    // Generic findView function to find views by name
    @SuppressLint("DiscouragedApi")
    private fun <T : View> View.findViewByName(name: String): T? {
        // SOSTITUITO: BuildConfig.LIBRARY_PACKAGE_NAME con nome package hardcoded
        val id = plugin.resources?.getIdentifier(name, "id", "it.dogior.hadEnough")
        return findViewById(id ?: return null)
    }

    @SuppressLint("DiscouragedApi")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        // SOSTITUITO: BuildConfig.LIBRARY_PACKAGE_NAME con nome package hardcoded
        val layoutId = plugin.resources?.getIdentifier("settings", "layout", "it.dogior.hadEnough")
        return layoutId?.takeIf { it != 0 }?.let {
            inflater.inflate(it, container, false)
        }
    }

    @SuppressLint("UseSwitchCompatOrMaterialCode")
    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize views
        val headerTw: TextView? = view.findViewByName("header_tw")
        headerTw?.text = getString("header_tw") ?: "Settings"
        
        val labelTw: TextView? = view.findViewByName("label")
        labelTw?.text = getString("label") ?: "Select Language"

        val langsDropdown: Spinner? = view.findViewByName("lang_spinner")
        val langs = arrayOf("it", "en")
        val langDisplayNames = arrayOf("Italiano", "English")
        
        langsDropdown?.adapter = ArrayAdapter(
            requireContext(), 
            android.R.layout.simple_spinner_dropdown_item, 
            langDisplayNames
        )
        langsDropdown?.setSelection(currentLangPosition)

        langsDropdown?.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                currentLang = langs[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {
            }
        }

        val saveBtn: ImageButton? = view.findViewByName("save_btn")
        saveBtn?.makeTvCompatible()
        saveBtn?.setImageDrawable(getDrawable("save_icon"))

        saveBtn?.setOnClickListener {
            sharedPref?.edit()?.apply {
                clear()
                putInt("langPosition", langsDropdown?.selectedItemPosition ?: 0)
                putString("lang", currentLang)
                apply()
            }
            showToast("Saved. Restart the app to apply the settings")
            dismiss()
        }
    }
}
