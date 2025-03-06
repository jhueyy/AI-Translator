package csc436.aitranslator

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Filter
import android.widget.Filterable
import android.widget.TextView

class LanguageAdapter(context: Context, private val originalList: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, originalList), Filterable {

    private var filteredList: List<String> = originalList

    override fun getCount(): Int {
        return filteredList.size
    }

    override fun getItem(position: Int): String? {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val query = constraint?.toString()?.lowercase() ?: ""
                filteredList = if (query.isEmpty()) {
                    originalList
                } else {
                    originalList.filter { it.lowercase().contains(query) }
                }
                return FilterResults().apply { values = filteredList; count = filteredList.size }
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults?) {
                notifyDataSetChanged()
            }
        }
    }

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView ?: LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false)

        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = getItem(position)
        textView.setTextColor(Color.WHITE) // Ensure text is white
        textView.textSize = 18f
        textView.setPadding(24, 16, 24, 16)
        textView.gravity = Gravity.CENTER
//        textView.setBackgroundResource(R.drawable.ripple_button) // Apply ripple_circle.xml
        textView.setBackgroundResource(0)
        return view
    }



}
