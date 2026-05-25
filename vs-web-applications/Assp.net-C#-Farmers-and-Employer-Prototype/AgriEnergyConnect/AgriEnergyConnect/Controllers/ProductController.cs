using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Mvc.Rendering;
using Microsoft.EntityFrameworkCore;
using AgriEnergyConnect.Data;
using AgriEnergyConnect.Models;
using System.Threading.Tasks;
using System.Linq;

public class ProductController : Controller
{
    private readonly ApplicationDbContext _context;
    public ProductController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: Product
    public async Task<IActionResult> Index()
    {
        var products = _context.Products.Include(p => p.Farmer);
        return View(await products.ToListAsync());
    }

    // GET: Product/Details/5
    public async Task<IActionResult> Details(int? id)
    {
        if (id == null)
            return NotFound();

        var product = await _context.Products
            .Include(p => p.Farmer)
            .FirstOrDefaultAsync(m => m.Id == id);

        if (product == null)
            return NotFound();

        return View(product);
    }

    // GET: Product/Create
    public IActionResult Create()
    {
        var farmers = _context.Farmers.ToList();
        if (!farmers.Any())
        {
            TempData["Error"] = "You must create a farmer before adding a product.";
            return RedirectToAction("Index");
        }

        ViewData["FarmerId"] = new SelectList(farmers, "Id", "Name");
        var product = new Product { ProductionDate = DateTime.Today };
        return View(product);
    }

    // POST: Product/Create
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Create([Bind("Name,Category,ProductionDate,FarmerId")] Product product)
    {
        if (ModelState.IsValid)
        {
            _context.Add(product);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }

        // Log model state errors for debugging
        var errors = ModelState.Values.SelectMany(v => v.Errors).Select(e => e.ErrorMessage).ToList();
        if (errors.Any())
        {
            ViewBag.ModelErrors = errors;
        }

        ViewData["FarmerId"] = new SelectList(_context.Farmers, "Id", "Name", product.FarmerId);
        return View(product);
    }

    // GET: Product/Edit/5
    public async Task<IActionResult> Edit(int? id)
    {
        if (id == null)
            return NotFound();

        var product = await _context.Products.FindAsync(id);
        if (product == null)
            return NotFound();

        ViewData["FarmerId"] = new SelectList(_context.Farmers, "Id", "Name", product.FarmerId);
        return View(product);
    }

    // POST: Product/Edit/5
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Edit(int id, [Bind("Id,Name,Category,ProductionDate,FarmerId")] Product product)
    {
        if (id != product.Id)
            return NotFound();

        if (ModelState.IsValid)
        {
            try
            {
                _context.Update(product);
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!ProductExists(product.Id))
                    return NotFound();
                else
                    throw;
            }
            return RedirectToAction(nameof(Index));
        }
        ViewData["FarmerId"] = new SelectList(_context.Farmers, "Id", "Name", product.FarmerId);
        return View(product);
    }

    // GET: Product/Delete/5
    public async Task<IActionResult> Delete(int? id)
    {
        if (id == null)
            return NotFound();

        var product = await _context.Products
            .Include(p => p.Farmer)
            .FirstOrDefaultAsync(m => m.Id == id);

        if (product == null)
            return NotFound();

        return View(product);
    }

    // POST: Product/Delete/5
    [HttpPost, ActionName("Delete")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> DeleteConfirmed(int id)
    {
        var product = await _context.Products.FindAsync(id);
        if (product != null)
        {
            _context.Products.Remove(product);
            await _context.SaveChangesAsync();
        }
        return RedirectToAction(nameof(Index));
    }

    private bool ProductExists(int id)
    {
        return _context.Products.Any(e => e.Id == id);
    }
}
