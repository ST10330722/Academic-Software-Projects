using ContractMonthlyClaimSystem.Data;
using ContractMonthlyClaimSystem.Models;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Threading.Tasks;

public class HomeController : Controller
{
    private readonly ApplicationDbContext _context;
    private readonly ILogger<HomeController> _logger;

    public HomeController(ILogger<HomeController> logger, ApplicationDbContext context)
    {
        _logger = logger;
        _context = context;
    }

    public IActionResult Index()
    {
        return View();
    }

    public IActionResult Privacy()
    {
        return View();
    }

    public IActionResult SubmitClaim()
    {
        return View();
    }

    [HttpPost]
    public async Task<IActionResult> SubmitClaim(Claim claim, IFormFile document)
    {
        if (ModelState.IsValid)
        {
            // Log the incoming claim data for debugging purposes
            _logger.LogInformation($"Lecturer: {claim.LecturerName}, Hourly Rate: {claim.HourlyRate}, Hours Worked: {claim.HoursWorked}, Total Amount: {claim.TotalAmount}");

            // Calculate the total amount
            claim.TotalAmount = claim.HourlyRate * claim.HoursWorked;
            claim.Status = "Pending"; // Initially set to pending

            // Handle file upload
            if (document != null && document.Length > 0)
            {
                var uploadsDirectory = Path.Combine(Directory.GetCurrentDirectory(), "wwwroot", "uploads");
                var filePath = Path.Combine(uploadsDirectory, document.FileName);

                if (!Directory.Exists(uploadsDirectory))
                {
                    Directory.CreateDirectory(uploadsDirectory);
                }

                using (var stream = new FileStream(filePath, FileMode.Create))
                {
                    await document.CopyToAsync(stream);
                }

                claim.DocumentPath = filePath;
            }

            // Log the claim before saving to the database
            _logger.LogInformation($"Saving claim with status: {claim.Status} and document path: {claim.DocumentPath}");

            // Add the claim to the database
            _context.Claims.Add(claim);
            await _context.SaveChangesAsync();

            // Log after saving
            _logger.LogInformation($"Claim saved with ID: {claim.Id}");

            return RedirectToAction("ClaimStatus", new { id = claim.Id });
        }

        return View(claim);
    }


    [HttpPost]
    public IActionResult ClaimStatus(int id)
    {
        var claim = _context.Claims.Find(id);

        // Log the claim retrieval process
        _logger.LogInformation($"Attempting to retrieve claim with ID: {id}");

        if (claim == null)
        {
            // Log if the claim is not found
            _logger.LogWarning($"Claim with ID: {id} was not found in the database.");
            ViewBag.ErrorMessage = "Claim not found. Please ensure the claim has been submitted correctly.";
            return View();
        }

        // Perform status update based on the claim data
        if (claim.HoursWorked < 1 || claim.HourlyRate < 50)
        {
            claim.Status = "Rejected";
        }
        else
        {
            claim.Status = "Approved";
        }

        // Log the updated status
        _logger.LogInformation($"Claim with ID: {claim.Id} updated to status: {claim.Status}");

        _context.SaveChanges();
        return View("ClaimStatus", claim);
    }

    [HttpPost]
    public async Task<IActionResult> GenerateInvoice()
    {
        var claims = await _context.Claims
            .Where(c => c.Status == "Approved")
            .ToListAsync();

        return View(claims);
    }

    [ResponseCache(Duration = 0, Location = ResponseCacheLocation.None, NoStore = true)]
    public IActionResult Error()
    {
        return View(new ErrorViewModel { RequestId = Activity.Current?.Id ?? HttpContext.TraceIdentifier });
    }
}
