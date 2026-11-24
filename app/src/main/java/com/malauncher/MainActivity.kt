package com.malauncher

import android.content.Intent
import android.content.pm.LauncherApps
import android.content.pm.ResolveInfo
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Process
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.malauncher.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val apps = mutableListOf<AppEntry>()

    data class AppEntry(
        val label: String,
        val packageName: String,
        val activityName: String,
        val icon: Drawable,
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.appGrid.layoutManager = GridLayoutManager(this, COLUMNS)
        binding.appGrid.adapter = AppAdapter()
    }

    override fun onResume() {
        super.onResume()
        loadApps()
    }

    private fun loadApps() {
        val launcherApps = getSystemService(LauncherApps::class.java)
        val profiles = launcherApps.profiles
        apps.clear()
        for (user in profiles) {
            for (info in launcherApps.getActivityList(null, user)) {
                apps += AppEntry(
                    label = info.label.toString(),
                    packageName = info.applicationInfo.packageName,
                    activityName = info.componentName.className,
                    icon = info.getIcon(resources.displayMetrics.densityDpi),
                )
            }
        }
        apps.sortBy { it.label.lowercase() }
        binding.appGrid.adapter?.notifyDataSetChanged()
    }

    private fun launch(entry: AppEntry) {
        val intent = Intent(Intent.ACTION_MAIN)
            .addCategory(Intent.CATEGORY_LAUNCHER)
            .setClassName(entry.packageName, entry.activityName)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
    }

    private inner class AppAdapter : RecyclerView.Adapter<AppAdapter.Holder>() {

        inner class Holder(view: View) : RecyclerView.ViewHolder(view) {
            val icon: ImageView = view.findViewById(R.id.appIcon)
            val label: TextView = view.findViewById(R.id.appLabel)
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_app, parent, false)
            return Holder(view)
        }

        override fun onBindViewHolder(holder: Holder, position: Int) {
            val entry = apps[position]
            holder.icon.setImageDrawable(entry.icon)
            holder.label.text = entry.label
            holder.itemView.setOnClickListener { launch(entry) }
        }

        override fun getItemCount(): Int = apps.size
    }

    companion object {
        private const val COLUMNS = 4
    }
}
