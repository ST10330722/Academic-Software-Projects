using Microsoft.AspNetCore.Mvc;
using AgriEnergyConnect.Data;
using AgriEnergyConnect.Models;
using Microsoft.EntityFrameworkCore;
using System.Threading.Tasks;

public class FarmerController : Controller
{
    private readonly ApplicationDbContext _context;

    public FarmerController(ApplicationDbContext context)
    {
        _context = context;
    }

    // GET: Farmer/Index
    public async Task<IActionResult> Index()
    {
        return View(await _context.Farmers.ToListAsync());
    }

    // GET: Farmer/Details/5
    public async Task<IActionResult> Details(int? id)
    {
        if (id == null)
            return NotFound();

        var farmer = await _context.Farmers
            .FirstOrDefaultAsync(m => m.Id == id);

        if (farmer == null)
            return NotFound();

        return View(farmer);
    }

    // GET: Farmer/Create
    public IActionResult Create()
    {
        return View();
    }

    // POST: Farmer/Create
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Create([Bind("Name,Location,ContactInfo")] Farmer farmer)
    {
        if (ModelState.IsValid)
        {
            _context.Add(farmer);
            await _context.SaveChangesAsync();
            return RedirectToAction(nameof(Index));
        }
        return View(farmer);
    }

    // GET: Farmer/Edit/5
    public async Task<IActionResult> Edit(int? id)
    {
        if (id == null)
            return NotFound();

        var farmer = await _context.Farmers.FindAsync(id);
        if (farmer == null)
            return NotFound();

        return View(farmer);
    }

    // POST: Farmer/Edit/5
    [HttpPost]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> Edit(int id, [Bind("Id,Name,Location,ContactInfo")] Farmer farmer)
    {
        if (id != farmer.Id)
            return NotFound();

        if (ModelState.IsValid)
        {
            try
            {
                _context.Update(farmer);
                await _context.SaveChangesAsync();
            }
            catch (DbUpdateConcurrencyException)
            {
                if (!FarmerExists(farmer.Id))
                    return NotFound();
                else
                    throw;
            }
            return RedirectToAction(nameof(Index));
        }
        return View(farmer);
    }

    // GET: Farmer/Delete/5
    public async Task<IActionResult> Delete(int? id)
    {
        if (id == null)
            return NotFound();

        var farmer = await _context.Farmers
            .FirstOrDefaultAsync(m => m.Id == id);

        if (farmer == null)
            return NotFound();

        return View(farmer);
    }

    // POST: Farmer/Delete/5
    [HttpPost, ActionName("Delete")]
    [ValidateAntiForgeryToken]
    public async Task<IActionResult> DeleteConfirmed(int id)
    {
        var farmer = await _context.Farmers.FindAsync(id);
        if (farmer != null)
        {
            _context.Farmers.Remove(farmer);
            await _context.SaveChangesAsync();
        }
        return RedirectToAction(nameof(Index));
    }

    private bool FarmerExists(int id)
    {
        return _context.Farmers.Any(e => e.Id == id);
    }
}
