package vcmsa.projects.poepart2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*

class ExpenseAdapter(
    private val context: Context,
    private val items: List<Expense>
) : ArrayAdapter<Expense>(context, 0, items) {

    private val inflater = LayoutInflater.from(context)

    override fun getView(pos: Int, convertView: View?, parent: ViewGroup): View {
        val view = convertView
            ?: inflater.inflate(R.layout.list_item_expense, parent, false)

        val exp = items[pos]
        val thumb = view.findViewById<ImageView>(R.id.expenseThumbnail)
        val txt = view.findViewById<TextView>(R.id.expenseText)

        txt.text = "${exp.date} | ${exp.description} | R${"%.2f".format(exp.amount)}"

        if (!exp.photoPath.isNullOrEmpty()) {
            thumb.visibility = View.VISIBLE
            val uri = Uri.parse(exp.photoPath)
            thumb.setImageURI(uri)

            view.setOnClickListener {
                context.startActivity(
                    Intent(Intent.ACTION_VIEW).apply {
                        data = uri
                        type = "image/*"
                    }
                )
            }
        } else {
            thumb.visibility = View.GONE
            view.setOnClickListener(null)
        }

        return view
    }
}
